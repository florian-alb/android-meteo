package com.meteo_app_java.data.remote.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Array of location results from the geocoding API.
 */
public class LocationSearchResponse extends ArrayList<LocationSearchResponse.LocationResult> {

    public static class LocationResult {
        public String name;

        @SerializedName("local_names")
        public LocalNames localNames;

        public double lat;
        public double lon;
        public String country;
        public String state;
    }

    public static class LocalNames {
        public String af;
        public String ar;
        public String az;
        public String bg;
        public String ca;
        public String da;
        public String de;
        public String el;
        public String en;
        public String eu;
        public String fa;
        public String fi;
        public String fr;
        public String gl;
        public String he;
        public String hi;
        public String hr;
        public String hu;
        public String id;
        public String it;
        public String ja;
        public String ko;
        public String la;
        public String lt;
        public String mk;
        public String nl;
        public String no;
        public String pl;
        public String pt;
        public String ro;
        public String ru;
        public String sk;
        public String sl;
        public String sr;
        public String sv;
        public String th;
        public String tr;
        public String ua;
        public String vi;
        public String zh;
    }
}