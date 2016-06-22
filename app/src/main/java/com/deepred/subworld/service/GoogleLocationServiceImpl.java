package com.deepred.subworld.service;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.deepred.subworld.ICommon;
import com.deepred.subworld.R;
import com.deepred.subworld.engine.GameManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;

/**
 * Created by aplicaty on 22/06/16.
 */
public class GoogleLocationServiceImpl implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private final static String TAG = "GoogleLocationSrvImpl";

    // Google Location API
    private GoogleApiClient mGoogleApiClient;

    private ServiceBoot srv;
    private GameManager gm;

    private boolean connected;
    private boolean started;
    private boolean gpsMode;

    public GoogleLocationServiceImpl(ServiceBoot service) {

        srv = service;
        started = false;
        connected = false;
        gpsMode = false;
        gm = GameManager.getInstance();

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(srv)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();

        gm.setLastLocationIfNull(getLastLocation());

    }

    public void disconnect() {
        stopLocationUpdates();
        mGoogleApiClient.disconnect();
    }

    public void switchProvider(boolean useGps) {
        if(!connected)
            return;

        if (srv.isRequiredGpsMode() == gpsMode && started)
            return;

        stopLocationUpdates();

        // Si aun no hemos obtenido una localizacion, mantenemos low precission
        if (useGps && gm.getLastLocationDate() != null) {
            requestLocationUpdates(true);
        } else {
            requestLocationUpdates(false);
        }

        started = true;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isGpsMode() {
        return gpsMode;
    }

    //Android Location methods
    private void requestLocationUpdates(boolean useGps) {
        if (ActivityCompat.checkSelfPermission(srv, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(srv, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.e(TAG, srv.getString(R.string.no_permissions));
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

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        final PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        /*result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                final LocationSettingsStates = locationSettingsResult.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        //...
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    OuterClass.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        //...
                        break;
                }
            }
        });*/

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    public void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    public Location getLastLocation() {
        if (ActivityCompat.checkSelfPermission(srv, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(srv, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        srv.onLocImplConnected();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, srv.getString(R.string.loc_apis_conn_err));
    }

    @Override
    public void onLocationChanged(Location loc) {
        gm.locationChange(loc);

        if(srv.isRequiredGpsMode() != gpsMode) {
            switchProvider(srv.isRequiredGpsMode());
        }

        // After 15 seconds, if no location is retrieved, go back to LOW_PRECISSION mode.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if(gm.getLastLocation().getTime() > System.currentTimeMillis() - ICommon.DISABLE_GPS_IF_NO_LOCATIONS_AFTER)
                    switchProvider(false);
            }
        }, ICommon.DISABLE_GPS_IF_NO_LOCATIONS_AFTER);
    }

    /**
     * Function to show settings alert dialog
     * */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(srv.getApplicationContext());

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
                srv.getApplicationContext().startActivity(intent);
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
}
