package com.deepred.subworld.utils;

import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 *
 */
public interface IViewRangeListener {
    void updateMyLocation();

    void updateMapElementLocation(String uid, int type, LatLng l);

    void removeMapElementLocation(String uid, int type);

    void setZoom(float zoom);
}
