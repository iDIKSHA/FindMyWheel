package com.example.acer.findmywheel;

/**
 * Created by Acer on 08-Sep-17.
 */

//CREDIT: http://javapapers.com/android/android-get-address-with-street-name-city-for-location-with-geocoding/


import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationAddress {
    private static final String TAG = "LocationAddress";

    public static void getAddressFromLocation(final double latitude, final double longitude,
                                              final Context context, final Handler handler) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                String result = null;
                try {
                    List<Address> addressList = geocoder.getFromLocation(
                            latitude, longitude, 1);
                    if (addressList != null && addressList.size() > 0) {
                      /*  Address address = addressList.get(0);
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                            sb.append(address.getAddressLine(i)).append("\n");
                        }*/
                        //  sb.append(address.getLocality()).append("\n");
                        //                  sb.append(address.getPostalCode()).append("\n");
                        //                 sb.append(address.getCountryName());
                        // result = sb.toString();
                        result = addressList.get(0).getAddressLine(0);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Unable connect to Geocoder", e);
                } finally {
                    Message message = Message.obtain();
                    message.setTarget(handler);
                    if (result != null) {
                        message.what = 1;
                        Bundle bundle = new Bundle();
                        //                     result = "Latitude: " + latitude +"\n" + " Longitude: " + longitude +
                        //                             "\n\nAddress:\n" + result;
                        bundle.putString("address", result);
                        bundle.putDouble("lati", latitude);
                        bundle.putDouble("longi", longitude);
                        message.setData(bundle);
                    } else {
                        message.what = 1;
                        Bundle bundle = new Bundle();
                        result = //"Latitude: " + latitude +"\n"+ " Longitude: " + longitude +
                                "\n Unable to get address for this lat-long.";
                        bundle.putString("address", result);
                        message.setData(bundle);
                    }
                    message.sendToTarget();
                }
            }
        };
        thread.start();
    }
}