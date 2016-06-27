package com.deepred.subworld.model;

import com.firebase.geofire.GeoLocation;

/**
 * Created by aplicaty on 24/06/16.
 */
public class Rival {
    private GeoLocation loc;
    private boolean isVisible;
    private User user;

    public Rival(GeoLocation _loc, boolean _isVisible) {
        loc = _loc;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
