package com.scottlindley.mobliezombie;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class UsageService extends Service {
    private static final String TAG = "UsageService";
    BroadcastReceiver mScreenReceiver;
    DBHelper mHelper;
    long mTimeOn, mTimeOff, mTimeDiff;
    int mRunningTime, mNumTimesChecked;

    @Override
    public void onCreate() {
        super.onCreate();
        mHelper = DBHelper.getInstance(getApplicationContext());
        mRunningTime = 0;
        mTimeOn = System.currentTimeMillis();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: "); 
        Thread serviceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mScreenReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat format = new SimpleDateFormat("MMMM d, yyyy");
                        String day = format.format(calendar.getTime());
                        int rowsAffected = 0;
                        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                            Log.d(TAG, "onReceive: SCREEN ON");
                            mTimeOn = System.currentTimeMillis();
                            mNumTimesChecked++;
                            rowsAffected = mHelper.updateChecks(day, mNumTimesChecked);
                        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
                            Log.d(TAG, "onReceive: SCREEN OFF");
                            mTimeOff = System.currentTimeMillis();
                            Log.d(TAG, "onReceive: time on: "+mTimeOn);
                            Log.d(TAG, "onReceive: time off: "+mTimeOff);
                            mTimeDiff = mTimeOff - mTimeOn;
                            Log.d(TAG, "onReceive: time diff: "+mTimeDiff);
                            mRunningTime = (int)(long)(mRunningTime + mTimeDiff)/1000;
                            Log.d(TAG, "onReceive: running time: "+mRunningTime);
                            rowsAffected = mHelper.updateSeconds(day, (mRunningTime));
                        }
                        if (rowsAffected == 0){
                            mHelper.addNewDateEntry(day, mRunningTime, mNumTimesChecked);
                        }
                    }
                };
                registerReceiver(mScreenReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
                registerReceiver(mScreenReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
            }
        });
        serviceThread.start();

        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mScreenReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
