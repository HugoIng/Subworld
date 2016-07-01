package com.deepred.subworld.model;

import com.firebase.geofire.GeoLocation;
import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by Manuel.Gutierrez on 30/06/2016.
 */
public abstract class MapElement {

    protected GeoLocation loc;
    protected boolean isVisible;

    public MapElement(GeoLocation _loc, boolean _isVisible) {
        loc = _loc;
        isVisible = _isVisible;
    }

    public MapElement(LatLng _ll, boolean _isVisible) {
        loc = new GeoLocation(_ll.getLatitude(), _ll.getLongitude());
        isVisible = _isVisible;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public GeoLocation getLocation() {
        return loc;
    }

    public void setLocation(GeoLocation _loc) {
        loc = _loc;
    }

}
