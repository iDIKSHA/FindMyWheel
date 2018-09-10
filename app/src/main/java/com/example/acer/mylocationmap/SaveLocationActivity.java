package com.example.acer.mylocationmap;

import android.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class SaveLocationActivity extends FragmentActivity implements OnMapReadyCallback, android.location.LocationListener {

    private GoogleMap mMap;
    LocationManager lm;
    Location l = new Location("l");
    String st = "", stt;
    TextView adrs_loc;
    Button save;
    AddressAndLocation al = new AddressAndLocation();
    ArrayList<AddressAndLocation> arraylist = new ArrayList<AddressAndLocation>();
    ArrayListHelper arrayListHelper;
    SparseArray<AddressAndLocation> sparseArray = new SparseArray<AddressAndLocation>();
    // LocationListener ll;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        adrs_loc = (TextView) findViewById(R.id.adrs_loc);
        save = (Button) findViewById(R.id.save);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveListener();
            }
        });

        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        arrayListHelper = new ArrayListHelper(this);

    }

    private void saveListener() {
        al = new AddressAndLocation();
        al.put(adrs_loc.getText().toString(), l);
        arraylist = arrayListHelper.getArray();
        if(arraylist != null) {
            arraylist.add(al);
            arrayListHelper.putArray(arraylist);
        }else {
            arraylist = new ArrayList<AddressAndLocation>();
            arraylist.add(al);
            arrayListHelper.putArray(arraylist);
        }
        Intent i = new Intent(this, DisplayStorageActivity.class);
        startActivity(i);
        finish();
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

        double currentLat, currentLong;
        Location loc = getCurrentLocation(this);
        if (loc != null) {
            currentLat = loc.getLatitude();
            currentLong = loc.getLongitude();
            LatLng currentLocation = new LatLng(currentLat, currentLong);
            mMap.addMarker(new MarkerOptions().position(currentLocation).title("Marker in " + currentLocation));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));

            stt = "\nLat :" + String.format("%5f", currentLat) + "\nLong :" + String.format("%5f", currentLong);
            l.setLatitude(currentLat);
            l.setLongitude(currentLong);
            adrs_loc.setText(stt);
        }else{
            adrs_loc.setText("Tap on map to get precise position.");
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (mMap != null) {
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(latLng));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                    l.setLatitude(latLng.latitude);
                    l.setLongitude(latLng.longitude);
                    // putOnTextview(l.getLatitude(), l.getLongitude());

                    LocationAddress locationAddress = new LocationAddress();
                    locationAddress.getAddressFromLocation(l.getLatitude(), l.getLongitude(),
                            getApplicationContext(), new GeocoderHandler());
                }
            }
        });

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                        android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.INTERNET
                }, 10);
            }
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

    }

    private void putOnTextview(double currentLat, double currentLong, String adr) {

        stt = "\nLat :" + String.format("%5f", currentLat) + "\nLong :" + String.format("%5f", currentLong) + "\n";
        //Toast.makeText(this, ""+currentLat+"\n"+currentLong, Toast.LENGTH_LONG).show();
        adrs_loc.setText(stt);


        if (adr.equalsIgnoreCase("Unable to get address for this lat-long.") || adr.equalsIgnoreCase("Unable connect to Geocoder") || (adr.isEmpty())) {
            stt += "";

        } else {
            stt += "\n" + adr;
        }
        adrs_loc.setText(stt);
    }


    private Location getCurrentLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                // Location is always null on S7 (only)
              //  Log.i(TAG, ">>> getLastKnownLocationObject() getLastKnownLocation: " + location);
                return location;
            }
        } catch (Exception e) {
            Log.e("EXCEPTION", ">>> getLastKnownLocationObject() exception", e);
        }
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {     }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {    }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }

                //http://javapapers.com/android/android-get-address-with-street-name-city-for-location-with-geocoding/

                private class GeocoderHandler extends Handler {
                    @Override
                    public void handleMessage(Message message) {
                        String locationAddress;
                        double lt=0.0,ln=0.0;
                        switch (message.what) {
                            case 1:
                                Bundle bundle = message.getData();
                                locationAddress = bundle.getString("address");
                                lt = bundle.getDouble("lati");
                                ln = bundle.getDouble("longi");
                                break;
                            default:
                                locationAddress = "";
                        }
                        st=locationAddress;
                        putOnTextview(lt, ln, st);
                    }
                }

            }
