package com.deepred.subworld;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.deepred.subworld.engine.GameManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;


/**
 * Created by Hugo.
 *
 * Manages location providers and app status
 */
public class ServiceBoot extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private SubworldApplication app;
    private GameManager gm;
    private final static String TAG = "Service";

    private static int LOCATION_LOW_PRECISSION = 0;
    private static int LOCATION_HIGH_PRECISSION = 1;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    // Google Location API
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private static boolean requiredGpsMode = false;
    private boolean effectiveGpsMode = false;

    private boolean isConnected; // Flag indicating BBDD connection. Prevents location requests before the app is logued into the BBDD.
    private boolean requestLocationsStarted; // Flag indicating locations are being requested already

    // TODO
    // Estrategias de localizacion: iniciar en modo LOW_PRECISSION.
    // Cuando la app pasa a primer plano solicitar un punto del gps(HIGH_PRECISSION)
    // y evaluar si hay usuarios en el rango de vision, en cuyo caso mantenerlo asi.
    // Sino hay gente en el rango volver a LOW
    // Cuando la app pasa a segundo plano o cuando no hay usuarios en el rango de vision, volver a LOW

    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //icConnected = false;
        app = (SubworldApplication) getApplication();
        ApplicationHolder.setAplicatyApplication(app);
        app.setServiceBoot(this);
        gm = GameManager.getInstance();

        isConnected = false;
        requestLocationsStarted = false;

        // Register receiver that handles screen on and screen off logic
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        BroadcastReceiver mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();
        
        gm.setLastLocationIfNull(getLastLocation(LOCATION_LOW_PRECISSION));

        Log.d(TAG, "ServiceBoot initiated");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        return android.app.Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        app.setServiceBoot(null);
        stopLocationUpdates();
        mGoogleApiClient.disconnect();
    }

    public static void setProvider(boolean useGPS) {
        Log.d(TAG, "SwitchProvider: use GPS:" + useGPS);
        if (useGPS == requiredGpsMode)
            return;

        requiredGpsMode = useGPS;
    }

    public void switchProvider(boolean useGPS) {
        Log.d(TAG, "switchProvider: use GPS:" + useGPS);

        requiredGpsMode = useGPS;

        if(!isConnected) {
            return;
        }

        if (requiredGpsMode == effectiveGpsMode)
            return;

        stopLocationUpdates();

        // Si aun no hemos obtenido una localizacion, mantenemos low precission
        if (requiredGpsMode && gm.getLastLocationDate() != null) {
            requestLocationUpdates(LOCATION_HIGH_PRECISSION);
        } else {
            requestLocationUpdates(LOCATION_LOW_PRECISSION);
        }

        requestLocationsStarted = true;
    }

    public boolean isUsingGPS() {
        return (effectiveGpsMode && gm.getLastLocationDate() != null);
    }

    // BBDD Connection callback

    public void onBBDDConnected() {
        isConnected = true;
        if(!requestLocationsStarted)
            switchProvider(gm.checkBackgroundStatus());
    }


    //Android Location methods
    private void requestLocationUpdates(int precission) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(ICommon.LOCATION_GOOGLE_TIME_INTERVAL);
        mLocationRequest.setFastestInterval(ICommon.LOCATION_MIN_TIME_BW_UPDATES);

        int priority;
        if(precission == LOCATION_HIGH_PRECISSION) {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
            effectiveGpsMode = true;
        } else {
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
            effectiveGpsMode = false;
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

    public Location getLastLocation(int precission) {
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
        switchProvider(gm.checkBackgroundStatus());
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("SERVICE", "Google Loc Apis connection error");
    }

    public class LocalBinder extends Binder {
        public ServiceBoot getService() {
            return ServiceBoot.this;
        }
    }


    @Override
    public void onLocationChanged(Location loc) {
        gm.locationChange(loc);

        if(requiredGpsMode != effectiveGpsMode) {
            switchProvider(requiredGpsMode);
        }

        // Si pasan 15 segundos y no se obtiene localizacion en modo gps, volver a modo red
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

}
