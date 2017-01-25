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
import java.util.List;

import static android.content.ContentValues.TAG;

public class UsageService extends Service {
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
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        //TODO: Edit and add updates to this notification
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Mobile Zombie")
                .setContentText("Tracking Usage")
                .setSmallIcon(R.drawable.zombie)
                .setContentIntent(pendingIntent)
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
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("MMMM d, yyyy");
        String day = format.format(calendar.getTime());
        mTimeOn = System.currentTimeMillis();
        mNumTimesChecked++;
        mHelper.updateChecks(day, mNumTimesChecked);
    }

    private void handleScreenOff(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("MMMM d, yyyy");
        String today = format.format(calendar.getTime());
        mTimeOff = System.currentTimeMillis();
        mTimeDiff = (int) (long) (mTimeOff - mTimeOn) / 1000;
        mRunningTime = mRunningTime + mTimeDiff;
        List<String> days = mHelper.getAllDays();
        if (!days.isEmpty() && days.contains(today)){
            //if it's not a new day
            mHelper.updateSeconds(today, mRunningTime);
        } else {
            //It's a new day
            long secondsSinceMidnight = findSecondsPastMidnight();
            if (mTimeDiff > secondsSinceMidnight){
                //User was using his/her phone at midnight
                mRunningTime = (int) (mRunningTime - secondsSinceMidnight);
                int yesterdayUpdated = mHelper.updateYesterday(mRunningTime, mNumTimesChecked);
                mRunningTime = (int) secondsSinceMidnight;
                mNumTimesChecked = 0;
                mHelper.addNewDateEntry(today, mRunningTime, mNumTimesChecked);
                if (yesterdayUpdated == 0){
                    mHelper.updateSeconds(today, 0);
                    mHelper.updateChecks(today, 0);
                }
            } else {
                //User starting using phone in the new day
                mNumTimesChecked = 1;
                mTimeDiff = (int) (long) (mTimeOff - mTimeOn) / 1000;
                mRunningTime = mTimeDiff;
                mHelper.addNewDateEntry(today, mRunningTime, mNumTimesChecked);
            }
        }
        mTimeOn = mTimeOff;
    }

    private long findSecondsPastMidnight(){
        Calendar c = Calendar.getInstance();
        long now = c.getTimeInMillis();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long passed = now - c.getTimeInMillis();
        return passed / 1000;
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
