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

        switch (action) {
            case ICommon.MY_LOCATION:
                Location loc = intent.getParcelableExtra(ICommon.MY_LOCATION);
                act.updateMyMarker(loc);
                break;
            case ICommon.MAPELEMENT_LOCATION: {
                LatLng latLng = intent.getParcelableExtra(ICommon.MAPELEMENT_LOCATION);
                String uid = intent.getStringExtra(ICommon.UID);
                int type = intent.getIntExtra(ICommon.MAPELEMENT_TYPE, 0);
                act.updateMarker(uid, type, latLng);
                break;
            }
            case ICommon.REMOVE_MAPELEMENT_LOCATION: {
                String uid = intent.getStringExtra(ICommon.UID);
                act.removeMarker(uid);
                break;
            }
            case ICommon.SET_ZOOM:
                float zoom = intent.getFloatExtra(ICommon.SET_ZOOM, 0);
                act.setZoom(zoom);
                break;
            case ICommon.SET_PROVIDER_INFO:
                boolean prov = intent.getBooleanExtra(ICommon.SET_PROVIDER_INFO, false);
                act.providerChanged(prov);
                break;
        }
    }
}
