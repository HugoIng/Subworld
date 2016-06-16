package com.deepred.subworld.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import com.deepred.subworld.ApplicationHolder;
import com.deepred.subworld.SubworldApplication;

/**
 * Created by aplicaty on 25/02/16.
 */
public class BaseNotificationBuilder implements INotificationBuilder {

    public static final String SOAPBOX_NOTIF_CLASS = "NOTIFICATION_CLASS";
    public static final String SOAPBOX_NOTIF_ID = "NOTIFICATION_ID";
    protected static final String SOAPBOX_NOTIF_DELETED_EVENT = "SOAPBOX_NOTIF_DELETED_EVENT";
    //protected ArrayList<INewUpdatedContent> noticationStack;
    protected android.app.NotificationManager notificationManager;
    protected Context context;

    protected BaseNotificationBuilder(){
        //noticationStack = new ArrayList<>();
        context = ApplicationHolder.getApp();
        notificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    //protected abstract Class getSourceClass();

    protected PendingIntent getDeleteIntent(){
        Intent intent = new Intent(context, NotificationDeleteReceiver.class);
        //intent.putExtra(SOAPBOX_NOTIF_CLASS, getSourceClass());
        intent.setAction(SOAPBOX_NOTIF_DELETED_EVENT);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public void clearNotifications(){
        /*synchronized (noticationStack) {
            noticationStack.clear();
        }*/
    }

    protected IBaseBuilder getBaseBuilder() {
        final PendingIntent pi = getDeleteIntent();
        final NotificationCompat.Builder b = new NotificationCompat.Builder(context)
                .setDeleteIntent(getDeleteIntent());
        return new IBaseBuilder(){
            @Override
            public NotificationCompat.Builder getBuilder(){
                return b;
            }
            @Override
            public PendingIntent getPendingIntent(){
                return pi;
            }
        };
    }

    public void vibrate() {
        SubworldApplication app = ApplicationHolder.getApp();
        Vibrator v = (Vibrator) app.getSystemService(app.getApplicationContext().VIBRATOR_SERVICE);
        if (v.hasVibrator()) {
            // The following numbers represent millisecond lengths
            int dot = 200;      // Length of a Morse Code "dot" in milliseconds
            int dash = 500;     // Length of a Morse Code "dash" in milliseconds
            int short_gap = 200;    // Length of Gap Between dots/dashes
            int medium_gap = 500;   // Length of Gap Between Letters
            int long_gap = 1000;    // Length of Gap Between Words
            long[] pattern = {
                    0,  // Start immediately
                    dot, short_gap, dot,
            };
            v.vibrate(pattern, -1);
        }
    }

    public void ring(String path) {
        try {
            Uri notification = Uri.parse(path);
            SubworldApplication app = ApplicationHolder.getApp();
            Ringtone r = RingtoneManager.getRingtone(app.getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
