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
    public static final String ANSWER_REFRESH_INTENT = "Answer Refresh";
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

        //TODO: Edit and add updates to this notification
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Mobile Zombie")
                .setContentText("Tracking Usage")
                .setSmallIcon(R.drawable.zombie)
                .setContentIntent(pendingIntent)
                .setTicker("HI")
                .build();
        startForeground(NOTIFICATION_ID, notification);

        mScreenReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                    handleScreenOn();
                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    handleScreenOff();
                }
            }
        };

        mRefreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleScreenOff();
                Intent refreshDoneIntent = new Intent(ANSWER_REFRESH_INTENT);
                sendBroadcast(refreshDoneIntent);
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenReceiver, filter);

        registerReceiver(mRefreshReceiver, new IntentFilter(MainActivity.REQUEST_REFRESH_INTENT));
    }

    private void handleScreenOn(){
        Log.d(TAG, "onReceive: SCREEN ON");
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("MMMM d, yyyy");
        String day = format.format(calendar.getTime());
        mTimeOn = System.currentTimeMillis();
        mNumTimesChecked++;
        int rowsAffected = mHelper.updateChecks(day, mNumTimesChecked);
        if (rowsAffected == 0) {
            mHelper.addNewDateEntry(day, mRunningTime, mNumTimesChecked);
        }
    }

    private void handleScreenOff(){
        Log.d(TAG, "onReceive: SCREEN OFF");
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("MMMM d, yyyy");
        String day = format.format(calendar.getTime());
        mTimeOff = System.currentTimeMillis();
        mTimeDiff = (int) (long) (mTimeOff - mTimeOn) / 1000;
        mRunningTime = mRunningTime + mTimeDiff;
        int rowsAffected = mHelper.updateSeconds(day, mRunningTime);
        if (rowsAffected == 0) {
            mHelper.addNewDateEntry(day, mRunningTime, mNumTimesChecked);
        }
        mTimeOn = mTimeOff;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {return START_NOT_STICKY;}

    @Override
    public void onDestroy() {
        handleScreenOff();
        unregisterReceiver(mScreenReceiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {return null;}
}
