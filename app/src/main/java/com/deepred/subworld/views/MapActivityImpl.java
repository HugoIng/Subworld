package com.deepred.subworld.views;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;

import com.deepred.subworld.ApplicationHolder;
import com.deepred.subworld.ICommon;
import com.deepred.subworld.R;
import com.deepred.subworld.SubworldApplication;
import com.deepred.subworld.engine.GameService;
import com.deepred.subworld.model.MapMarker;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Derives from AbstractMapActivity
 */
public class MapActivityImpl extends AbstractMapActivity implements MapboxMap.OnMarkerClickListener {
    private String TAG = "MapActivityImpl";

    // Markers
    private MarkerOptions myMark;
    private Map<String, MapMarker> markers;
    private double zoom;
    private Location pendingMyMark;
    private Map<String, MapMarker> pendingMarkers;
    private Double pendingZoom;
    private boolean isGps;
    private MapActivityReceiver serviceReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myMark = null;
        markers = new HashMap<>();
        zoom = 14.00;
        pendingMyMark = null;
        pendingMarkers = new HashMap<>();
        pendingZoom = null;

        serviceReceiver = new MapActivityReceiver(this);
        IntentFilter filter = new IntentFilter(ICommon.MY_LOCATION);
        filter.addAction(ICommon.MAPELEMENT_LOCATION);
        filter.addAction(ICommon.REMOVE_MAPELEMENT_LOCATION);
        filter.addAction(ICommon.SET_ZOOM);
        filter.addAction(ICommon.SET_PROVIDER_INFO);
        registerReceiver(serviceReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent mServiceIntent = new Intent(this, GameService.class);
        mServiceIntent.setData(Uri.parse(ICommon.MAP_ACTIVITY_RESUMED));
        startService(mServiceIntent); // Starts the IntentService
    }

    @Override
    protected void onPause() {
        super.onPause();
        Intent mServiceIntent = new Intent(this, GameService.class);
        mServiceIntent.setData(Uri.parse(ICommon.MAP_ACTIVITY_PAUSED));
        startService(mServiceIntent); // Starts the IntentService
    }

