package com.deepred.subworld;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.deepred.subworld.engine.DataManager;
import com.deepred.subworld.model.User;
import com.deepred.subworld.notifications.BaseNotificationBuilder;
import com.deepred.subworld.service.ServiceBoot;
import com.deepred.subworld.utils.IUserCallbacks;
import com.deepred.subworld.utils.MyUserManager;
import com.deepred.subworld.views.CharactersSelectionActivity;
import com.deepred.subworld.views.LoginActivity;
import com.deepred.subworld.views.MapboxActivity;

/**
 * Created by aplicaty on 25/02/16.
 */
public class InitApplication extends Activity implements IUserCallbacks {
    private SubworldApplication app;
    private Bundle extraFromNotification = null;
    private SharedPreferences prefs;
    private String email;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (SubworldApplication)getApplication();
        ApplicationHolder.setAplicatyApplication((SubworldApplication) app);

        if (app.getServiceBoot() == null) {
            startService(new Intent(InitApplication.this, ServiceBoot.class));
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

        DataManager.getInstance();

        // Restore preferences
        prefs = getSharedPreferences(ICommon.PREFS_NAME, Context.MODE_PRIVATE);
        email = prefs.getString(ICommon.EMAIL, null);
        password = prefs.getString(ICommon.PASSWORD, null);

        // Look for credentials
        if (email != null && password != null) {

            setContentView(R.layout.activity_init);

            MyUserManager.getInstance().register4UserNotifications(this);

            // Login with credentials
            DataManager.getInstance().loginOrRegister(email, password, new LoginActivity.ILoginCallbacks() {

                @Override
                public void onLoginOk(boolean wait4User) {
                    ServiceBoot serv = ApplicationHolder.getApp().getServiceBoot();
                    if(serv != null)
                        serv.onBBDDConnected();
                    else
                        ServiceBoot.setBBDDConnected();
                    DataManager.getInstance().getUser();
                }

                @Override
                public void onLoginError() {
                    launchLogin();
                }
            });
        } else {
            launchLogin();
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        MyUserManager.getInstance().unregister4UserNotifications(this);
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
            Intent intent = new Intent(getApplicationContext(), MapboxActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            this.startActivity(intent);
        }
    }
}
