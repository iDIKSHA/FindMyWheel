package com.example.acer.mylocationmap;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.acer.mylocationmap.MainActivity.setSnackBar;

public class MapsActivityRoute extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private boolean isRouteAvailable;
    CoordinatorLayout coordinatorLayout;
    static Location to_location = new Location("to_location");
    private static final String TAG = MapsActivityRoute.class.getSimpleName();

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    static LatLng FROM_LOCATION, TO_LOCATION;
    ArrayList<AddressAndLocation> arrayList = new ArrayList<>();
    ArrayListHelper arrayListHelper;
    AddressAndLocation al;

    double latitude, longitude;
    LocationManager locationManager;
    LocationListener locationListener;
    TextView mTitleTextView;
    View locationButton;


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_route);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordlayout);

        locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1")).
                getParent()).findViewById(Integer.parseInt("2"));

        android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);

        View mCustomView = mInflater.inflate(R.layout.custom_actionbar, null);
        mTitleTextView = (TextView) mCustomView.findViewById(R.id.action_bar_tv);
        mTitleTextView.setText("");
        arrayListHelper = new ArrayListHelper(this);

        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);
        locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                setMessage(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
                displayLocationSettingsRequest(getBaseContext());
            }
        };

        //obtain  Intent Object send  from SenderActivity
        Intent intent = this.getIntent();
        if (intent != null) {
            String strdata = intent.getExtras().getString("Uniqid");
            if (strdata.equals("From_Activity_Main")) {
                arrayList = arrayListHelper.getArray();
                int x = arrayList.size();
                if (arrayList != null) {
                    // if (!(x == 0)) {
                    Location to = arrayList.get(x - 1).getLocation();
                    if (to != null) {
                        latitude = to.getLatitude();
                        longitude = to.getLongitude();
                        TO_LOCATION = new LatLng(latitude, longitude);
                        to_location.setLatitude(latitude);
                        to_location.setLongitude(longitude);
                    }
                }
            }
            if (strdata.equals("From_Activity_Storage")) {
                latitude = intent.getDoubleExtra("lati", 0.0);
                longitude = intent.getDoubleExtra("longi", 0.0);
                TO_LOCATION = new LatLng(latitude, longitude);
                to_location.setLatitude(latitude);
                to_location.setLongitude(longitude);
            }

        }
        locationManager.requestLocationUpdates("gps", 0, 0, locationListener);
    }

    public String getEmojiByUnicode(int unicode) {
        return new String(Character.toChars(unicode));
    }

    public void sendNotification() {
        int laughing = 0x1F604;
        int dancing_girlUnicode = 0x1F483;
        int eyes_unicode = 0x1F440;
        String emoji = getEmojiByUnicode(laughing);
        String dancing_girl = getEmojiByUnicode(dancing_girlUnicode);
        String eyes = getEmojiByUnicode(eyes_unicode);
        //Get an instance of NotificationManager//
        String text = "You seem to be approaching!" + dancing_girl + '\n' + "Just a little eyeballing" + eyes + emoji;
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentTitle("FindMyWheel")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(text))
                        .setContentText(text)
                        .setAutoCancel(true)
                        .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(), 0));

        // Gets an instance of the NotificationManager service//

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // When you issue multiple notifications about the same type of event,
        // it’s best practice for your app to try to update an existing notification
        // with this new information, rather than immediately creating a new notification.
        // If you want to update this notification at a later date, you need to assign it an ID.
        // You can then use this ID whenever you issue a subsequent notification.
        // If the previous notification is still visible, the system will update this existing notification,
        // rather than create a new one. In this example, the notification’s ID is 001//

        mNotificationManager.notify(001, mBuilder.build());
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                        android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.INTERNET
                }, 10);
            }
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
     /*   mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {

                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    displayLocationSettingsRequest(getBaseContext());
                }
                return true;
            }
        });*/

        // and next place it, for exemple, on bottom right (as Google Maps app)
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        // position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 1);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        markerForVehicle(TO_LOCATION);
        setupMap();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    private void markerForVehicle(LatLng vehicleLatLng) {
        // Define marker options
        MarkerOptions markerOptions = new MarkerOptions()
                .position(vehicleLatLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        mMap.addMarker(markerOptions);
    }

    private void setupMap() {
        Location loc = getCurrentLocation(this);
        if (loc != null) {
//            locationManager.requestLocationUpdates("gps", 0, 0, locationListener);
            //Toast.makeText(this,"Location not null",Toast.LENGTH_LONG).show();
            LatLng currentLocation = new LatLng(loc.getLatitude(), loc.getLongitude());
            FROM_LOCATION = currentLocation;
            mMap.addMarker(new MarkerOptions().position(currentLocation).title("Marker in " + currentLocation));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
            //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));
            drawNow();
            setMessage(loc);
        } else {
            Toast.makeText(this, "Location : NULL", Toast.LENGTH_LONG).show();
        }
    }

    private void setMessage(Location location) {
        if (isRouteAvailable) {
            float d = to_location.distanceTo(location);

            if (d <= 0) {
                float distInCM = d * 100;
                mTitleTextView.setText("You are " + String.format("%.0f", distInCM) + "cm away from this place");

            } else {
                mTitleTextView.setText("You are " + String.format("%.2f", d) + "m away from this place");
            }
            if (d < 5) {
                sendNotification();
            }
            if (d > 1000) {
                float distInKM = d / 1000;
                mTitleTextView.setText("You are " + String.format("%.2f", distInKM) + "km away from this place");
            }
        }
    }


    private Location  getCurrentLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = null;
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates("gps", 0, 0, locationListener);
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                // Location is always null on S7 (only)
                //  Log.i(TAG, ">>> getLastKnownLocationObject() getLastKnownLocation: " + location);+
                if (location != null)
                    Log.e("LLOOCCAATTIIOONN::::: ", "" + location.getLongitude() + "LATITUDE: " + location.getLatitude());
//                return location;
            }
        } catch (Exception e) {
            Log.e("EXCEPTION", ">>> getLastKnownLocationObject() exception", e);
        }
        return location;
    }

    private void drawNow() {

        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(FROM_LOCATION, TO_LOCATION);

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
        //}
    }


    public void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @TargetApi(Build.VERSION_CODES.DONUT)
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(MapsActivityRoute.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }


    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();


            parserTask.execute(result);

        }
    }


    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.RED);
                lineOptions.geodesic(true);

            }

            if (lineOptions != null) {
                isRouteAvailable = true;
// Drawing polyline in the Google Map for the i-th route
                mMap.addPolyline(lineOptions);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(FROM_LOCATION, 12));
            } else {
                isRouteAvailable = false;
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(TO_LOCATION, 3));
                setSnackBar(coordinatorLayout, "No Route Found", 2);
            }
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}
