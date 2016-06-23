package com.deepred.subworld.engine;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.deepred.subworld.ApplicationHolder;
import com.deepred.subworld.ApplicationLifecycleHandler;
import com.deepred.subworld.ICommon;
import com.deepred.subworld.SubworldApplication;
import com.deepred.subworld.model.User;
import com.deepred.subworld.utils.IMapUpdatesListener;
import com.deepred.subworld.utils.IUserCallbacks;
import com.deepred.subworld.utils.IViewRangeListener;
import com.deepred.subworld.utils.MyUserManager;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//import com.google.android.gms.maps.model.LatLng;

/**
 * Created by aplicaty on 25/02/16.
 */
public class GameManager implements IViewRangeListener {

    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private static Object lock = new Object();
    private static volatile GameManager instance = null;
    private String TAG = "GameManager";
    // User location
    private Location lastLocation;
    private Long lastLocationDate;
    private IMapUpdatesListener actListener;
    private SubworldApplication app;
    private UsersViewRangeManager viewRange;
    //Messages pendientes
    private boolean hasMyLocationPending = false;
    private boolean hasLocationsPending = false;
    private Map<String, LatLng> locsPending = new HashMap<String, LatLng>();
    private boolean hasRemovesPending = false;
    private ArrayList<String> removesPending = new ArrayList<String>();
    private boolean hasZoomPending = false;
    private float zoomPending;
    private boolean hasProvPending = false;
    private boolean provPending;

    private GameManager() {
        lastLocation = null;
        lastLocationDate = null;
        viewRange = UsersViewRangeManager.getInstance();
    }

