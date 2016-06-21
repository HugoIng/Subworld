package com.deepred.subworld.engine;

import android.location.Location;
import android.util.Log;

import com.deepred.subworld.ApplicationHolder;
import com.deepred.subworld.ICommon;
import com.deepred.subworld.ServiceBoot;
import com.firebase.geofire.GeoLocation;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;

//import com.google.android.gms.maps.model.LatLng;

/**
 * Created by aplicaty on 29/02/16.
 */
public class UsersViewRangeManager {

    public interface IVisibilityCompletionListener {
        void onCompleted(boolean isVisible);
    }

    private static Object lock = new Object();
    private static volatile UsersViewRangeManager instance = null;
    //private GameManager gm;

    //private HashMap<String, GeoLocation> users;
    private UsersMap users;
    private String myUid;
    private GeoLocation myLocation;

    private final static String TAG = "RangeManager";
    private double actualRange = 200; // Range in meters
    private float zoom;
    private boolean applyRangeReduction;
    private List<String> previousUsersToBeRemoved;

    private UsersViewRangeManager() {
        //users = new HashMap<String, GeoLocation>();
        users = new UsersMap();
        //gm = GameManager.getInstance();
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

        // Si no hay suficientes puntos
        if(users.countVisibleUsersInRange() < ICommon.MIN_USERS_IN_RANGE) {
            // Widen the range area
            if(actualRange < ICommon.MAX_RANGE) {
                Log.d(TAG, "Performing range checks: widen the area");
                if(actualRange < ICommon.RANGE_VARIATION) {
                    actualRange = ICommon.RANGE_VARIATION;
                } else {
                    actualRange += ICommon.RANGE_VARIATION;
                }
            } else {
                // Cambiar de GPS a Red
                Log.d(TAG, "Performing range checks: switch to low precission (gps off)");
                ServiceBoot serv = ApplicationHolder.getApp().getServiceBoot();
                if(serv != null)
                    serv.switchProvider(false);
                else
                    ServiceBoot.setProvider(false);
                GameManager.getInstance().changeBackgroundState(true);
            }
        }

        if(users.countVisibleUsersInRange() > ICommon.MAX_USERS_IN_RANGE) {
            // Reduce the area and focus onto less users
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
                // Make a copy of the users ids in actual map
                previousUsersToBeRemoved.addAll(users.getKeys());
            }
            update(GameManager.getInstance().getLastLocation());
            // Adjust zoom
            setZoom();
        }
    }

    public void add(final String uid, final GeoLocation g, boolean isMe) {
        Log.d(TAG, "Add " + uid + " (isMe:" + isMe + ")");
        if(isMe) {
            // actualiza
            myUid = uid;
            myLocation = g;

            // Comprueba la visibilidad de los otros usuarios al cambiar mi posicion
            refreshUsersVisibility();

            GameManager.getInstance().updateLocation(uid, new LatLng(g.latitude, g.longitude), true);
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
            GameManager.getInstance().checkVisibility(uid, new LatLng(g.latitude, g.longitude), new IVisibilityCompletionListener() {
                @Override
                public void onCompleted(boolean isVisible) {
                    boolean wasVisibleBefore = users.isVisible(uid);

                    // Actualiza users
                    users.put(uid, g, isVisible);

                    if (isVisible) {
                        // Lo pinto
                        GameManager.getInstance().updateLocation(uid, new LatLng(g.latitude, g.longitude), false);
                    } else if (wasVisibleBefore) {
                        // Ya no se ve, lo borro.
                        GameManager.getInstance().removeLocation(uid);
                    }
                }
            });
        }
    }

    public void remove(String uid) {
        UsersMap.UserInRange u = users.remove(uid);
        if(u.isVisible()) {
            GameManager.getInstance().removeLocation(uid);
        }
    }

    public void setZoom() {
        zoom = rangeToZoomLevel();
        GameManager.getInstance().setZoom(zoom);
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
        for(String uid:users.getKeys()) {
            checkUserVisibility(uid);
        }
    }

    private void checkUserVisibility(final String uid) {
        Log.d(TAG, "checkUserVisibility: " + uid);
        final UsersMap.UserInRange u = users.get(uid);
        GeoLocation g = u.getLocation();
        final LatLng loc = new LatLng(g.latitude, g.longitude);

        GameManager.getInstance().checkVisibility(uid, loc, new IVisibilityCompletionListener() {

            @Override
            public void onCompleted(boolean isVisible) {
                boolean wasVisibleBefore = u.isVisible();
                u.setVisible(isVisible);

                if (isVisible != wasVisibleBefore) {
                    if (isVisible) {
                        // Lo pinto
                        GameManager.getInstance().updateLocation(uid, loc, false);
                    } else if (wasVisibleBefore) {
                        // Ya no se ve, lo borro.
                        GameManager.getInstance().removeLocation(uid);
                    }
                }
            }
        });
    }
}
