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

public class RequestsActivity extends AppCompatActivity {

    private getRequestsTask mGetTask = null;
    private rejectRequestTask mRejTask = null;
    private acceptRequestTask mAccTask = null;

    private LocalStorage localStorage = null;

    private Snackbar requestFailed = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        localStorage = (LocalStorage) getApplication();
        requestFailed = Snackbar.make(findViewById(R.id.myCoordinatorLayout), R.string.server_error, Snackbar.LENGTH_LONG);

        //start getting all requests, the rest is handled in onPosExecute
        mGetTask = new getRequestsTask(this);
        mGetTask.execute((Void) null);

    }

    public class getRequestsTask extends AsyncTask<Void, Void, String> {

        //will be needed for programmatically adding the table later
        private final Context mContext;

        getRequestsTask(Context context) {
            mContext = context;

        }

        @Override
        protected String doInBackground(Void... params) {
            return ServerHandler.getRequest("request/" + Integer.toString(localStorage.getUserID()));
        }

        @Override
        protected void onPostExecute(final String result) {
            mGetTask = null;

            //something went wrong on the server side so inform the user
            if (result.equals("-1")) {
                requestFailed.show();
                finish();

            } else if (result.isEmpty()) {
                Snackbar noRequests = Snackbar.make(findViewById(R.id.myCoordinatorLayout), "You currently have no friend requests.", Snackbar.LENGTH_LONG);
                noRequests.show();
            } else {
                //start dynamically making a table based on the server results
                TableLayout requestTable = (TableLayout) findViewById(R.id.requestTable);
                try {
                    JSONArray responseArray = new JSONArray(result);
                    for (int i = 0; i < responseArray.length(); i++) {
                        JSONObject response = responseArray.getJSONObject(i);
                        final String phone = response.getString("phone");
                        final TableRow tr = new TableRow(mContext);

                        final TextView c1 = new TextView(mContext);
                        c1.setText(phone);

                        final Button b1 = new Button(mContext);
                        b1.setText("Accept");
                        final Button b2 = new Button(mContext);
                        b2.setText("Reject");

                        b1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mAccTask = new acceptRequestTask(phone, localStorage.getUserID());
                                mAccTask.execute((Void) null);
                                tr.removeView(c1);
                                tr.removeView(b1);
                                tr.removeView(b2);
                            }
                        });

                        b2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mRejTask = new rejectRequestTask(phone, localStorage.getUserID());
                                mRejTask.execute((Void) null);
                                tr.removeView(c1);
                                tr.removeView(b1);
                                tr.removeView(b2);

                            }
                        });

                        tr.addView(c1);
                        tr.addView(b1);
                        tr.addView(b2);

                        requestTable.addView(tr);
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


    public class acceptRequestTask extends AsyncTask<Void, Void, Integer> {

        private final String mPhone;
        private final Integer mUserID;

        acceptRequestTask(String phone, Integer userID) {
            mPhone = phone;
            mUserID = userID;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            return ServerHandler.postRequest("accept/" + Integer.toString(mUserID) + "/" + mPhone);
        }

        @Override
        protected void onPostExecute(final Integer success) {

            if (success == -1) {
                //something went wrong so tell the user

                requestFailed.show();
            }
            //Everything went according to plan so we don't have to do anything
        }


    }

    public class rejectRequestTask extends AsyncTask<Void, Void, Integer> {

        private final String mPhone;
        private final Integer mUserID;

        rejectRequestTask(String phone, Integer userID) {
            mPhone = phone;
            mUserID = userID;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            return ServerHandler.postRequest("reject/" + Integer.toString(mUserID) + "/" + mPhone);
        }

        @Override
        protected void onPostExecute(final Integer success) {
            mRejTask = null;

            if (success == -1) {
                //something went wrong so tell the user

                requestFailed.show();
            }
            //Everything went according to plan so we don't have to do anything
        }

    }
}

