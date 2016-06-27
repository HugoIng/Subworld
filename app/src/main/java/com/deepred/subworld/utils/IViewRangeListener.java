package com.deepred.subworld.utils;

//import com.google.android.gms.maps.model.LatLng;

import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 *
 */
public interface IViewRangeListener {
    public void updateMyLocation();

    public void updateRivalLocation(String uid, LatLng l);

    public void removeRivalLocation(String uid);
    public void setZoom(float zoom);
}
