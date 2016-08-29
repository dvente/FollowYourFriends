package pt.findyourfriends;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    //local storage for userID
    private LocalStorage localStorage = null;

    //async tasks to login
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mPhoneView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_activity);

        // Set up the login form.
        mPhoneView = (AutoCompleteTextView) findViewById(R.id.phone);

        Button mPhoneRegisterButton = (Button) findViewById(R.id.phone_register_button);
        Button mPhoneSignInButton = (Button) findViewById(R.id.phone_sign_in_button);
        mPhoneSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mPhoneRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                registerNewUser();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        localStorage = (LocalStorage) getApplication();
    }

    private void registerNewUser() {
        if (mAuthTask != null) {
            return;
        }

        mPhoneView.setError(null);

        String phone = mPhoneView.getText().toString();

        showProgress(true);
        RegisterUserTask mResTask = new RegisterUserTask(phone);
        mResTask.execute((Void) null);
    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mPhoneView.setError(null);

        // Store values at the time of the login attempt.
        String email = mPhoneView.getText().toString();

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        showProgress(true);
        mAuthTask = new UserLoginTask(email);
        mAuthTask.execute((Void) null);
        //}
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        //List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            //    emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        //addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Integer> {

        private final String mPhone;

        UserLoginTask(String phone) {
            mPhone = phone;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            return ServerHandler.postRequest("login/" + mPhone);
        }

        @Override
        protected void onPostExecute(final Integer response) {
            mAuthTask = null;
            showProgress(false);

            if (response != -1) {
                localStorage.setUserID(response);
                startActivity(new Intent(LoginActivity.this, MapsActivity.class));
                super.onPostExecute(response);
                finish();
            } else {
                mPhoneView.setError("We couldn't find your phone in our database, have you registered yet?");
                mPhoneView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }


    public class RegisterUserTask extends AsyncTask<Void, Void, Integer> {

        private final String mPhone;

        RegisterUserTask(String phone) {
            mPhone = phone;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            return ServerHandler.postRequest("new/" + mPhone);
        }

        @Override
        protected void onPostExecute(final Integer response) {
            mAuthTask = null;
            showProgress(false);


            if (response != -1) {
                //set the global userID for later use
                localStorage.setUserID(response);
                startActivity(new Intent(LoginActivity.this, MapsActivity.class));
                super.onPostExecute(response);
                finish();
            } else {
                mPhoneView.setError("You're already registered with us. Please sign in.");
                mPhoneView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

