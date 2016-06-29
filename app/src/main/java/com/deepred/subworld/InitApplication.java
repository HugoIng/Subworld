package com.deepred.subworld;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.deepred.subworld.engine.GameService;
import com.deepred.subworld.model.User;
import com.deepred.subworld.notifications.BaseNotificationBuilder;
import com.deepred.subworld.service.LocationService;
import com.deepred.subworld.utils.ICallbacks;
import com.deepred.subworld.utils.MyUserManager;
import com.deepred.subworld.views.CharactersSelectionActivity;
import com.deepred.subworld.views.LoginActivity;
import com.deepred.subworld.views.MapActivityImpl;

/**
 * Created by aplicaty on 25/02/16.
 */
public class InitApplication extends Activity implements ICallbacks.IUserCallbacks {
    private static final String TAG = "InitAplication";
    private SubworldApplication app;
    private Bundle extraFromNotification = null;
    private SharedPreferences prefs;
    private String email;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (SubworldApplication)getApplication();
        ApplicationHolder.setApp(app);

        if (app.getLocationService() == null) {
            startService(new Intent(InitApplication.this, LocationService.class));
        } else {
            Log.d("InitApplication", "Service already started");
        }

        Intent i = getIntent();
        if (i != null) {
            // We are coming from a notification
            Class clazz = (Class) i.getSerializableExtra(BaseNotificationBuilder.SUBWORLD_NOTIF_CLASS);
            if (clazz != null) {
                extraFromNotification = i.getExtras();
            }
        }

        // Restore preferences
        email = app.getPreference(ICommon.EMAIL);
        password = app.getPreference(ICommon.PASSWORD);

        // Look for credentials
        if (email != null && password != null) {

            setContentView(R.layout.activity_init);

            MyUserManager.getInstance().register4UserNotifications(this);

            // Request login or register with the background service
            Intent mServiceIntent = new Intent(this, GameService.class);
            mServiceIntent.setData(Uri.parse(ICommon.LOGIN_REGISTER));
            mServiceIntent.putExtra(ICommon.EMAIL, email);
            mServiceIntent.putExtra(ICommon.PASSWORD, password);
            mServiceIntent.putExtra(ICommon.SCREEN_CONTEXT, getLocalClassName());
            mServiceIntent.putExtra(ICommon.RESULT_RECEIVER, new ResultReceiver(null) {
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    super.onReceiveResult(resultCode, resultData);
                    if (resultCode == RESULT_OK) {
                        Log.d(TAG, "Login OK!");
                    } else {
                        Log.d(TAG, "Login failed!");
                        launchLogin();
                    }
                }
            });
            startService(mServiceIntent); // Starts the IntentService

        } else {
            launchLogin();
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (email != null && password != null) {
            MyUserManager.getInstance().unregister4UserNotifications(this);
        }
    }
    

    private void launchLogin() {
        Intent outI = new Intent(this, LoginActivity.class);
        if (extraFromNotification != null) {
            outI.putExtras(extraFromNotification);
        }
        outI.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(outI);

        extraFromNotification = null;
    }

    @Override
    public void onUserChange(User user) {
        if(user == null || user.getName().isEmpty() || user.getChrType() == ICommon.CHRS_NOT_SET) {
            Intent intent = new Intent(getApplicationContext(), CharactersSelectionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
            this.startActivity(intent);
        }
        //else entrar
        else {
            Intent intent = new Intent(getApplicationContext(), MapActivityImpl.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            this.startActivity(intent);
        }
    }
}
