package com.deepred.subworld.service;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.deepred.subworld.ICommon;
import com.deepred.subworld.engine.GameService;

/**
 *
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class StatusReceiver extends BroadcastReceiver implements Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {
    private static final String TAG = "SW SERVICE StatusRcvr  ";

    private boolean isAppInBackgroundState; // Real app status Foreground / Background
    private boolean screenOff; // Device screen is on/off

    private LocationService srv;

    public StatusReceiver() {
        isAppInBackgroundState = true;
        screenOff = false;
    }

    /*
    Public methods
     */
    public void setService(LocationService _srv) {
        srv = _srv;
    }

    protected boolean isAppInBackground() {
        return (isAppInBackgroundState || screenOff);
    }


    /*
    Private methods and overrides
     */

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        switch (action) {
            case ICommon.SET_GPS_STATUS:
                Log.v(TAG, "onReceive: SET_GPS_STATUS");
                LocationService.setRequiredGpsMode(intent.getBooleanExtra(ICommon.SET_GPS_STATUS, false));
                break;
            case Intent.ACTION_SCREEN_OFF:
                Log.v(TAG, "onReceive: SCREEN_OFF");
                screenOff = true;
                break;
            case Intent.ACTION_SCREEN_ON:
                Log.v(TAG, "onReceive: SCREEN_ON");
                screenOff = false;
                if (!isAppInBackgroundState) {
                    LocationService.setRequiredGpsMode(true);
                }
                break;
        }

        reportStatusChanges();
    }


    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (isAppInBackgroundState) {
            setIsAppInBackground(false);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    @Override
    public void onTrimMemory(int level) {
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            setIsAppInBackground(true);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

    }

    @Override
    public void onLowMemory() {

    }

    private void setIsAppInBackground(boolean state) {
        if (isAppInBackgroundState != state) {
            isAppInBackgroundState = state;

            boolean finalStatus = !isAppInBackgroundState && !screenOff;
            LocationService.setRequiredGpsMode(finalStatus);

            reportStatusChanges();

            Intent mServiceIntent = new Intent(srv, GameService.class);
            mServiceIntent.setData(Uri.parse(ICommon.SET_BACKGROUND_STATUS));
            mServiceIntent.putExtra(ICommon.SET_BACKGROUND_STATUS, !finalStatus);
            srv.startService(mServiceIntent); // Starts the IntentService
        }
    }

    private void reportStatusChanges() {
        srv.evaluateGps();
    }
}
