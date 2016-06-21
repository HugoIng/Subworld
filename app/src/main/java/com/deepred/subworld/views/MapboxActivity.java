package com.deepred.subworld.views;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.deepred.subworld.ICommon;
import com.deepred.subworld.R;
import com.deepred.subworld.engine.GameManager;
import com.deepred.subworld.model.User;
import com.deepred.subworld.utils.IMarkersListener;
import com.deepred.subworld.utils.MyUserManager;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapboxActivity extends AppCompatActivity implements IMarkersListener {

    private String TITLES[] = {"Backpack","Hidden","Thefts","Lost"};
    private int ICONS[] = {android.R.drawable.ic_media_play,android.R.drawable.ic_media_play,android.R.drawable.ic_media_play,android.R.drawable.ic_media_play};
    private String NAME = "";
    private String EMAIL = "";
    private String TAG = "MapboxActivity";
    RecyclerView mRecyclerView;                           // Declaring RecyclerView
    RecyclerView.Adapter mAdapter;                        // Declaring Adapter For Recycler View
    RecyclerView.LayoutManager mLayoutManager;            // Declaring Layout Manager as a linear layout manager
    DrawerLayout drawer;
    private MapView mapView;
    private MapboxMap map;
    private GameManager gm;
    private boolean isGps;

    // Markers
    private MarkerOptions myMark;
    private Map<String,MarkerOptions> markers;
    private double zoom;

    private Location pendingMyMark;
    private Map<String,LatLng> pendingMarkers;
    private Double pendingZoom;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MapboxActivity", "onCreate");
        setTitle("Subworld");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapbox);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        myMark = null;
        markers = new HashMap<>();
        zoom = 14.00;
        pendingMyMark = null;
        pendingMarkers = new HashMap<>();
        pendingZoom = null;

        mapView = (MapView) findViewById(R.id.mapboxview);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {

                // Interact with the map using mapboxMap here
                map = mapboxMap;

                // Set pending markers and zoom if they exist
                if(pendingZoom != null) {
                    zoom = Double.valueOf(pendingZoom);
                    setZoom(zoom);
                }
                if(pendingMyMark != null) {
                    updateMyMarker(pendingMyMark);
                } else {
                    updateMyMarker(gm.getLastLocation());
                }
                if(pendingMarkers.size() > 0) {
                    ArrayList<String> keys = (ArrayList<String>) pendingMarkers.keySet();

                    for(int i=0; i < keys.size(); i++) {
                        String key = keys.get(i);
                        LatLng l = pendingMarkers.get(key);
                        pendingMarkers.remove(key);
                        updateMarker(key, l);
                    }
                }
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView); // Assigning the RecyclerView Object to the xml View
        mRecyclerView.setHasFixedSize(true);                            // Letting the system know that the list objects are of fixed size

        // Menu data
        User u = MyUserManager.getInstance().getUser();
        NAME = u.getName();
        EMAIL = u.getEmail();

        int imgId = -1;
        if (u.getChrType() == ICommon.CHRS_ARCHEOLOGIST)
            imgId = R.drawable.c1;
        else if (u.getChrType() == ICommon.CHRS_FORT_TELLER)
            imgId = R.drawable.c2;
        else if (u.getChrType() == ICommon.CHRS_SPY)
            imgId = R.drawable.c3;
        else if (u.getChrType() == ICommon.CHRS_THIEF)
            imgId = R.drawable.c4;

        mAdapter = new MyAdapter(TITLES,ICONS,NAME,EMAIL,imgId);       // Creating the Adapter of MyAdapter class(which we are going to see in a bit)
        // And passing the titles,icons,header view name, header view email,
        // and header view profile picture

        mRecyclerView.setAdapter(mAdapter);                              // Setting the adapter to RecyclerView
        mLayoutManager = new LinearLayoutManager(this);                 // Creating a layout Manager
        mRecyclerView.setLayoutManager(mLayoutManager);                 // Setting the layout Manager
        drawer = (DrawerLayout) findViewById(R.id.DrawerLayout);        // Drawer object Assigned to the view

        gm = GameManager.getInstance();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        gm.registerListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        gm.unregisterListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( keyCode == KeyEvent.KEYCODE_MENU ) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return true;
            //case R.id.action_settings:
            //    return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}
    }

    @Override
    public void updateMarker(String uid, LatLng latLng) {
        Log.d("WEB", "updateMarker" + latLng.getLatitude() + "," + latLng.getLongitude() + ", uid:" + uid);

        MarkerOptions m = markers.get(uid);
        if(m != null) {
            m.position(latLng);
        } else {
            m = new MarkerOptions()
                    .position(latLng)
                    .title("User " + uid)
                    //.icon(icon));
                    .snippet("marker to user " + uid);
            if(map != null) {
                markers.put(uid, m);
                map.addMarker(m);
                map.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {
                        Toast.makeText(MapboxActivity.this, marker.getTitle(), Toast.LENGTH_LONG).show();
                        return true;
                    }
                });
            } else {
                pendingMarkers.put(uid,latLng);
            }
        }
    }

    @Override
    public void updateMyMarker(Location loc) {
        Log.d("WEB", "updateMyMarker: " + loc.getLatitude() + "," + loc.getLongitude() + ", bearing:" + loc.getBearing() + ", provider:" + loc.getProvider());

        if(myMark != null) {
            myMark.position(new LatLng(loc.getLatitude(), loc.getLongitude()));
        } else {
            if(map != null) {
                LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());

                CameraPosition position = new CameraPosition.Builder()
                        .target(latLng) // Sets the new camera position
                        .zoom(zoom) // Sets the zoom
                        .bearing(0) // Rotate the camera
                        .tilt(30) // Set the camera tilt
                        .build(); // Creates a CameraPosition from the builder

                map.animateCamera(CameraUpdateFactory
                        .newCameraPosition(position), 7000);

                MarkerOptions m = new MarkerOptions()
                        .position(latLng)
                        .title("Me")
                        //.icon(icon));
                        .snippet("my marker");
                myMark = m;
                map.addMarker(myMark);

                map.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {
                        Toast.makeText(MapboxActivity.this, marker.getTitle(), Toast.LENGTH_LONG).show();
                        return true;
                    }
                });

            } else {
                pendingMyMark = loc;
            }
        }
    }

    @Override
    public void removeMarker(String uid) {
        if(map != null) {
            MarkerOptions m = markers.get(uid);
            if (m != null) {
                markers.remove(uid);
                map.removeMarker(m.getMarker());
            }
        } else {
            pendingMarkers.remove(uid);
        }
    }

    @Override
    public void providerChanged(boolean GpsEnabled) {
        Log.d(TAG, "Provider changed: gps enabled:" + GpsEnabled);
        if(isGps != GpsEnabled) {
            isGps = GpsEnabled;
            final ImageView i = (ImageView) findViewById(R.id.gps_state);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(isGps) {
                        i.setImageResource(R.drawable.gps);
                    } else {
                        i.setImageResource(R.drawable.wifi);
                    }
                }
            });
        }
    }

    @Override
    public void setZoom(double zoom) {
        Log.d("WEB", "setZoom" + zoom);
        if(map != null) {
            CameraPosition position = new CameraPosition.Builder()
                    .zoom(zoom) // Sets the zoom
                    .build(); // Creates a CameraPosition from the builder

            map.animateCamera(CameraUpdateFactory
                    .newCameraPosition(position), 7000);
        } else {
            pendingZoom = zoom;
        }
    }

}
