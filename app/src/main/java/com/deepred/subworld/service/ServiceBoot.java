package com.deepred.subworld.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.deepred.subworld.ApplicationHolder;
import com.deepred.subworld.ScreenReceiver;
import com.deepred.subworld.SubworldApplication;
import com.deepred.subworld.engine.GameManager;


/**
 * Created by Hugo.
 *
 * Sticky service managing location providers and app status
 * Strategy used: it starts in LOW_PRECISSION mode (wifi/network),
 * when the app goes to the foreground it switches to HIGH_PRECISSION.
 * HIGH_PRECISSION is maintained as long as the app is foreground and
 * there are users within range; otherwise it goes back to LOW_PRECISSION
 */
public class ServiceBoot extends Service  {

    // Inner accessors and constants
    private final static String TAG = "Service";
    private SubworldApplication app;
    private GameManager gm;
    private GoogleLocationServiceImpl locImpl;

    private static boolean requiredGpsMode = false;
    private static boolean isConnectedBBDD = false; // Flag indicating BBDD conection is OK

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
        app = (SubworldApplication) getApplication();
        ApplicationHolder.setAplicatyApplication(app);
        app.setServiceBoot(this);
        gm = GameManager.getInstance();

        // Register receiver that handles screen on and screen off logic
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        BroadcastReceiver mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);

        locImpl = new GoogleLocationServiceImpl(this);

        Log.d(TAG, "Service Created");

        switchProvider(gm.checkBackgroundStatus());
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
        locImpl.disconnect();
        locImpl = null;
    }


    /*
    * Request locations and set mode
     */
    public void switchProvider(boolean useGPS) {
        Log.d(TAG, "SwitchProvider: use GPS:" + useGPS);
        requiredGpsMode = useGPS;

        if(!isConnectedBBDD)
            return;

        locImpl.switchProvider(requiredGpsMode);
    }
    /*
    * Static method to be used when the app starts before the service does
     */
    public static void setProvider(boolean useGPS) {
        Log.d(TAG, "Static switchProvider: use GPS:" + useGPS);
        requiredGpsMode = useGPS;
    }

    /*public boolean isUsingGPS() {
        return (locImpl.isGpsMode() && gm.getLastLocationDate() != null);
    }*/



    /*
    * Called when connection to firebase succeeds.
    * If the location service is prepared but not started, start collecting locations
    * Init with app's status back/foreground
     */
    public void onBBDDConnected() {
        isConnectedBBDD = true;

        if(!locImpl.isStarted())
            switchProvider(gm.checkBackgroundStatus());
    }
    /*
    * Static method to be used when the app starts before the service does
     */
    public static void setBBDDConnected() {
        isConnectedBBDD = true;
    }

    /*
    * Called when Google Location APIs connect successfully.
    * Init with app's status back/foreground
     */
    protected void onLocImplConnected () {
        if(isConnectedBBDD)
            switchProvider(gm.checkBackgroundStatus());
    }

    protected boolean isRequiredGpsMode() {
        return requiredGpsMode;
    }


    public class LocalBinder extends Binder {
        public ServiceBoot getService() {
            return ServiceBoot.this;
        }
    }

}
