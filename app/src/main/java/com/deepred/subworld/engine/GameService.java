package com.deepred.subworld.engine;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.deepred.subworld.ApplicationHolder;
import com.deepred.subworld.ICommon;
import com.deepred.subworld.model.Treasure;
import com.deepred.subworld.model.User;
import com.deepred.subworld.utils.ICallbacks;
import com.deepred.subworld.utils.IViewRangeListener;
import com.deepred.subworld.utils.MyUserManager;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by aplicaty on 25/02/16.
 */
public class GameService extends IntentService implements IViewRangeListener {

    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private static String TAG = "GameService";

    // User location
    private Location lastLocation;
    private boolean mapActivityIsResumed;

    private UsersViewRangeManager viewRange;

    // Pending locations, etc.
    private boolean hasMyLocationPending = false;
    private boolean hasLocationsPending = false;
    private Map<String, LatLng> locsPending = new HashMap<>();
    private boolean hasRemovesPending = false;
    private ArrayList<String> removesPending = new ArrayList<>();
    private boolean hasZoomPending = false;
    private float zoomPending;
    private boolean hasProvPending = false;
    private boolean provPending;

    public GameService() {
        super(TAG);
        lastLocation = null;
        mapActivityIsResumed = false;
        viewRange = UsersViewRangeManager.getInstance();
        viewRange.setServiceContext(this);
    }

    @Override
    protected void onHandleIntent(final Intent workIntent) {
        // Gets data from the incoming Intent
        String dataString = workIntent.getDataString();

        switch (dataString) {
            case ICommon.NEW_LOCATION_FROM_SRV: {
            /*
            * A new location is provided by GoogleLocationServiceImpl
             */
                Bundle bundle = workIntent.getExtras();
                if (bundle == null) {
                    return;
                }
                Location location = bundle.getParcelable(ICommon.NEW_LOCATION_FROM_SRV);
                if (location == null) {
                    return;
                }

                // Filter old, unaccurate locations
                if (isBetterLocation(location)) {
                    lastLocation = location;
                    // Deal with new location: DDBB query to update the rivals that are within are view range
                    viewRange.update(lastLocation);
                }

                break;
            }
            case ICommon.MAP_ACTIVITY_RESUMED:
            /*
            * Map activity just resumed
             */
                mapActivityIsResumed = true;

                if (hasMyLocationPending) {
                    // Broadcast my location
                    broadcastLocation(lastLocation);
                    hasMyLocationPending = false;
                }

                if (hasLocationsPending) {
                    for (String uid : locsPending.keySet()) {
                        // Broadcast rivals locations
                        broadcastRivalLocation(uid, locsPending.get(uid));
                        locsPending.remove(uid);
                    }
                    hasLocationsPending = false;
                }

                if (hasRemovesPending) {
                    for (String uid : removesPending) {
                        // Remove rivals from map
                        broadcastRemoveRival(uid);
                        removesPending.remove(uid);
                    }
                    hasRemovesPending = false;
                }

                if (hasZoomPending) {
                    // Broadcast zoom change
                    broadcastZoom(zoomPending);
                    hasZoomPending = false;
                }

                if (hasProvPending) {
                    // Broadcast provider change
                    broadcastProvider(provPending);
                    hasProvPending = false;
                }

                break;
            case ICommon.MAP_ACTIVITY_PAUSED:
            /*
            * Map activity just paused
             */
                mapActivityIsResumed = false;

                break;
            case ICommon.SET_BACKGROUND_STATUS: {
            /*
            *
             */
                Bundle bundle = workIntent.getExtras();
                if (bundle == null) {
                    return;
                }
                boolean status = bundle.getBoolean(ICommon.SET_BACKGROUND_STATUS);
                changeBackgroundState(status);
                break;
            }
            case ICommon.LOGIN_REGISTER:
            /*
            * Login or register
             */
                String email = workIntent.getStringExtra(ICommon.EMAIL);
                String password = workIntent.getStringExtra(ICommon.PASSWORD);
                final String screen_context = workIntent.getStringExtra(ICommon.SCREEN_CONTEXT);

                // Login with credentials
                DataManager.getInstance().loginOrRegister(email, password, new ICallbacks.ILoginCallbacks() {

                    @Override
                    public void onLoginOk(boolean wait4User) {
                        Log.v(TAG, "Requesting login on firebase");

                        Intent localIntent = new Intent(ICommon.BBDD_CONNECTED);
                        // Broadcasts the Intent to receivers in this app.
                        LocalBroadcastManager.getInstance(GameService.this).sendBroadcast(localIntent);

                        DataManager.getInstance().getUser();

                        // Notify the screen to update interface
                        ResultReceiver rec = workIntent.getParcelableExtra(ICommon.RESULT_RECEIVER);
                        //Bundle b= new Bundle();
                        //b.putString("ServiceTag","aziz");
                        rec.send(Activity.RESULT_OK, /*b*/ null);
                    }

                    @Override
                    public void onLoginError() {
                        // From LoginActivity: notify the screen to update interface
                        ResultReceiver rec = workIntent.getParcelableExtra(ICommon.RESULT_RECEIVER);
                        //Bundle b= new Bundle();
                        //b.putString("ServiceTag","aziz");
                        rec.send(Activity.RESULT_CANCELED, /*b*/ null);
                    }
                });
                break;
            case ICommon.CHECK_NAME:
                final String name = workIntent.getStringExtra(ICommon.NAME);
                final int chr_selected = workIntent.getIntExtra(ICommon.CHR_TYPE, ICommon.CHRS_NOT_SET);

                DataManager.getInstance().checkName(name, new ICallbacks.INameCheckCallbacks() {
                    @Override
                    public void onValidUsername() {
                        DataManager.getInstance().storeUsername(name, new ICallbacks.INameStoringCallbacks() {
                            @Override
                            public void onStored(boolean ok) {
                                if (ok) {
                                    User u = MyUserManager.getInstance().getUser();
                                    String uid = DataManager.getInstance().getUid();
                                    u.setUid(uid);
                                    u.setName(name);
                                    u.setChrType(chr_selected);
                                    SharedPreferences prefs = ApplicationHolder.getApp().getPreferences();
                                    u.setEmail(prefs.getString(ICommon.EMAIL, null));
                                    addDefaultTreasure(u);
                                    DataManager.getInstance().saveUser(u, new ICallbacks.IUserInitialStoreCallbacks() {
                                        @Override
                                        public void onUserCreationError() {
                                            ResultReceiver rec = workIntent.getParcelableExtra(ICommon.RESULT_RECEIVER);
                                            Bundle b = new Bundle();
                                            b.putString(ICommon.MOTIVE, "Error storing user. Try again later.");
                                            rec.send(Activity.RESULT_CANCELED, b);
                                        }

                                        @Override
                                        public void onUserCreationSuccess() {
                                            ResultReceiver rec = workIntent.getParcelableExtra(ICommon.RESULT_RECEIVER);
                                            rec.send(Activity.RESULT_OK, null);
                                        }
                                    });

                                } else {
                                    ResultReceiver rec = workIntent.getParcelableExtra(ICommon.RESULT_RECEIVER);
                                    Bundle b = new Bundle();
                                    b.putString(ICommon.MOTIVE, "Error storing user name. Try again later.");
                                    rec.send(Activity.RESULT_CANCELED, b);
                                }
                            }
                        });
                    }

                    @Override
                    public void onNameAlreadyExists() {
                        ResultReceiver rec = workIntent.getParcelableExtra(ICommon.RESULT_RECEIVER);
                        Bundle b = new Bundle();
                        b.putString(ICommon.MOTIVE, "Name already exists");
                        rec.send(Activity.RESULT_CANCELED, b);
                    }
                });

                break;
        }
    }

