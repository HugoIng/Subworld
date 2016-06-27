package com.deepred.subworld.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.deepred.subworld.ICommon;
import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by aplicaty on 25/06/16.
 */
public class MapActivityReceiver extends BroadcastReceiver {
    private MapActivityImpl act;

    public MapActivityReceiver(MapActivityImpl _act) {
        act = _act;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(ICommon.MY_LOCATION)) {
            Location loc = intent.getParcelableExtra(ICommon.MY_LOCATION);
            act.updateMyMarker(loc);
        } else if (action.equals(ICommon.RIVAL_LOCATION)) {
            LatLng latLng = intent.getParcelableExtra(ICommon.RIVAL_LOCATION);
            String uid = intent.getStringExtra(ICommon.UID);
            act.updateMarker(uid, latLng);
        } else if (action.equals(ICommon.REMOVE_RIVAL_LOCATION)) {
            String uid = intent.getStringExtra(ICommon.UID);
            act.removeMarker(uid);
        } else if (action.equals(ICommon.SET_ZOOM)) {
            float zoom = intent.getFloatExtra(ICommon.SET_ZOOM, 0);
            act.setZoom(zoom);
        } else if (action.equals(ICommon.SET_PROVIDER_INFO)) {
            boolean prov = intent.getBooleanExtra(ICommon.SET_PROVIDER_INFO, false);
            act.providerChanged(prov);
        }
    }
}
