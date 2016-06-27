package com.deepred.subworld;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.deepred.subworld.service.LocationService;
import com.deepred.subworld.service.StatusReceiver;
import com.deepred.subworld.utils.OnBackPressed;

import java.util.Properties;

/**
 * Created by aplicaty on 25/02/16.
 */
public class SubworldApplication extends MultiDexApplication {
    private static final String TAG = "SubworldApplication";
    private SharedPreferences preferences;

    private LocationService locationService;
    //private StatusReceiver handler;
    private Properties properties = new Properties();
    private OnBackPressed onBackPressed;


    public SubworldApplication() {
        super();

        /*handler = new StatusReceiver();

        // Register receiver that handles screen on and screen off logic and background state
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(ICommon.BACKGROUND_STATUS);
        filter.addAction(ICommon.BBDD_CONNECTED);
        registerReceiver(handler, filter);
        registerActivityLifecycleCallbacks(handler);
        registerComponentCallbacks(handler);*/

        /*if (locationService == null) {
            startService(new Intent(this, LocationService.class));
        } else {
            Log.d(TAG, "Service already started");

        }*/
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        /*unregisterActivityLifecycleCallbacks(handler);
        unregisterComponentCallbacks(handler);
        unregisterReceiver(handler);*/
    }

    public SharedPreferences getPreferences() {
        return getSharedPreferences(ICommon.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void savePreference(String key, String value) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getPreference(String key) {
        return getPreferences().getString(key, null);
    }

    public LocationService getLocationService() {
        return locationService;
    }

    public void setLocationService(LocationService locationService, StatusReceiver handler) {
        this.locationService = locationService;
        handler.setService(locationService);
        registerActivityLifecycleCallbacks(handler);
        registerComponentCallbacks(handler);
    }


    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                handleException(thread, e);
            }
        });
    }

    public void handleException(Thread thread, Throwable e) {
        e.printStackTrace();
        Log.e("EXCEPTION", "AplicatyApplication, handleException: " + e.getMessage(), e);
        if (e.getCause() != null) {
            logCause(e.getCause());
            e.printStackTrace();
        }

        System.exit(1); // kill off the crashed app

        // Restart the app
        /*Intent intent = new Intent(this, ErrorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);*/
    }

    public void logCause(Throwable t) {
        Log.e("EXCEPTION", "AplicatyApplication, logCause: " + t.getMessage(), t);
        if (t.getCause() != null) {
            logCause(t.getCause());
            t.getCause().printStackTrace();
        }
    }


    public OnBackPressed getOnBackPressed() {
        return onBackPressed;
    }

    public void setOnBackPressed(OnBackPressed onBackPressed) {
        this.onBackPressed = onBackPressed;
    }

}
