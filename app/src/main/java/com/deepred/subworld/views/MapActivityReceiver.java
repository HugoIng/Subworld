package com.deepred.subworld.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.deepred.subworld.ICommon;
import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by aplicaty on 25/06/16.
 */
public class MapActivityReceiver extends BroadcastReceiver {
    private final static String TAG = "SW VIEWS MapActivityRcv";
    private MapActivityImpl act;

    public MapActivityReceiver(MapActivityImpl _act) {
        act = _act;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        switch (action) {
            case ICommon.MY_LOCATION: {
                Log.v(TAG, "onReceive: MY_LOCATION");
                Bundle bundle = intent.getExtras();
                if (bundle == null) {
                    return;
                }
                Location loc = bundle.getParcelable(ICommon.MY_LOCATION);

                act.updateMyMarker(loc);
                break;
            }
            case ICommon.MAPELEMENT_LOCATION: {
                Log.v(TAG, "onReceive: MAPELEMENT_LOCATION");
                Bundle bundle = intent.getExtras();
                if (bundle == null) {
                    return;
                }
                LatLng latLng = bundle.getParcelable(ICommon.MAPELEMENT_LOCATION);
                String uid = intent.getStringExtra(ICommon.UID);
                int type = intent.getIntExtra(ICommon.MAPELEMENT_TYPE, 0);
                act.updateMarker(uid, type, latLng);
                break;
            }
            case ICommon.REMOVE_MAPELEMENT_LOCATION: {
                Log.v(TAG, "onReceive: REMOVE_MAPELEMENT_LOCATION");
                String uid = intent.getStringExtra(ICommon.UID);
                act.removeMarker(uid);
                break;
            }
            case ICommon.SET_ZOOM:
                Log.v(TAG, "onReceive: SET_ZOOM");
                float zoom = intent.getFloatExtra(ICommon.SET_ZOOM, 0);
                act.setZoom(zoom);
                break;
            case ICommon.SET_PROVIDER_INFO:
                Log.v(TAG, "onReceive: SET_PROVIDER_INFO");
                boolean prov = intent.getBooleanExtra(ICommon.SET_PROVIDER_INFO, false);
                act.providerChanged(prov);
                break;
            case ICommon.SHOW_ACTION_SCREEN:
                Log.v(TAG, "onReceive: SHOW_ACTION_SCREEN");
                //boolean prov = intent.getBooleanExtra(ICommon.SET_PROVIDER_INFO, false);
                act.showActionScreen();
                break;
        }
    }
}
