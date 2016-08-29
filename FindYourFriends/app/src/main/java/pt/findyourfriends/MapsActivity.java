package pt.findyourfriends;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private final static Integer CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    //needed for location and map
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private updateLocationTask mUpTask = null;

    //needed for server communication
    private getFriendsTask mGetTask = null;
    private LocalStorage localStorage = null;

    private Snackbar requestFailed = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        localStorage = (LocalStorage) getApplication();//global user id

        Button menuButton = (Button) findViewById(R.id.buttonMenu);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(MapsActivity.this, MenuActivity.class));
            }
        });

        Button logoutButton = (Button) findViewById(R.id.buttonLogOut);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                localStorage.setUserID(-1);
                startActivity(new Intent(MapsActivity.this, LoginActivity.class));
            }
        });

        //Connect to the Google API service
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        //Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1 * 1000)           //1 sec in ms
                .setFastestInterval(1 * 500);   //1 sec in ms

        requestFailed = Snackbar.make(findViewById(R.id.MapsCoordinatorLayout), R.string.server_error, Snackbar.LENGTH_LONG);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mGoogleApiClient.connect();
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Location services connected.");
        //check the permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            Log.i(TAG, "No last known location.");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

        mGetTask = new getFriendsTask(location);
        mGetTask.execute();
    }

    private void handleNewLocation(Location location) {

        //clear all previous markers
        mMap.clear();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        //set users marker
        mMap.addMarker(new MarkerOptions().position(latLng).title("Here I am!"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        mUpTask = new updateLocationTask(localStorage.getUserID(),
                Double.toString(latLng.latitude) + ".." + Double.toString(latLng.longitude));
        mUpTask.execute();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                //Start and Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
        mGetTask = new getFriendsTask(location);
        mGetTask.execute();

    }

    public class getFriendsTask extends AsyncTask<Void, Void, String> {

        private final Location userLocation;

        getFriendsTask(Location location) {
            userLocation = location;
        }

        @Override
        protected String doInBackground(Void... params) {
            return ServerHandler.getRequest("friend/" + Integer.toString(localStorage.getUserID()));
        }

        @Override
        protected void onPostExecute(final String result) {
            mGetTask = null;

            if (result.equals("-1")) {
                //something went wrong so kill the activity
                finish();
            } else {
                //the builder will take care of the camera for us
                LatLngBounds.Builder builder = new LatLngBounds.Builder();

                //LatLngBounds has weird behaviour with just one or zero
                //markers so keep a counter and a last marker so we can check and correct for that
                //the dummy marker is just for initialization and should never be shown
                MarkerOptions lastMarkerOpt = new MarkerOptions().position(new LatLng(0,0)).title("Dummy marker");;
                Integer markerCounter = 0;

                if(userLocation != null){ //if there is a last known location, place a marker there.
                    LatLng userLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
                    builder.include(userLatLng);
                    MarkerOptions userMarkerOpt = new MarkerOptions().position(userLatLng).title("You are here");
                    lastMarkerOpt = userMarkerOpt;
                    mMap.addMarker(userMarkerOpt);
                    markerCounter += 1;
                }

                try {

                    JSONArray responseArray = new JSONArray(result);

                    for (int i = 0; i < responseArray.length(); i++) {
                        JSONObject response = responseArray.getJSONObject(i);
                        String phone = response.getString("phone");
                        String location = response.getString("location");

                        //make sure location has the correct format
                        if (location.contains("..")) {
                            String[] parts = location.split("\\.\\.");
                            LatLng latLng = new LatLng(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
                            MarkerOptions markerOpt = new MarkerOptions().position(latLng).title(phone);
                            lastMarkerOpt = markerOpt;
                            builder.include(markerOpt.getPosition());
                            mMap.addMarker(markerOpt);
                            markerCounter += 1;
                        } else {
                            Log.d("friend", "No last known location for this friend");
                        }
                    }

                    if(markerCounter == 0){
                        //no markers so we don't have to do anything
                    } else if(markerCounter == 1){
                        //set the camara manually with a city level zoom
                        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(lastMarkerOpt.getPosition(),10F);
                        mMap.moveCamera(cu);
                    } else{
                        //multiple makrers so let LatLngBounds do the work for us
                        LatLngBounds bounds = builder.build();
                        int padding = 50; //offset from edges of the map in pixels
                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                        mMap.moveCamera(cu);

                    }


                } catch (JSONException j) {
                    j.printStackTrace();
                    // ignore
                }
            }
        }

        @Override
        protected void onCancelled() {
            mGetTask = null;

        }
    }


    public class updateLocationTask extends AsyncTask<Void, Void, Integer> {

        private final String mLocation;
        private final Integer mUserID;

        updateLocationTask(Integer userID, String location) {
            mLocation = location;
            mUserID = userID;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            return ServerHandler.postRequest("update/" + Integer.toString(mUserID) + "/" + mLocation);
        }

        @Override
        protected void onPostExecute(final Integer success) {
            mUpTask = null;

            if (success == -1) {
                //something went wrong so tell the user

                requestFailed.show();
            }
            //Everything went according to plan so we don't have to do anything
        }

    }

}