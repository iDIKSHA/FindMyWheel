package com.example.acer.findmywheel;

import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Acer on 19-Dec-17.
 */

class AddressAndLocation {
    private String addrs;
    private Location location;

    public AddressAndLocation() {
        this.addrs = "";
        this.location = new Location("LOCATION");
    }

    public String getAddrs() {
        return addrs;
    }

    public Location getLocation() {
        return location;
    }

    public void put(String s, Location l) {
        addrs = s;
        location = l;
    }

    public JSONObject getJSONObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("address", this.addrs);
            obj.put("lat", this.location.getLatitude());
            obj.put("lon", this.location.getLongitude());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