    public static GameManager getInstance() {
        GameManager localInstance = instance;
        if (localInstance == null) {
            synchronized (lock) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new GameManager();
                }
            }
        }
        return localInstance;
    }

    public void locationChange(Location location) {
        Log.d(TAG, "Location received");
        if(isBetterLocation(location)) {
            lastLocation = location;
            lastLocationDate = System.currentTimeMillis();

            Context ctx = ApplicationHolder.getApp().getBaseContext();
            SharedPreferences prefs = ctx.getSharedPreferences(ICommon.PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(ICommon.LAST_LOCATION_LATITUDE, Double.toString(location.getLatitude()));
            editor.putString(ICommon.LAST_LOCATION_LONGITUDE, Double.toString(location.getLongitude()));
            editor.putString(ICommon.LAST_LOCATION_PROVIDER,location.getProvider());
            editor.commit();

            // Save my location and eval people in range
            viewRange.update(location);
        }
    }

    public Location getLastLocation() {
        // Devolvemos la localizacion almacenada si no es nula
        if(lastLocation == null) {
            Context ctx = ApplicationHolder.getApp().getBaseContext();
            SharedPreferences prefs = ctx.getSharedPreferences(ICommon.PREFS_NAME, Context.MODE_PRIVATE);
            String lastLocationLat = prefs.getString(ICommon.LAST_LOCATION_LATITUDE, null);
            String lastLocationLong = prefs.getString(ICommon.LAST_LOCATION_LONGITUDE, null);
            String lastLocationProv = prefs.getString(ICommon.LAST_LOCATION_PROVIDER, null);
            if(lastLocationLat != null && lastLocationLong != null) {
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
        }
        return lastLocation;
    }

    public Long getLastLocationDate() {
        return lastLocationDate;
    }

    public void setLastLocationIfNull(Location loc) {
        if(lastLocation == null)
            lastLocation = loc;
    }

    public void registerListener(IMapUpdatesListener act) {
        actListener = act;

        if(hasMyLocationPending) {
            actListener.updateMyMarker(lastLocation);
            hasMyLocationPending = false;
        }

        if(hasLocationsPending) {
            for(String uid : locsPending.keySet()) {
                actListener.updateMarker(uid, locsPending.get(uid));
                locsPending.remove(uid);
            }
            hasLocationsPending = false;
        }

        if (hasRemovesPending) {
            for(String uid : removesPending) {
                actListener.removeMarker(uid);
                removesPending.remove(uid);
            }
            hasRemovesPending = false;
        }

        if(hasZoomPending) {
            actListener.setZoom(zoomPending);
            hasZoomPending = false;
        }

        if(hasProvPending) {
            actListener.providerChanged(provPending);
            hasProvPending = false;
        }
    }

    public void unregisterListener() {
        actListener = null;
    }

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     */
    protected boolean isBetterLocation(Location location) {
        if (lastLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - lastLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - lastLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                lastLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    @Override
    public void updateLocation(String uid, LatLng latLng, boolean isMe) {
        if (actListener != null) {
            if(isMe) {
                actListener.updateMyMarker(lastLocation);
            } else {
                actListener.updateMarker(uid, latLng);
            }
        } else {
            if(isMe) {
                hasMyLocationPending = true;
            } else {
                locsPending.put(uid, latLng);
                hasLocationsPending = true;
            }
        }
    }


    @Override
    public void removeLocation(String uid) {
        if(actListener != null)
            actListener.removeMarker(uid);
        else {
            removesPending.add(uid);
            hasRemovesPending = true;
        }
    }

    @Override
    public void setZoom(float zoom) {
        if(actListener != null)
            actListener.setZoom(zoom);
        else {
            zoomPending = zoom;
            hasZoomPending = true;
        }
    }

    public void changeBackgroundState(boolean backgroudStatus) {
        Log.d(TAG, "changeBackgroundState:" + backgroudStatus);
        // App in fore/background status
        // Si pasamos a background bajamos la resolucion del GPS
        // Si pasamos a foreground subimos la precision del GPS
        updateProvider(!backgroudStatus);
    }

    public boolean checkBackgroundStatus() {
        Log.d(TAG, "checkBackgroundStatus");
        boolean backgroudStatus = ApplicationLifecycleHandler.getInstance().isAppInBackground();
        boolean useGps = !backgroudStatus;
        updateProvider(useGps);
        return useGps;
    }

    private void updateProvider(boolean status) {
        if(actListener != null)
            actListener.providerChanged(status);
        else {
            provPending = status;
            hasProvPending = true;
        }
    }

    public void checkVisibility(String uid, LatLng loc, final UsersViewRangeManager.IVisibilityCompletionListener cb) {
        // Aplicar las reglas de visibilidad entre mi usuario y este
        Log.d(TAG, "checkVisibility: " + uid);

        final User myUser = MyUserManager.getInstance().getUser();
        Location otherUserLocation = new Location("?");
        otherUserLocation.setLatitude(loc.getLatitude());
        otherUserLocation.setLongitude(loc.getLongitude());
        final float distance = lastLocation.distanceTo(otherUserLocation);

        // Obtener el usuario de la BBDD
        DataManager.getInstance().getUser(uid, new IUserCallbacks() {
            @Override
            public void onUserChange(User user) {
                boolean isVisible = applyVisibilityRules(myUser, user, distance);
                Log.d(TAG, "checkVisibility returns:" + isVisible);
                cb.onCompleted(isVisible);
            }
        });
    }

    private boolean applyVisibilityRules(User myUser, User otherUser, float distance) {
        boolean ret = false;
        int myWatchingSkill = myUser.getSkills().getWatching().getValue();
        int otherHidingSkill = otherUser.getSkills().getHiding().getValue();
        int tot = myWatchingSkill - otherHidingSkill;
        if(tot < 0)
            tot = 0;
        int range = calculateDistanceRange(distance);
        if (range > -1)
            return ICommon.distanceTable[tot][range];
        else
            return false;
    }

    private int calculateDistanceRange(float distance) {
        Float d = new Float(distance);
        int dist = d.intValue();
        for (int i = 0; i < ICommon.distanceRanges.length; i++) {
            if(dist <= ICommon.distanceRanges[i]) {
                return i;
            }
        }
        return -1;
    }
}
