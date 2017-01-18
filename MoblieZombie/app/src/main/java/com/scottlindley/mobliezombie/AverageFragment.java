package com.scottlindley.mobliezombie;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

public class AverageFragment extends Fragment {
    private DBHelper mHelper;
    private TextView mAverageTimeView, mAverageChecksView, mPercentageView, mProjectedUseView;
    private SwipeRefreshLayout mRefreshLayout;
    private ProgressBar mProgressBar;


    public AverageFragment() {
        // Required empty public constructor
    }

    public static AverageFragment newInstance() {
        AverageFragment fragment = new AverageFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_average, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mHelper = DBHelper.getInstance(view.getContext());

        setUpViews(view);

        refreshFragmentData();
        super.onViewCreated(view, savedInstanceState);
    }

    private void setUpViews(View view){
        mAverageTimeView = (TextView)view.findViewById(R.id.average_time);
        mAverageChecksView = (TextView)view.findViewById(R.id.average_checks);
        mPercentageView = (TextView)view.findViewById(R.id.day_percentage_view);
        mProjectedUseView = (TextView)view.findViewById(R.id.projected_use);
        mProgressBar = (ProgressBar)view.findViewById(R.id.day_ratio_bar);


        mRefreshLayout =
                (SwipeRefreshLayout)view.findViewById(R.id.average_swipe_refresh);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFragmentData();
                Intent intent = new Intent(MainActivity.FRAGMENT_REFRESH_INTENT);
                getActivity().sendBroadcast(intent);
                mRefreshLayout.setRefreshing(false);
            }
        });
        mRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));

        setUpReceiver();
    }

    private void setUpReceiver(){
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refreshFragmentData();
            }
        };
        getActivity().registerReceiver(receiver, new IntentFilter(MainActivity.ACTIVITY_TO_FRAGMENT_REFRESH));
    }

    private void refreshFragmentData(){
        List<DayData> data = mHelper.getAllData();
        if (!data.isEmpty()) {
            int averageTime = 0;
            int averageChecks = 0;
            for (DayData day : data) {
                averageTime = averageTime + day.getSeconds();
                averageChecks = averageChecks + day.getChecks();
            }
            averageTime = averageTime / data.size();
            averageChecks = averageChecks / data.size();

            int totalSeconds = averageTime;
            int hours = totalSeconds / 3600;
            int minutes = (totalSeconds % 3600) / 60;
            int seconds = totalSeconds % 60;

            String timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);

            String projectedString = createProjectedUseString(averageTime);

            mAverageTimeView.setText("Average Time: " + timeFormatted);
            mAverageChecksView.setText("Average Checks: " + averageChecks);
            mPercentageView.setText((averageTime * 100 / mProgressBar.getMax()) + "% of your day");
            mProjectedUseView.setText(projectedString);
            mProgressBar.setProgress(averageTime);
        }
    }

    private String createProjectedUseString(int averageTime){
        int projectedTime = averageTime * 365;

        StringBuilder builder = new StringBuilder();
        builder.append("Yearly Projection: ");
        if (projectedTime < 60){
            //under a minute per year
            builder.append(projectedTime + " second");
            if (projectedTime != 1) {
                builder.append("s");
            }
        } else if (projectedTime < 3600 && projectedTime >= 60){
            //under an hour per year
            int minutes = projectedTime/60;
            builder.append(minutes + " minute");
            if (minutes != 1){
                builder.append("s");
            }
            if (projectedTime%60 > 0) {
                builder.append(" and " + projectedTime % 60 + " second");
                if (projectedTime % 60 != 1) {
                    builder.append("s");
                }
            }
        } else if (projectedTime < 86400 && projectedTime >= 3600){
            //under a day per year
            int hours = projectedTime/3600;
            int minutes = (projectedTime%3600)/60;
            builder.append(hours + " hour");
            if (hours != 1){
                builder.append("s");
            }
            if (minutes > 0) {
                builder.append(" and " + minutes + " minute");
                if (minutes != 1) {
                    builder.append("s");
                }
            }
        } else if (projectedTime < 604800 && projectedTime >= 86400){
            //under a week per year
            int days = projectedTime/86400;
            int hours = (projectedTime%86400)/3600;
            builder.append(days + " day");
            if (days != 1){
                builder.append("s");
            }
            if (hours > 0) {
                builder.append(" and " + hours + " hour");
                if (hours != 1) {
                    builder.append("s");
                }
            }
        } else if (projectedTime <268000 && projectedTime >= 604800){
            //under a month per year
            int weeks = projectedTime/604800;
            int days = (projectedTime%604800)/86400;
            builder.append(weeks + " week");
            if (weeks != 1){
                builder.append("s");
            }
            if (days > 0) {
                builder.append(" and " + days + " day");
                if (days != 1) {
                    builder.append("s");
                }
            }
        } else {
            //over a month per year
            int months = projectedTime/2628000;
            int days = (projectedTime%2628000)/86400;
            builder.append(months + " month");
            if (months != 1){
                builder.append("s");
            }
            if (days > 0) {
                builder.append(" and " + days + " day");
                if (days != 1) {
                    builder.append("s");
                }
            }
        }

        return builder.toString();
    }

    @Override
    public void onResume() {
        refreshFragmentData();
        super.onResume();
    }
}
