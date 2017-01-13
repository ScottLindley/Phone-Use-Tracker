package com.scottlindley.mobliezombie;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.IBinder;
import android.view.Display;

import java.util.Timer;
import java.util.TimerTask;

public class UsageService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Thread serviceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                final Handler handler = new Handler();
                Timer timer = new Timer(false);
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                //Do stuff here
                            }
                        });
                    }
                };
                timer.schedule(timerTask, 1000);
            }
        });

        return START_NOT_STICKY;
    }

    private boolean isScreenOn(){
        boolean screenIsOn = false;
        DisplayManager displayManager =
                (DisplayManager)getSystemService(Context.DISPLAY_SERVICE);
        for (Display currentDisplay : displayManager.getDisplays()){
            if (currentDisplay.getState() != Display.STATE_OFF){
                screenIsOn = true;
            }
        }
        return screenIsOn;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
