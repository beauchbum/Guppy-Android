package com.example.androidhive;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class Broadcasting_Service extends Service {
    public Broadcasting_Service() {

    }

    void handleCommand(Intent intent) {

    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        handleCommand(intent);
        return START_STICKY;
    }



    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