    public void mapReady() {
        Log.d(TAG, "Map is ready");

        map.setOnMarkerClickListener(this);

        // Set pending markers and zoom if they exist
        if (pendingZoom != null) {
            zoom = pendingZoom;
            setZoom(zoom);
        }
        if (pendingMyMark != null) {
            updateMyMarker(pendingMyMark);
        } else {
            updateMyMarker(getLastLocation());
        }
        if (pendingMarkers.size() > 0) {
            ArrayList<String> keys = (ArrayList<String>) pendingMarkers.keySet();

            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                MapMarker elem = pendingMarkers.get(key);
                pendingMarkers.remove(key);
                updateMarker(key, elem.getType(), elem.getMo());
            }
        }
    }

    /*
    * Retrieve stored or default location. Used when the map is shown before any points are provided from the service
     */
    private Location getLastLocation() {
        Location lastLocation;
        SubworldApplication app = ApplicationHolder.getApp();
        String lastLocationLat = app.getPreference(ICommon.LAST_LOCATION_LATITUDE);
        String lastLocationLong = app.getPreference(ICommon.LAST_LOCATION_LONGITUDE);
        String lastLocationProv = app.getPreference(ICommon.LAST_LOCATION_PROVIDER);
        if (lastLocationLat != null && lastLocationLong != null) {
            // Devolvemos la ultima localizacion de localStorage
            lastLocation = new Location(lastLocationProv);
            lastLocation.setLatitude(Double.parseDouble(lastLocationLat));
            lastLocation.setLongitude(Double.parseDouble(lastLocationLong));
        } else {
            // Devolvemos la localizacion por defecto de la app
            lastLocation = new Location(ICommon.DEFAULT_PROVIDER);
            lastLocation.setLatitude(ICommon.DEFAULT_LATITUDE);
            lastLocation.setLongitude(ICommon.DEFAULT_LONGITUDE);
        }
        return lastLocation;
    }

    public void updateMarker(String uid, int type, LatLng latLng) {
        Log.d(TAG, "updateMarker" + latLng.getLatitude() + "," + latLng.getLongitude() + ", uid:" + uid);
        final MapMarker elem = markers.get(uid);
        final MarkerOptions m = elem.getMo();
        if (m != null) {
            updateExistingMarker(m, latLng);
        } else {
            MarkerOptions m2 = new MarkerOptions()
                    .position(latLng)
                    .title("User " + uid)
                    //.icon(icon));
                    .snippet("marker to user " + uid);
            updateNewMarker(uid, type, m2);
        }
    }

    public void updateMarker(String uid, int type, MarkerOptions mo) {
        Log.d(TAG, "updateMarker" + mo.getPosition().getLatitude() + "," + mo.getPosition().getLongitude() + ", uid:" + uid);
        final MapMarker elem = markers.get(uid);
        final MarkerOptions m = elem.getMo();
        if (m != null) {
            updateExistingMarker(m, mo.getPosition());
        } else {
            updateNewMarker(uid, type, mo);
        }
    }

    private void updateExistingMarker(final MarkerOptions m, final LatLng pos) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m.position(pos);
            }
        });
    }

    private void updateNewMarker(String uid, int type, final MarkerOptions mo) {
        MapMarker marker = new MapMarker(mo, type, uid);

        if (map != null) {
            markers.put(uid, marker);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    map.addMarker(mo);
                }
            });
        } else {
            pendingMarkers.put(uid, marker);
        }
    }

    public void updateMyMarker(final Location loc) {
        Log.d(TAG, "updateMyMarker: " + loc.getLatitude() + "," + loc.getLongitude() + ", bearing:" + loc.getBearing() + ", provider:" + loc.getProvider());

        LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());

        final CameraPosition position = new CameraPosition.Builder()
                .target(latLng) // Sets the new camera position
                .zoom(zoom) // Sets the zoom
                .bearing(0) // Rotate the camera
                .tilt(30) // Set the camera tilt
                .build(); // Creates a CameraPosition from the builder

        if (myMark != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    myMark.position(new LatLng(loc.getLatitude(), loc.getLongitude()));

                    map.animateCamera(CameraUpdateFactory
                            .newCameraPosition(position), 3000);
                }
            });
        } else {
            if (map != null) {
                // Create an Icon object for the marker to use
                IconFactory iconFactory = IconFactory.getInstance(this);
                Drawable iconDrawable = ContextCompat.getDrawable(this, R.drawable.arrow_smaller);
                //Icon icon = iconFactory.fromDrawable(iconDrawable);

                myMark = new MarkerOptions()
                        .position(latLng)
                        .title("Me")
                        //.icon(icon)
                        .snippet("my marker");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        map.addMarker(myMark);

                        map.animateCamera(CameraUpdateFactory
                                .newCameraPosition(position), 3000);
                    }
                });
            } else {
                pendingMyMark = loc;
            }
        }
    }

    public void removeMarker(String uid) {
        if (map != null) {
            MapMarker elem = markers.get(uid);
            final MarkerOptions m = elem.getMo();
            if (m != null) {
                markers.remove(uid);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        map.removeMarker(m.getMarker());
                    }
                });
            }
        } else {
            pendingMarkers.remove(uid);
        }
    }

    public void providerChanged(boolean GpsEnabled) {
        Log.d(TAG, "Provider changed: gps enabled:" + GpsEnabled);
        if (isGps != GpsEnabled) {
            isGps = GpsEnabled;
            final ImageView i = (ImageView) findViewById(R.id.gps_state);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isGps) {
                        i.setImageResource(R.drawable.gps);
                    } else {
                        i.setImageResource(R.drawable.wifi);
                    }
                }
            });
        }
    }

    public void setZoom(double _zoom) {
        Log.d("WEB", "setZoom" + zoom);
        zoom = _zoom;

        if (map != null) {
            final CameraPosition position = new CameraPosition.Builder()
                    .zoom(zoom) // Sets the zoom
                    .build(); // Creates a CameraPosition from the builder

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    map.animateCamera(CameraUpdateFactory
                            .newCameraPosition(position), 3000);
                }
            });
        } else {
            pendingZoom = _zoom;
        }
    }


    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {

        if (marker.equals(myMark.getMarker())) {
            Log.d(TAG, "MyMarker encontrado");
            // Open drawer
            drawer.openDrawer(Gravity.LEFT);
        } else {
            for (Map.Entry<String, MapMarker> entry : markers.entrySet()) {
                MarkerOptions m = entry.getValue().getMo();
                if (m.getMarker().equals(marker)) {
                    Log.d(TAG, "Marker encontrado: " + entry.getKey());

                    // Ask for the rival to the service
                    Intent mServiceIntent = new Intent(this, GameService.class);
                    mServiceIntent.setData(Uri.parse(ICommon.MAPELEMENT_SELECTED));
                    mServiceIntent.putExtra(ICommon.UID, entry.getKey());
                    startService(mServiceIntent); // Starts the IntentService
                }
            }
        }

        return true;
    }
}
