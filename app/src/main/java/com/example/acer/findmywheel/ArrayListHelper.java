package com.example.acer.findmywheel;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by Acer on 26-Dec-17.
 */

public class ArrayListHelper {
    ArrayList<AddressAndLocation> arrayList = new ArrayList<>();
    Context mContext;

    public ArrayListHelper(Context context) {
        this.mContext = context;
    }

    public ArrayList<AddressAndLocation> getArray() {

        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        String jsonString = appSharedPrefs.getString("MyObject", "");

        Gson gson = new Gson();
        Type listOfLocationType = new TypeToken<ArrayList<AddressAndLocation>>() {
        }.getType();
        arrayList = gson.fromJson(jsonString, listOfLocationType);
        return arrayList;
    }

    public void putArray(ArrayList<AddressAndLocation> array) {
        final SharedPreferences appSharedPrefs;
        appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        // .getDefaultSharedPreferences();
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(array);
        prefsEditor.putString("MyObject", json);
        prefsEditor.commit();
    }
}
