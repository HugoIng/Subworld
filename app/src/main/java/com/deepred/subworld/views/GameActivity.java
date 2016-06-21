package com.deepred.subworld.views;

import android.animation.ValueAnimator;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import com.deepred.subworld.ApplicationHolder;
import com.deepred.subworld.R;
import com.deepred.subworld.ServiceBoot;
import com.deepred.subworld.engine.GameManager;
import com.deepred.subworld.utils.DoubleArrayEvaluator;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

public class GameActivity extends AppCompatActivity implements OnMapReadyCallback/*, IMarkersListener*/ {

    private GoogleMap mMap;
    private GameManager gm;

    // Markers
    private Marker myMark;
    private Map<String,Marker> markers;

    private DrawerLayout drawer;
    private ActionBarDrawerToggle mDrawerToggle;

    private boolean isGps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        setContentView(R.layout.activity_game);
        drawer = (DrawerLayout) findViewById(R.id.drawer);
        setupDrawerLayout();

        if(savedInstanceState == null) {

        }

        myMark = null;
        markers = new HashMap<String, Marker>();
        isGps = false;

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Temporal
        gm = GameManager.getInstance();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Handle the drawer Actions
        if(mDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }

        if(id == R.id.action_center_map) {
            if(myMark == null)
                return true;

            //Centrar el mapa
            mMap.animateCamera(CameraUpdateFactory.newLatLng(myMark.getPosition()));
            return true;
        } else if(id == R.id.action_switch_location_source) {
            if(isGps) {
                item.setIcon(R.drawable.wifi);
                Toast.makeText(this, "Using network location provider", Toast.LENGTH_LONG).show();
            } else {
                item.setIcon(R.drawable.gps);
                Toast.makeText(this, "Using gps location provider", Toast.LENGTH_LONG).show();
            }
            isGps = !isGps;

            ServiceBoot serv = ApplicationHolder.getApp().getServiceBoot();
            if(serv != null)
                serv.switchProvider(isGps);
            else
                ServiceBoot.setProvider(isGps);
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerLayout() {
        // Instantiate the Drawer Toggle
        mDrawerToggle = new ActionBarDrawerToggle(this, drawer, R.string.app_name, R.string.app_name){

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
                getSupportActionBar().setTitle(getString(R.string.app_name));
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
                getSupportActionBar().setTitle(getString(R.string.app_name));
            }
        };

        // Set the Toggle on the Drawer, And tell the Action Bar Up Icon to show
        drawer.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    /*@Override
    public void onNavSelected(int position) {
        Toast.makeText(this, "Selected item: " + position + " from nav", Toast.LENGTH_SHORT).show();
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        //gm.registerListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        gm.unregisterListener();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Other supported types include: MAP_TYPE_NORMAL,
        // MAP_TYPE_TERRAIN, MAP_TYPE_HYBRID and MAP_TYPE_NONE, MAP_TYPE_SATELLITE
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        UiSettings mUiSettings = mMap.getUiSettings();
        mUiSettings.setCompassEnabled(true);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return showMarkerInfo(marker);
            }
        });

        Location loc = gm.getLastLocation();
        if(loc == null) {
            LatLng pos = new LatLng(32, -44); // In the middle of the ocean!!!
            float baseZoom = 12.0f;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, baseZoom));
        } else {
            updateMyMarker(loc);
        }
    }

    private boolean showMarkerInfo(Marker marker) {
        return false; // false por ahora para que muestre por defecto
    }

    //@Override
    public void updateMarker(String uid, LatLng l) {
        Marker m = markers.get(uid);
        if (m != null) {
            animateMarker(m, l);
        } else {
            markers.put(uid, mMap.addMarker(new MarkerOptions().position(l).title(uid)/*.snippet("My Snippet")*/.icon(BitmapDescriptorFactory.fromResource(android.R.drawable.presence_offline))));
        }
    }

    //@Override
    public void updateMyMarker(Location loc) {
        LatLng l = new LatLng(loc.getLatitude(), loc.getLongitude());
        if(myMark != null) {
            animateMarker(myMark, l);
            mMap.animateCamera(CameraUpdateFactory.newLatLng(l));
        } else {
            //myMark = mMap.addMarker(new MarkerOptions().position(l).title(getString(R.string.you_are_here))/*.snippet("My Snippet")*/.icon(BitmapDescriptorFactory.fromResource(android.R.drawable.presence_online)));
            myMark = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow_smaller)).position(l).flat(true)/*.rotation(245)*/);

            CameraPosition cameraPosition = CameraPosition.builder().target(l).zoom(13).bearing(loc.getBearing()).tilt(30).build();

            // Animate the change in camera view over 2 seconds
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000, null);
        }
    }

    //@Override
    public void removeMarker(String uid) {
        Marker m = markers.get(uid);
        if(m != null) {
            m.remove();
            markers.remove(m);
        }
    }

    //@Override
    public void setZoom(float zoom) {
        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
    }



    //@Override
    public void providerChanged(boolean GpsEnabled) {
        if(isGps != GpsEnabled) {
            isGps = GpsEnabled;
            final MenuItem b = (MenuItem) findViewById(R.id.action_switch_location_source);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(isGps) {
                        b.setIcon(R.drawable.gps);
                    } else {
                        b.setIcon(R.drawable.wifi);
                    }
                }
            });
        }
    }

    private void animateMarker(final Marker marker, LatLng destLatLng) {
        double[] startValues = new double[]{marker.getPosition().latitude, marker.getPosition().longitude};
        double[] endValues = new double[]{destLatLng.latitude, destLatLng.longitude};
        ValueAnimator latLngAnimator = ValueAnimator.ofObject(new DoubleArrayEvaluator(), startValues, endValues);
        latLngAnimator.setDuration(600);
        latLngAnimator.setInterpolator(new DecelerateInterpolator());
        latLngAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                double[] animatedValue = (double[]) animation.getAnimatedValue();
                marker.setPosition(new LatLng(animatedValue[0], animatedValue[1]));
            }
        });
        latLngAnimator.start();
    }




}
