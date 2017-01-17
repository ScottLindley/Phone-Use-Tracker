package com.scottlindley.mobliezombie;

import android.app.Notification;
import android.app.PendingIntent;
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
    public static final int NOTIFICATION_ID = 88;
    BroadcastReceiver mScreenReceiver, mRefreshReceiver;
    DBHelper mHelper;
    long mTimeOn, mTimeOff;
    int mRunningTime, mNumTimesChecked, mTimeDiff;

    @Override
    public void onCreate() {
        super.onCreate();
        mHelper = DBHelper.getInstance(getApplicationContext());
        mRunningTime = 0;
        mTimeOn = System.currentTimeMillis();
        Log.d(TAG, "onStartCommand: ");
        Intent notificationIntent = new Intent(this, UsageService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new Notification.Builder(this)
                .setContentTitle("Mobile Zombie")
                .setContentText("Tracking Usage")
                .setSmallIcon(R.drawable.crown)
                .setContentIntent(pendingIntent)
                .setTicker("HI")
                .build();
        startForeground(NOTIFICATION_ID, notification);
        mScreenReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat format = new SimpleDateFormat("MMMM d, yyyy");
                String day = format.format(calendar.getTime());
                int rowsAffected = 0;
                if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                    Log.d(TAG, "onReceive: SCREEN ON");
                    long lastTimeOn = mTimeOn;
                    mTimeOn = System.currentTimeMillis();
                    if (lastTimeOn != mTimeOn) {
                        mNumTimesChecked++;
                        rowsAffected = mHelper.updateChecks(day, mNumTimesChecked);
                        if (rowsAffected == 0) {
                            mHelper.addNewDateEntry(day, mRunningTime, mNumTimesChecked);
                        }
                    }
                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
                    long lastTimeOff = mTimeOff;
                    Log.d(TAG, "onReceive: SCREEN OFF");
                    mTimeOff = System.currentTimeMillis();
                    if (lastTimeOff != mTimeOff) {
                        Log.d(TAG, "onReceive: time on: " + mTimeOn);
                        Log.d(TAG, "onReceive: time off: " + mTimeOff);
                        mTimeDiff = (int) (long) (mTimeOff - mTimeOn) / 1000;
                        Log.d(TAG, "onReceive: time diff: " + mTimeDiff);
                        mRunningTime = mRunningTime + mTimeDiff;
                        Log.d(TAG, "onReceive: running time: " + mRunningTime);
                        rowsAffected = mHelper.updateSeconds(day, (mRunningTime));
                        if (rowsAffected == 0) {
                            mHelper.addNewDateEntry(day, mRunningTime, mNumTimesChecked);
                        }
                    }
                }
            }
        };

        mRefreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenReceiver, filter);

        registerReceiver(mRefreshReceiver, new IntentFilter(MainActivity.REQUEST_REFRESH_INTENT));


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

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
