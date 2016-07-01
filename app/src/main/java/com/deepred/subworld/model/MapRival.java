package com.deepred.subworld.model;

import com.firebase.geofire.GeoLocation;
import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by aplicaty on 24/06/16.
 */
public class MapRival extends MapElement {
    private User user;

    public MapRival(GeoLocation _loc, boolean _isVisible) {
        super(_loc, _isVisible);

    }

    public MapRival(LatLng _ll, boolean _isVisible) {
        super(_ll, _isVisible);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
