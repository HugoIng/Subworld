package com.deepred.subworld.engine;

import android.content.Intent;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.deepred.subworld.ICommon;
import com.deepred.subworld.model.Rival;
import com.firebase.geofire.GeoLocation;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;

//import com.google.android.gms.maps.model.LatLng;

/**
 * Created by aplicaty on 29/02/16.
 */
public class UsersViewRangeManager {

    private final static String TAG = "RangeManager";
    private static Object lock = new Object();
    private static volatile UsersViewRangeManager instance = null;
    private GameService gm;
    private RivalsMap rivals;
    private GeoLocation myLocation;
    private double actualRange = 200; // Range in meters
    private float zoom;
    private boolean applyRangeReduction;
    private List<String> previousUsersToBeRemoved;
    private UsersViewRangeManager() {
        rivals = new RivalsMap();
        previousUsersToBeRemoved = new ArrayList<String>();
        applyRangeReduction = false;
        zoom = rangeToZoomLevel();
    }

    public static UsersViewRangeManager getInstance() {
        UsersViewRangeManager localInstance = instance;
        if(localInstance == null) {
            synchronized (lock) {
                localInstance = instance;
                if(localInstance == null) {
                    instance = localInstance = new UsersViewRangeManager();
                }
            }
        }
        return localInstance;
    }

    public void setServiceContext(GameService ctx) {
        gm = ctx;
    }

    public float getZoom() { return zoom; }

    public void update(Location loc) {
        DataManager.getInstance().queryLocations(loc, actualRange / 1000);
    }

    public void queryCompleted() {
        performRangeCheck();
    }

    private void performRangeCheck() {
        Log.d(TAG, "Performing range checks");
        double previousRange = actualRange;

        if(applyRangeReduction) {
            for(String uid : previousUsersToBeRemoved) {
                remove(uid);
            }
            previousUsersToBeRemoved.clear();
            applyRangeReduction = false;
        }

        // When there are not enough rivals
        if(rivals.countVisibleUsersInRange() < ICommon.MIN_USERS_IN_RANGE) {
            // Widen the range area
            if(actualRange < ICommon.MAX_RANGE) {
                Log.d(TAG, "Performing range checks: widen the area");
                if(actualRange < ICommon.RANGE_VARIATION) {
                    actualRange = ICommon.RANGE_VARIATION;
                } else {
                    actualRange += ICommon.RANGE_VARIATION;
                }
            } else {
                // Switch from GPS a Network
                Log.d(TAG, "Performing range checks: switch to low precission (gps off)");
                //LocationService serv = ApplicationHolder.getApp().getLocationService();
                /*if(serv != null)
                    serv.switchProvider(false);
                else
                    LocationService.setProvider(false);*/
                Intent localIntent = new Intent(ICommon.BACKGROUND_STATUS)
                                // Puts the status into the Intent
                                .putExtra(ICommon.BACKGROUND_STATUS, false);
                // Broadcasts the Intent to receivers in this app.
                LocalBroadcastManager.getInstance(gm).sendBroadcast(localIntent);

                gm.changeBackgroundState(true);
            }
        }

        if(rivals.countVisibleUsersInRange() > ICommon.MAX_USERS_IN_RANGE) {
            // Reduce the area and focus onto less rivals
            Log.d(TAG, "Performing range checks: reduce the area");
            if(actualRange > ICommon.RANGE_VARIATION) {
                actualRange -= ICommon.RANGE_VARIATION;
            } else {
                if(actualRange > ICommon.MIN_RANGE) {
                    actualRange -= ICommon.SMALL_RANGE_VARIATION;
                }
            }
        }

        if(actualRange != previousRange) {
            applyRangeReduction = (actualRange < previousRange);
            if(applyRangeReduction) {
                // Make a copy of the rivals ids in actual map
                previousUsersToBeRemoved.addAll(rivals.getKeys());
            }

            Location loc = new Location("?");
            loc.setLatitude(myLocation.latitude);
            loc.setLongitude(myLocation.longitude);
            update(loc);
            // Adjust zoom
            setZoom();
        }
    }

    public void add(final String uid, final GeoLocation g, boolean isMe) {
        Log.d(TAG, "Add " + uid + " (isMe:" + isMe + ")");
        if(isMe) {
            // actualiza
            myLocation = g;

            // Comprueba la visibilidad de los otros usuarios al cambiar mi posicion
            refreshUsersVisibility();

            gm.updateMyLocation();
        } else {
            if(applyRangeReduction) {
                // if this uid was on the previous range and will still be on the map
                // remove from previousUsersToBeRemoved list, otherwise it will be
                // erased when query completes
                int index = previousUsersToBeRemoved.indexOf(uid);
                if(index > -1) {
                    previousUsersToBeRemoved.remove(index);
                }
            }

            // Check visibilidad
            gm.checkVisibility(uid, new LatLng(g.latitude, g.longitude), new IVisibilityCompletionListener() {
                @Override
                public void onCompleted(boolean isVisible) {
                    boolean wasVisibleBefore = rivals.isVisible(uid);

                    // Actualiza rivals
                    rivals.put(uid, g, isVisible);

                    if (isVisible) {
                        // Lo pinto
                        gm.updateRivalLocation(uid, new LatLng(g.latitude, g.longitude));
                    } else if (wasVisibleBefore) {
                        // Ya no se ve, lo borro.
                        gm.removeRivalLocation(uid);
                    }
                }
            });
        }
    }

    public void remove(String uid) {
        Rival u = rivals.remove(uid);
        if(u.isVisible()) {
            gm.removeRivalLocation(uid);
        }
    }

    public void setZoom() {
        zoom = rangeToZoomLevel();
        gm.setZoom(zoom);
    }

    private double zoomLevelToRadius(double zoomLevel) {
        // Approximation to fit circle into view
        return 16384000/Math.pow(2, zoomLevel);
    }

    private float rangeToZoomLevel() {
        Double d  = Math.log(16384000.0f/actualRange)/Math.log(2.0f);
        return d.floatValue();
    }

    private void refreshUsersVisibility() {
        Log.d(TAG, "refreshUsersVisibility");
        for(String uid: rivals.getKeys()) {
            checkUserVisibility(uid);
        }
    }

    private void checkUserVisibility(final String uid) {
        Log.d(TAG, "checkUserVisibility: " + uid);
        final Rival u = rivals.get(uid);
        GeoLocation g = u.getLocation();
        final LatLng loc = new LatLng(g.latitude, g.longitude);

        gm.checkVisibility(uid, loc, new IVisibilityCompletionListener() {

            @Override
            public void onCompleted(boolean isVisible) {
                boolean wasVisibleBefore = u.isVisible();
                u.setVisible(isVisible);

                if (isVisible != wasVisibleBefore) {
                    if (isVisible) {
                        // Lo pinto
                        gm.updateMyLocation();
                    } else if (wasVisibleBefore) {
                        // Ya no se ve, lo borro.
                        gm.removeRivalLocation(uid);
                    }
                }
            }
        });
    }

    public interface IVisibilityCompletionListener {
        void onCompleted(boolean isVisible);
    }
}
