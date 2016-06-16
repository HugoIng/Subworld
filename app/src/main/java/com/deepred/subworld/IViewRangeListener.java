package com.deepred.subworld;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by aplicaty on 29/02/16.
 */
public interface IViewRangeListener {
    public void updateLocation(String uid, LatLng l, boolean isMe);
    public void removeLocation(String uid);
    public void setZoom(float zoom);
}
