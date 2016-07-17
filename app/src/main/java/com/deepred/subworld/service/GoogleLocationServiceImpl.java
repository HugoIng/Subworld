package com.deepred.subworld.service;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.deepred.subworld.ICommon;
import com.deepred.subworld.R;
import com.deepred.subworld.engine.GameService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 *
 */
public class GoogleLocationServiceImpl extends LocationService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private final static String TAG = "SW SERVICE GoogleLocSrv";

    // Google Location API
    private GoogleApiClient mGoogleApiClient;

    private boolean connected; // Coneccted to GoogleAPI
    private boolean started; // Has started requesting locations?
    private boolean gpsMode; // Real gpsMode in use

    private Location lastLocation;

    //TODO
    private long lastModeChangeTimestamp;
    private boolean hasRetrievedLocationsSinceLastModeChange;

    @Override
    public void onCreate() {
        super.onCreate();
        started = false;
        connected = false;
        gpsMode = false;
        hasRetrievedLocationsSinceLastModeChange = false;

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();

        lastLocation = getLastLocation();
        if (lastLocation != null)
            sendLastLocation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnect();
    }


    public void disconnect() {
        stopLocationUpdates();
        mGoogleApiClient.disconnect();
    }

    public boolean isStarted() {
        return started;
    }



    //Android Location methods
    private void requestLocationUpdates(boolean useGps) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.e(TAG, getString(R.string.no_permissions));
            return;
        }

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(ICommon.LOCATION_GOOGLE_TIME_INTERVAL);
        mLocationRequest.setFastestInterval(ICommon.LOCATION_MIN_TIME_BW_UPDATES);

        int priority;
        if(useGps) {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
            gpsMode = true;
        } else {
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
            gpsMode = false;
        }

        mLocationRequest.setPriority(priority);
        mLocationRequest.setSmallestDisplacement(ICommon.LOCATION_MIN_DISTANCE_CHANGE_FOR_UPDATES);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    private Location getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null; //TODO;
        }

        return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    //Google Api callbacks
    @Override
    public void onConnected(Bundle bundle) {
        connected = true;
        //onLocImplConnected();
        if (isConnectedBBDD) {
            //updateBackgroundStatus();
            evaluateGps();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, getString(R.string.loc_apis_conn_err));
    }

    @Override
    public void onLocationChanged(final Location loc) {

        lastLocation = loc;

        sendLastLocation();

        // After 15 seconds, if no location is retrieved, go back to LOW_PRECISSION mode.
        /*Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if (lastLocation.getTime() > System.currentTimeMillis() - ICommon.DISABLE_GPS_IF_NO_LOCATIONS_AFTER)
                    Log.d(TAG, "Gps desabilitado por obsolescencia");
                    switchProvider(false);
            }
        }, ICommon.DISABLE_GPS_IF_NO_LOCATIONS_AFTER);*/
    }


    private void sendLastLocation() {
        Log.d(TAG, "Sending location: " + lastLocation.toString());
        Intent mServiceIntent = new Intent(this, GameService.class);
        mServiceIntent.setData(Uri.parse(ICommon.NEW_LOCATION_FROM_SRV));
        mServiceIntent.putExtra(ICommon.NEW_LOCATION_FROM_SRV, lastLocation);
        startService(mServiceIntent); // Starts the IntentService
    }


    /**
     * Function to show settings alert dialog
     * */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getApplicationContext());

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                getApplicationContext().startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }


    protected void evaluateGps() {

        if (!started) {
            switchProvider(true);
        } else {
            if (handler.isAppInBackground()) {
                // Si estamos en background y el GPS esta activo, lo cambiamos a NETWORK
                if (gpsMode) {
                    switchProvider(false);
                }
            } else {
                if (requiredGpsMode != gpsMode)
                    switchProvider(requiredGpsMode);
            }
        }
    }

    private void switchProvider(boolean useGps) {
        requiredGpsMode = useGps;

        if (!isConnectedBBDD)
            return;

        if (!connected)
            return;

        if (requiredGpsMode == gpsMode && started)
            return;

        Log.d(TAG, "SwitchProvider: use GPS:" + useGps);

        stopLocationUpdates();

        // Si aun no hemos obtenido una localizacion, mantenemos low precission
        boolean activateGPS;
        if (useGps && lastLocation != null && lastLocation.getTime() != 0) {
            activateGPS = true;
        } else {
            activateGPS = false;
        }

        requestLocationUpdates(activateGPS);

        Intent localIntent = new Intent(ICommon.SET_PROVIDER_INFO)
                // Puts the status into the Intent
                .putExtra(ICommon.SET_PROVIDER_INFO, activateGPS);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

        started = true;
    }
}