    private void addDefaultTreasure(User user) {
        //Treasure
        String uid = user.getUid();
        Treasure t = new Treasure(uid);
        String treasureId = uid + "_" + t.getCreated().getTime();
        user.getBackpack().put(treasureId, t);
    }


    @Override
    public void updateMyLocation() {
        if (mapActivityIsResumed) {
            // Broadcast my location
            broadcastLocation(lastLocation);
        } else {
            hasMyLocationPending = true;
        }
    }

    @Override
    public void updateRivalLocation(String uid, LatLng latLng) {
        if (mapActivityIsResumed) {
            // Broadcast rival
            broadcastRivalLocation(uid, latLng);
        } else {
            locsPending.put(uid, latLng);
            hasLocationsPending = true;
        }
    }

    @Override
    public void removeRivalLocation(String uid) {
        if (mapActivityIsResumed) {
            // Broadcast marker id to be erased from map
            broadcastRemoveRival(uid);
        } else {
            removesPending.add(uid);
            hasRemovesPending = true;
        }
    }

    @Override
    public void setZoom(float zoom) {
        if (mapActivityIsResumed) {
            // Broadcast zoom change
            broadcastZoom(zoom);
        } else {
            zoomPending = zoom;
            hasZoomPending = true;
        }
    }

    /*

     */
    private void updateProvider(boolean status) {
        if (mapActivityIsResumed) {
            // Broadcast provider status
            broadcastProvider(status);
        } else {
            provPending = status;
            hasProvPending = true;
        }
    }

    /*
    * Helper methods to send the location to the screen
     */
    private void broadcastLocation(Location loc) {
        Intent localIntent =
                new Intent(ICommon.MY_LOCATION)
                        // Puts the status into the Intent
                        .putExtra(ICommon.MY_LOCATION, loc);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private void broadcastRivalLocation(String uid, LatLng latLng) {
        Intent localIntent =
                new Intent(ICommon.RIVAL_LOCATION)
                        // Puts the status into the Intent
                        .putExtra(ICommon.RIVAL_LOCATION, latLng)
                        .putExtra(ICommon.UID, uid);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private void broadcastRemoveRival(String uid) {
        Intent localIntent =
                new Intent(ICommon.REMOVE_RIVAL_LOCATION)
                        // Puts the status into the Intent
                        .putExtra(ICommon.UID, uid);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private void broadcastZoom(float zoom) {
        Intent localIntent =
                new Intent(ICommon.SET_ZOOM)
                        // Puts the status into the Intent
                        .putExtra(ICommon.SET_ZOOM, zoom);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private void broadcastProvider(boolean prov) {
        Intent localIntent =
                new Intent(ICommon.SET_PROVIDER_INFO)
                        // Puts the status into the Intent
                        .putExtra(ICommon.SET_PROVIDER_INFO, prov);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }


    /**
     * Determines whether one Location reading is better than the current Location fix
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



    public void changeBackgroundState(boolean backgroudStatus) {
        Log.d(TAG, "changeBackgroundState:" + backgroudStatus);
        // App in fore/background status
        // Si pasamos a background bajamos la resolucion del GPS
        // Si pasamos a foreground subimos la precision del GPS
        updateProvider(!backgroudStatus);
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
        DataManager.getInstance().getUser(uid, new ICallbacks.IUserCallbacks() {
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
        if (tot < 0)
            tot = 0;
        int range = calculateDistanceRange(distance);
        return range > -1 && ICommon.distanceTable[tot][range];
    }

    private int calculateDistanceRange(float distance) {
        Float d = distance;
        int dist = d.intValue();
        for (int i = 0; i < ICommon.distanceRanges.length; i++) {
            if(dist <= ICommon.distanceRanges[i]) {
                return i;
            }
        }
        return -1;
    }
}
