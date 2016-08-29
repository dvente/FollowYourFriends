package pt.findyourfriends;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

@SuppressWarnings("ConstantConditions")
public class AddFriendsActivity extends AppCompatActivity {

    private SendRequestTask mRequestTask = null;
    private Snackbar requestSucceeded = null;
    private Snackbar requestFailed = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);
        final LocalStorage localStorage = (LocalStorage) getApplication();

        requestSucceeded = Snackbar.make(findViewById(R.id.myCoordinatorLayout), R.string.request_sent_successful, Snackbar.LENGTH_SHORT);
        requestFailed = Snackbar.make(findViewById(R.id.myCoordinatorLayout), R.string.request_number_unknown, Snackbar.LENGTH_LONG);

        Button submitButton = (Button) findViewById(R.id.buttonSubmit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AutoCompleteTextView phoneNumberView = (AutoCompleteTextView) findViewById(R.id.phone_number);
                String phone = phoneNumberView.getText().toString();
                mRequestTask = new SendRequestTask(phone, localStorage.getUserID());
                mRequestTask.execute((Void) null);
            }
        });


    }

    public class SendRequestTask extends AsyncTask<Void, Void, Integer> {

        private final String mPhone;
        private final Integer mUserID;

        SendRequestTask(String phone, Integer userID) {
            mPhone = phone;
            mUserID = userID;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            return ServerHandler.postRequest("request/" + mPhone + "/" + Integer.toString(mUserID));
        }

        @Override
        protected void onPostExecute(final Integer success) {
            mRequestTask = null;

            if (success == -1) {
                requestFailed.show();
            } else {
                requestSucceeded.show();
            }
        }

    }
}
