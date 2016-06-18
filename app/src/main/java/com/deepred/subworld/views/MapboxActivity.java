package com.deepred.subworld.views;

import android.location.Location;
import android.os.Bundle;
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

import com.deepred.subworld.R;
import com.deepred.subworld.engine.GameManager;
import com.deepred.subworld.model.User;
import com.deepred.subworld.utils.IMarkersListener;
import com.deepred.subworld.utils.MyUserManager;
import com.google.android.gms.maps.model.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

public class MapboxActivity extends AppCompatActivity implements IMarkersListener {

    private String TITLES[] = {"Backpack","Hidden","Thefts","Lost"};
    private int ICONS[] = {android.R.drawable.ic_media_pause,android.R.drawable.ic_media_pause,android.R.drawable.ic_media_play,android.R.drawable.ic_media_pause};
    private String NAME = "";
    private String EMAIL = "";
    private String TAG = "MapboxActivity";
    RecyclerView mRecyclerView;                           // Declaring RecyclerView
    RecyclerView.Adapter mAdapter;                        // Declaring Adapter For Recycler View
    RecyclerView.LayoutManager mLayoutManager;            // Declaring Layout Manager as a linear layout manager
    DrawerLayout drawer;
    private MapView mapView;
    private GameManager gm;
    private boolean isGps;


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

        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView); // Assigning the RecyclerView Object to the xml View

        mRecyclerView.setHasFixedSize(true);                            // Letting the system know that the list objects are of fixed size

        // Menu data
        User u = MyUserManager.getInstance().getUser();
        NAME = u.getName();
        EMAIL = u.getEmail();

        mapView = (MapView) findViewById(R.id.mapboxview);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {

                // Interact with the map using mapboxMap here

            }
        });

        mAdapter = new MyAdapter(TITLES,ICONS,NAME,EMAIL,R.drawable.c2);       // Creating the Adapter of MyAdapter class(which we are going to see in a bit)
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

    /*
    onSaveInstanceState();
    onLowMemory();
    onDestroy();*/

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
        Log.d("WEB", "updateMarker" + latLng.latitude + "," + latLng.longitude + ", uid:" + uid);
        //webview.loadUrl("javascript:updateMarker('" + uid + "'," + latLng.latitude + "," + latLng.longitude + ")");
    }

    @Override
    public void updateMyMarker(Location loc) {
        Log.d("WEB", "updateMyMarker: " + loc.getLatitude() + "," + loc.getLongitude() + ", bearing:" + loc.getBearing() + ", provider:" + loc.getProvider());
        //webview.loadUrl("javascript:updateMyMarker(" + loc.getLatitude() + "," + loc.getLongitude() + "," + loc.getBearing() + ")");
    }

    @Override
    public void removeMarker(String uid) {
        //webview.loadUrl("javascript:removeMarker(" + uid + ")");
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
    public void setZoom(float zoom) {
        Log.d("WEB", "setZoom" + zoom);
        //la webview.loadUrl("javascript:setZoom(" + zoom + ")");
    }

}
