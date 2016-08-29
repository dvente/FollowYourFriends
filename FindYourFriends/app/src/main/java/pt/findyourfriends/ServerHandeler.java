package pt.findyourfriends;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class ServerHandler {

    public static String getRequest(String parameters) {
        try {
            //standard GET request to the server
            String baseUrl = "http://pt2016.liacs.nl/team18/";
            //String baseUrl = "http://10.0.2.2:9000/";
            URL url = new URL(baseUrl + parameters);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");


            // Get response data and build a string out of it so we can send it to onPostExecute
            // that one runs on the UI thread so it can do the necessary work.
            BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = input.readLine()) != null)
                total.append(line).append('\n');
            input.close();
            String readResult = total.toString();

            //check if everything went ok, if not pass special string to say so
            Integer responseCode = connection.getResponseCode();
            Log.d("GET", "Response code: " + Integer.toString(responseCode));
            if (responseCode == 200)
                return readResult;
            else
                return "-1";


        } catch (IOException e) {
            e.printStackTrace();
        }
        return "-1";
    }

    public static Integer postRequest(String parameters) {
        try {

            //standard POST request
            Integer id = -1;
            String baseUrl = "http://pt2016.liacs.nl/team18/";
            //String baseUrl = "http://10.0.2.2:9000/";
            URL url = new URL(baseUrl + parameters);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
            dStream.flush();
            dStream.close();

            //attemptLogin and newUser will return the userID which we'll have to set
            //the rest return nothing, in which case we don't need to do anything
            BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            try {
                JSONArray responseArray;
                //maybe input is empty so make a try
                try {
                    responseArray = new JSONArray(input.readLine());
                    JSONObject response = responseArray.getJSONObject(0);
                    id = response.getInt("id");
                } catch (java.lang.NullPointerException e) {
                    //nothing was sent back so we don't have to do anything
                }
            } catch (JSONException j) {
                //ignore
            }

            input.close();

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                Log.d("POST", "Response code: " + Integer.toString(responseCode));
                //return userID if returned so LoginActivity can set it
                //otherwise return 0 which means a simple OK
                if (id != -1)
                    return id;
                else
                    return 0;
            } else {
                //server trouble, log the response code and let the Activity know something went wrong
                Log.e("POST", "Response code: " + Integer.toString(responseCode));
                return -1;
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }


}
