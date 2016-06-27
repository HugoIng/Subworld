package com.deepred.subworld.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.deepred.subworld.ApplicationHolder;
import com.deepred.subworld.ApplicationLifecycleHandler;
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

    private final static String TAG = "Service";

    protected static boolean requiredGpsMode = false;
    protected static boolean isConnectedBBDD = false; // Flag indicating BBDD conection is OK

    private StatusReceiver handler;

    /*
    * Static method to be used when the app starts before the service does
     */
    public static void setProvider(boolean useGPS) {
        Log.d(TAG, "Static switchProvider: use GPS:" + useGPS);
        requiredGpsMode = useGPS;
    }

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
        filter.addAction(ICommon.BACKGROUND_STATUS);
        filter.addAction(ICommon.BBDD_CONNECTED);
        registerReceiver(handler, filter);
        //registerActivityLifecycleCallbacks(handler);
        registerComponentCallbacks(handler);

        //handler.setService(this);

        Log.d(TAG, "Service Created");

        ApplicationHolder.getApp().setLocationService(this, handler);

        getBackgroundStatus();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        return android.app.Service.START_STICKY;
    }

    /*
    * Request locations and set mode
     */
    public abstract void switchProvider(boolean useGPS);

    protected abstract boolean isStarted();

    /*
    * Called when connection to firebase succeeds.
    * If the location service is prepared but not started, start collecting locations
    * Init with app's status back/foreground
     */
    public void onBBDDConnected() {
        isConnectedBBDD = true;
        if (!isStarted())
            getBackgroundStatus();
    }

    /*
    * Called when Google Location APIs connect successfully.
    * Init with app's status back/foreground
     */
    protected void onLocImplConnected () {
        if(isConnectedBBDD)
            getBackgroundStatus();
    }

    protected boolean isRequiredGpsMode() {
        return requiredGpsMode;
    }

    private void getBackgroundStatus() {
        /*Intent mServiceIntent = new Intent(this, GameService.class);
        mServiceIntent.setData(Uri.parse(ICommon.GET_BACKGROUND_STATUS));
        startService(mServiceIntent); // Starts the IntentService*/

        boolean status = ApplicationLifecycleHandler.getInstance().isAppInBackground();
        switchProvider(status);
    }

    public class LocalBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

}
