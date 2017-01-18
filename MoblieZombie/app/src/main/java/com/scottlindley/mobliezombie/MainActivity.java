package com.scottlindley.mobliezombie;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.support.design.widget.TabLayout;


import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    public static final String FRAGMENT_REFRESH_INTENT = "Fragment Refresh Intent";
    public static final String ACTIVITY_TO_FRAGMENT_REFRESH = "act to frag refresh";
    public static final String REQUEST_REFRESH_INTENT = "Request Refresh";
    private SwipeRefreshLayout mRefreshLayout;
    private TextView mClockView, mChecksView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        DBAssetHelper dbAssetSetUp = new DBAssetHelper(MainActivity.this);
        dbAssetSetUp.getReadableDatabase();

        setUpRefreshReceiver();

        startTrackingService();

        setUpViews();

        sendRefreshRequest();
    }

    private void startTrackingService(){
        final Intent intent = new Intent(MainActivity.this, UsageService.class);
        Thread serviceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                startService(intent);
            }
        });
        serviceThread.start();
    }

    private void setUpViews(){
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        mClockView = (TextView)findViewById(R.id.day_clock);
        mChecksView = (TextView)findViewById(R.id.day_unlocks);

        mClockView.setText(getResources().getString(R.string.day_clock));
        mChecksView.setText(getResources().getString(R.string.day_checks));

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                sendRefreshRequest();
            }
        });
        mRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));

        ViewPager viewPager = (ViewPager)findViewById(R.id.view_pager);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                sendRefreshRequest();
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void updateViews(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("MMMM d, yyyy");
        String day = format.format(calendar.getTime());
        DayData dayData = DBHelper.getInstance(MainActivity.this).getDaysData(day);

        if (dayData != null) {
            int totalSeconds = dayData.getSeconds();
            int hours = totalSeconds / 3600;
            int minutes = (totalSeconds % 3600) / 60;
            int seconds = totalSeconds % 60;

            String timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            String clockText =
                    getResources().getString(R.string.day_clock) + " " + timeFormatted;
            String checksText =
                    getResources().getString(R.string.day_checks) + " " + dayData.getChecks();
            mRefreshLayout.setRefreshing(false);
            mClockView.setText(clockText);
            mChecksView.setText(checksText);
        }
    }

    private void setUpRefreshReceiver(){
        BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateViews();
            }
        };

        BroadcastReceiver fragmentRefreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                sendRefreshRequest();
            }
        };

        registerReceiver(refreshReceiver, new IntentFilter(UsageService.ANSWER_REFRESH_INTENT));
        registerReceiver(fragmentRefreshReceiver, new IntentFilter(FRAGMENT_REFRESH_INTENT));
    }

    private void sendRefreshRequest(){
        Intent serviceIntent = new Intent(REQUEST_REFRESH_INTENT);
        sendBroadcast(serviceIntent);
        Intent fragmentIntent = new Intent(ACTIVITY_TO_FRAGMENT_REFRESH);
        sendBroadcast(fragmentIntent);
    }

    @Override
    protected void onResume() {
        sendRefreshRequest();
        super.onResume();
    }
}
