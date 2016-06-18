package com.deepred.subworld.utils;

import com.google.android.gms.maps.model.LatLng;

/**
 *
 */
public interface IViewRangeListener {
    public void updateLocation(String uid, LatLng l, boolean isMe);
    public void removeLocation(String uid);
    public void setZoom(float zoom);
}
