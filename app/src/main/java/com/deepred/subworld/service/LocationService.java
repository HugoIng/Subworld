package com.deepred.subworld.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.deepred.subworld.ApplicationHolder;
import com.deepred.subworld.ICommon;


/**
 * Created by Hugo.
 *
 * Sticky service managing location providers and app status
 * Strategy used: it starts in LOW_PRECISSION mode (wifi/network),
 * when the app goes to the foreground it switches to HIGH_PRECISSION.
 * HIGH_PRECISSION is maintained as long as the app is foreground and
 * there are users within range; otherwise it goes back to LOW_PRECISSION
 */
public abstract class LocationService extends Service {

    private final static String TAG = "LocationService";

    protected static boolean requiredGpsMode = false;
    protected static boolean isConnectedBBDD = false; // Flag indicating BBDD conection is OK

    protected StatusReceiver handler;

    /*
    * Static method to be used when the app starts before the service does
     */
    public static void setBBDDConnected() {
        isConnectedBBDD = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        handler = new StatusReceiver();

        // Register receiver that handles screen on and screen off logic and background state
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(ICommon.SET_GPS_STATUS);
        registerReceiver(handler, filter);
        registerComponentCallbacks(handler);

        Log.d(TAG, "Service Created");

        ApplicationHolder.getApp().setLocationService(this, handler);

        evaluateGps();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        return android.app.Service.START_STICKY;
    }

    public abstract void evaluateGps();

    protected abstract boolean isStarted();

    /*
    * Called when connection to firebase succeeds.
    * If the location service is prepared but not started, start collecting locations
    * Init with app's status back/foreground
     */
    public void onBBDDConnected() {
        isConnectedBBDD = true;
        if (!isStarted()) {
            evaluateGps();
        }
    }

    protected boolean isRequiredGpsMode() {
        return requiredGpsMode;
    }

    /*
    * Static method to be used when the app starts before the service does
     */
    public static void setRequiredGpsMode(boolean useGPS) {
        requiredGpsMode = useGPS;
    }

    public class LocalBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

}
