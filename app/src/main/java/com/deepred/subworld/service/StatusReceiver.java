package com.deepred.subworld.service;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;

import com.deepred.subworld.ICommon;
import com.deepred.subworld.engine.GameService;

/**
 *
 */
public class StatusReceiver extends BroadcastReceiver implements Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {
    private static final String TAG = "StatusReceiver";
    private boolean isAppInBackgroundState; // Real app status Foreground / Background
    private boolean screenOff; // Device screen is on/off
    private LocationService srv;

    public StatusReceiver() {
        isAppInBackgroundState = true;
        screenOff = false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(ICommon.BACKGROUND_STATUS)) {
            boolean status = intent.getBooleanExtra(ICommon.BACKGROUND_STATUS, false);
            if (srv != null)
                srv.switchProvider(status);
            else
                LocationService.setProvider(status);
        } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
            screenOff = true;
            updateScreenStatus(screenOff);
        } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
            screenOff = false;
            updateScreenStatus(screenOff);
        } else if (action.equals(ICommon.BBDD_CONNECTED)) {
            if (srv != null)
                srv.onBBDDConnected();
            else
                LocationService.setBBDDConnected();
        }
    }

    public void setService(LocationService _srv) {
        srv = _srv;
    }

    public boolean isAppInBackground() {
        return (isAppInBackgroundState || screenOff);
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
        isAppInBackgroundState = state;
        //SubworldApplication app = ApplicationHolder.getApp();
        //if (app != null) {
            sendAppInBackgroundStatus(isAppInBackgroundState);
        //}
    }

    private void sendAppInBackgroundStatus(boolean status) {
        Intent mServiceIntent = new Intent(srv, GameService.class);
        mServiceIntent.setData(Uri.parse(ICommon.SET_BACKGROUND_STATUS));
        mServiceIntent.putExtra(ICommon.SET_BACKGROUND_STATUS, status);
        srv.startService(mServiceIntent); // Starts the IntentService

        boolean useGPS = !status;

        if (srv != null)
            srv.switchProvider(useGPS);
        else
            LocationService.setProvider(useGPS);
    }

    public void updateScreenStatus(boolean screenState) {
        screenOff = screenState;
        if (!isAppInBackgroundState) {
            //SubworldApplication app = ApplicationHolder.getApp();
            //if (app != null) {
                sendAppInBackgroundStatus(screenOff);
            //}
        }
    }
}
