package com.deepred.subworld.engine;

import android.content.Intent;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.deepred.subworld.ICommon;
import com.deepred.subworld.model.MapElement;
import com.deepred.subworld.model.MapRival;
import com.firebase.geofire.GeoLocation;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by aplicaty on 29/02/16.
 */
public class ViewRangeManager {

    private final static String TAG = "SW ENGINE RangeManager ";
    private static Object lock = new Object();
    private static volatile ViewRangeManager instance = null;
    private GameService gm;
    private MapElements elems;
    private double actualRange = 200; // Range in meters
    private float zoom;
    private boolean applyRangeReduction;
    private List<String> previousUsersToBeRemoved;

    private ViewRangeManager() {
        elems = new MapElements();
        previousUsersToBeRemoved = new ArrayList<>();
        applyRangeReduction = false;
        zoom = rangeToZoomLevel();
    }

    public static ViewRangeManager getInstance() {
        ViewRangeManager localInstance = instance;
        if(localInstance == null) {
            synchronized (lock) {
                localInstance = instance;
                if(localInstance == null) {
                    instance = localInstance = new ViewRangeManager();
                }
            }
        }
        return localInstance;
    }

    public void setContext(GameService ctx) {
        gm = ctx;
    }


    /*
    * Request an update to the DDBB with the new user position
     */
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

        // When there are not enough elems
        if (elems.countVisibleUsersInRange() < ICommon.MIN_USERS_IN_RANGE) {
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

                Intent localIntent = new Intent(ICommon.SET_GPS_STATUS)
                                // Puts the status into the Intent
                        .putExtra(ICommon.SET_GPS_STATUS, false);
                // Broadcasts the Intent to receivers in this app.
                LocalBroadcastManager.getInstance(gm).sendBroadcast(localIntent);

                gm.changeBackgroundState(true);
            }
        }

        if (elems.countVisibleUsersInRange() > ICommon.MAX_USERS_IN_RANGE) {
            // Reduce the area and focus onto less elems
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
                // Make a copy of the elems ids in actual map
                previousUsersToBeRemoved.addAll(elems.getKeys());
            }

            // Adjust zoom
            setZoom();
        }
    }

    public void add(final String uid, final int type, final GeoLocation g, boolean isMe) {
        Log.d(TAG, "Add " + uid + " (isMe:" + isMe + ")");
        if(isMe) {
            // Comprueba la visibilidad de los otros usuarios al cambiar mi posicion
            refreshElementsVisibility();
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

            elems.put(uid, g, type, false);
            checkElementVisibility(uid);
        }
    }

    private void refreshElementsVisibility() {
        Log.d(TAG, "refreshElementsVisibility");
        for (String uid : elems.getKeys()) {
            checkElementVisibility(uid);
        }
    }

    private void checkElementVisibility(final String uid) {
        Log.d(TAG, "checkElementVisibility: " + uid);
        gm.checkVisibility(uid);
    }

    public void remove(String uid) {
        MapElement u = elems.remove(uid);
        if(u.isVisible()) {
            gm.removeMapElementLocation(uid);
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

    public MapElement getMapElement(String _uid) {
        return elems.get(_uid);
    }

    public float getZoom() {
        return zoom;
    }
}
