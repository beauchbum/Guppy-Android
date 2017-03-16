package com.example.androidhive;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;

import static android.R.attr.data;

public class MyIntentService extends GcmListenerService {


        @Override
        public void onMessageReceived(String from, Bundle data) {
            String message = data.getString("message");
            NotificationManager notificationManager= (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle("Test")
                    .setContentText(message);
            notificationManager.notify(1, mBuilder.build());
        }



}
