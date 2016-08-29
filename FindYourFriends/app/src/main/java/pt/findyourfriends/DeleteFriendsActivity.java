package pt.findyourfriends;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("ConstantConditions")
public class DeleteFriendsActivity extends AppCompatActivity {

    private getFriendsTask mGetTask = null;
    private deleteFriendsTask mRejTask = null;
    private LocalStorage localStorage = null;
    private Snackbar requestFailed = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_friends);


        localStorage = (LocalStorage) getApplication();
        requestFailed = Snackbar.make(findViewById(R.id.myCoordinatorLayout), R.string.server_error, Snackbar.LENGTH_LONG);

        //get all friends, rest is handled in onPostExecute
        mGetTask = new getFriendsTask(this);
        mGetTask.execute((Void) null);

    }

    public class getFriendsTask extends AsyncTask<Void, Void, String> {

        //will be needed for programmatically adding the table later
        private final Context mContext;

        getFriendsTask(Context context) {
            mContext = context;

        }

        @Override
        protected String doInBackground(Void... params) {
            return ServerHandler.getRequest("friend/" + Integer.toString(localStorage.getUserID()));
        }

        @Override
        protected void onPostExecute(final String result) {
            mGetTask = null;

            //something went wrong on the server side so inform the user
            if (result.equals("-1")) {
                requestFailed.show();
                finish();

            } else {
                //start dynamically making a table with the friends in it
                //based upon the server results
                TableLayout friendTable = (TableLayout) findViewById(R.id.friendTable);

                try {
                    JSONArray responseArray = new JSONArray(result);
                    //now loop over all results and add a row for each
                    for (int i = 0; i < responseArray.length(); i++) {

                        JSONObject response = responseArray.getJSONObject(i);
                        //final so stOnClickListener can use them
                        final String phone = response.getString("phone");
                        final TableRow tr = new TableRow(mContext);

                        final TextView c1 = new TextView(mContext);
                        c1.setText(phone);

                        final Button b1 = new Button(mContext);
                        b1.setText("Delete");

                        b1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mRejTask = new deleteFriendsTask(phone, localStorage.getUserID());
                                mRejTask.execute((Void) null);
                                tr.removeView(c1);
                                tr.removeView(b1);

                            }
                        });

                        tr.addView(c1);
                        tr.addView(b1);

                        friendTable.addView(tr);
                    }
                } catch (JSONException j) {
                    // ignore
                }
            }
        }

        @Override
        protected void onCancelled() {
            mGetTask = null;

        }
    }

    public class deleteFriendsTask extends AsyncTask<Void, Void, Integer> {

        private final String mPhone;
        private final Integer mUserID;

        deleteFriendsTask(String phone, Integer userID) {
            mPhone = phone;
            mUserID = userID;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            return ServerHandler.postRequest("delete/" + Integer.toString(mUserID) + "/" + mPhone);
        }

        @Override
        protected void onPostExecute(final Integer success) {
            mRejTask = null;

            if (success == -1) {
                //something went wrong so tell the user

                requestFailed.show();
            }
            //Everything went according to plan so we don't have to do anything
            //removal of the button tells the user it was successful
            //which is handled in getFriendsTask.onPostExecute
        }

    }
}

