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
import com.deepred.subworld.utils.IMapUpdatesListener;
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
 * Derives from AbstractMapActivity and implements IMapUpdatesListener methods
 */
public class MapActivityImpl extends AbstractMapActivity implements IMapUpdatesListener, MapboxMap.OnMarkerClickListener {
    private String TAG = "MapActivityImpl";

    // Markers
    private MarkerOptions myMark;
    private Map<String, MarkerOptions> markers;
    private double zoom;

    private Location pendingMyMark;
    private Map<String, LatLng> pendingMarkers;
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
        filter.addAction(ICommon.RIVAL_LOCATION);
        filter.addAction(ICommon.REMOVE_RIVAL_LOCATION);
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
            zoom = Double.valueOf(pendingZoom);
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
                LatLng l = pendingMarkers.get(key);
                pendingMarkers.remove(key);
                updateMarker(key, l);
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

    @Override
    public void updateMarker(String uid, final LatLng latLng) {
        Log.d(TAG, "updateMarker" + latLng.getLatitude() + "," + latLng.getLongitude() + ", uid:" + uid);

        final MarkerOptions m = markers.get(uid);
        if (m != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    m.position(latLng);
                }
            });
        } else {
            final MarkerOptions m2 = new MarkerOptions()
                    .position(latLng)
                    .title("User " + uid)
                    //.icon(icon));
                    .snippet("marker to user " + uid);
            if (map != null) {
                markers.put(uid, m2);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        map.addMarker(m2);
                    }
                });
            } else {
                pendingMarkers.put(uid, latLng);
            }
        }
    }

    @Override
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

                MarkerOptions m = new MarkerOptions()
                        .position(latLng)
                        .title("Me")
                        //.icon(icon)
                        .snippet("my marker");
                myMark = m;

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

    @Override
    public void removeMarker(String uid) {
        if (map != null) {
            final MarkerOptions m = markers.get(uid);
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

    @Override
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

    @Override
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
            for (Map.Entry<String, MarkerOptions> entry : markers.entrySet()) {
                if (entry.getValue().equals(marker)) {
                    Log.d(TAG, "Marker encontrado: " + entry.getKey());
                    Intent intent = new Intent(getApplicationContext(), UserActionActivity.class);
                    this.startActivity(intent);
                }
            }
        }

        return true;
    }
}
