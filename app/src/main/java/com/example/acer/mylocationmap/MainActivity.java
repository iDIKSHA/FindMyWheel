package com.example.acer.mylocationmap;

        import android.Manifest;
        import android.annotation.TargetApi;
        import android.app.AlertDialog;
        import android.app.PendingIntent;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.content.IntentSender;
        import android.content.SharedPreferences;
        import android.content.pm.ActivityInfo;
        import android.content.pm.PackageManager;
        import android.location.Location;
        import android.location.LocationListener;
        import android.location.LocationManager;
        import android.net.Uri;
        import android.os.Build;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.Message;
        import android.os.Parcel;
        import android.os.Parcelable;
        import android.preference.PreferenceManager;
        import android.provider.Settings;
        import android.support.annotation.NonNull;
        import android.support.annotation.Nullable;
        import android.support.design.widget.CoordinatorLayout;
        import android.support.design.widget.FloatingActionButton;
        import android.support.design.widget.Snackbar;
        import android.support.v4.app.ActivityCompat;
        import android.support.v4.content.ContextCompat;
        import android.support.v7.app.AppCompatActivity;
        import android.util.Log;
        import android.util.SparseArray;
        import android.view.Gravity;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.widget.Button;
        import android.widget.ProgressBar;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.google.android.gms.common.ConnectionResult;
        import com.google.android.gms.common.api.GoogleApiClient;
        import com.google.android.gms.common.api.PendingResult;
        import com.google.android.gms.common.api.ResultCallback;
        import com.google.android.gms.common.api.Status;
        import com.google.android.gms.location.ActivityRecognition;
        import com.google.android.gms.location.LocationRequest;
        import com.google.android.gms.location.LocationServices;
        import com.google.android.gms.location.LocationSettingsRequest;
        import com.google.android.gms.location.LocationSettingsResult;
        import com.google.android.gms.location.LocationSettingsStatusCodes;
        import com.google.gson.Gson;
  //      import com.vistrav.ask.Ask;

        import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;

        import java.text.DateFormat;
        import java.util.ArrayList;
        import java.util.Date;
        import java.text.SimpleDateFormat;
        import java.util.HashMap;
        import java.util.LinkedHashMap;
        import java.util.List;
        import java.util.Map;

        import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
        import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity{

    Button rbtn,direction;
    FloatingActionButton fab;
    LocationManager lm;
    CoordinatorLayout coordinatorLayout;
    ArrayListHelper arrayListHelper ;
    ArrayList<AddressAndLocation> arrayList = new ArrayList<AddressAndLocation>();
    boolean doubleBackToExitPressedOnce=false;
    static boolean isPermissionFlag=false;

    private static final String TAG = MainActivity.class.getSimpleName();

    protected static final int REQUEST_CHECK_SETTINGS=0x1;
    private RequestPermissionHandler mRequestPermissionHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRequestPermissionHandler = new RequestPermissionHandler();
        handleButtonClicked();

        rbtn=(Button)findViewById(R.id.remember);
        direction=(Button)findViewById(R.id.direction);
        fab = (FloatingActionButton)findViewById(R.id.fab);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordlayout);
        arrayListHelper = new ArrayListHelper(this);

        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        configureButton();
    }

    private void handleButtonClicked(){
        mRequestPermissionHandler.requestPermission(this, new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET
        }, 123, new RequestPermissionHandler.RequestPermissionListener() {
            @Override
            public void onSuccess() {
                //Toast.makeText(MainActivity.this, "request permission success", Toast.LENGTH_SHORT).show();
                isPermissionFlag = true;
            }

            @Override
            public void onFailed() {
                Toast.makeText(MainActivity.this, "request permission failed", Toast.LENGTH_SHORT).show();
                isPermissionFlag = false;
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mRequestPermissionHandler.onRequestPermissionsResult(requestCode, permissions,
                grantResults);
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
                            status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
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
    private void configureButton() {
        rbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), Maps2Activity.class);
                startActivity(i);
            }

          /*      rememberButtonFlag=true;
                lm.requestLocationUpdates("network", 0, 0, ll);
                String st=gtv.getText().toString();
                rgtv.setText(st);
                String stt=atv.getText().toString();
                ratv.setText(stt);
                isRememberButtonFlag=true;
                rememberLocation.setLatitude(lati);
                rememberLocation.setLongitude(longi);


                sparseArray=getArrayPref(getBaseContext(),"MY_DATA");

                al.put(stt,rememberLocation);
                if(sparseArray!= null){
                    sparseArray.put(sparseArray.size(),al);

                }else {
                    sparseArray =new SparseArray<AddressAndLocation>();
                    sparseArray.put(0, al);
                }
                saveArrayPref(getBaseContext(),"MY_DATA",sparseArray);
                Toast.makeText(getBaseContext(),"SPARSEARRAY : \n"+sparseArray+"\n",Toast.LENGTH_LONG).show();
               // direction.setEnabled(true);
            }*/
        });

        direction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arrayList = arrayListHelper.getArray();
                if(arrayList != null &&  arrayList.size()!=0) {
                    if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Intent intent = new Intent(MainActivity.this, MapsActivityRoute.class);
                        intent.putExtra("Uniqid","From_Activity_Main");
                        startActivity(intent);
                    }else{
                        displayLocationSettingsRequest(getBaseContext());
                    }
                }else{
                    setSnackBar(coordinatorLayout, "Add some place first", 1);
                }
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), DisplayStorageActivity.class);
                startActivity(i);
            }
        });
    }


    public static void setSnackBar(View coordinatorLayout, String snackTitle, int duration) {
        Snackbar snackbar;
        switch (duration){
            case 1:
            snackbar = Snackbar.make(coordinatorLayout, snackTitle, Snackbar.LENGTH_LONG);
                break;
            case 2:
                snackbar = Snackbar.make(coordinatorLayout, snackTitle, Snackbar.LENGTH_INDEFINITE);
                break;
            default:
                snackbar = Snackbar.make(coordinatorLayout, snackTitle, Snackbar.LENGTH_SHORT);
        }
        snackbar.show();
        View view = snackbar.getView();
        TextView txtv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        txtv.setGravity(Gravity.CENTER_HORIZONTAL);
    }


    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}