package com.scottlindley.mobliezombie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final String REQUEST_REFRESH_INTENT = "Request Refresh";
    private DBHelper mHelper;
    private Button mRefreshButton;
    private TextView mClockView, mChecksView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DBAssetHelper dbAssetSetUp = new DBAssetHelper(MainActivity.this);
        dbAssetSetUp.getReadableDatabase();

        final DBHelper mHelper = DBHelper.getInstance(this);


        final Intent intent = new Intent(MainActivity.this, UsageService.class);
        Thread serviceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                startService(intent);
            }
        });
        serviceThread.start();

        mRefreshButton = (Button)findViewById(R.id.refresh_button);
        mClockView = (TextView)findViewById(R.id.day_clock);
        mChecksView = (TextView)findViewById(R.id.day_unlocks);

        mClockView.setText(getResources().getString(R.string.day_clock));
        mChecksView.setText(getResources().getString(R.string.day_checks));

        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat format = new SimpleDateFormat("MMMM d, yyyy");
                String day = format.format(calendar.getTime());
                DayData dayData = mHelper.getDaysData(day);

                String lastRefreshDay = mClockView.getText().toString();

                if (dayData != null){
                    int totalSeconds = dayData.getSeconds();
                    int hours = totalSeconds / 3600;
                    int minutes = (totalSeconds % 3600) / 60;
                    int seconds = totalSeconds % 60;

                    String timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                    String clockText =
                            getResources().getString(R.string.day_clock) + " " + timeFormatted;
                    String checksText =
                            getResources().getString(R.string.day_checks) + " " + dayData.getChecks();
                    mClockView.setText(clockText);
                    mChecksView.setText(checksText);

                    if (lastRefreshDay.equals(clockText)){
                        Intent intent = new Intent(REQUEST_REFRESH_INTENT);
                        sendBroadcast(intent);
                    }
                } else {
                    mClockView.setText("NO DATA");
                    mChecksView.setText("NO DATA");
                }
            }
        });
    }

    @Override
    protected void onResume() {
        mRefreshButton.performClick();
        super.onResume();
    }
}
