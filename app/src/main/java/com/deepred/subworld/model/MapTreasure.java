package com.deepred.subworld.model;

import com.firebase.geofire.GeoLocation;
import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 *
 */
public class MapTreasure extends MapElement {
    private Treasure t;

    public MapTreasure(GeoLocation _loc, boolean _isVisible) {
        super(_loc, _isVisible);
    }

    public MapTreasure(LatLng _ll, boolean _isVisible) {
        super(_ll, _isVisible);
    }

    public Treasure getT() {
        return t;
    }

    public void setT(Treasure t) {
        this.t = t;
    }
}
