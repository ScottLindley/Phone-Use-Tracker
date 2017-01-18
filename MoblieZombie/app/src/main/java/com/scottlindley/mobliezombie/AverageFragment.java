package com.scottlindley.mobliezombie;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class AverageFragment extends Fragment {
    private DBHelper mHelper;
    private TextView mAverageTimeView, mAverageChecksView;
    private SwipeRefreshLayout mRefreshLayout;


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

        mAverageTimeView = (TextView)view.findViewById(R.id.average_time);
        mAverageChecksView = (TextView)view.findViewById(R.id.average_checks);

        mRefreshLayout =
                (SwipeRefreshLayout)view.findViewById(R.id.average_swipe_refresh);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
                mRefreshLayout.setRefreshing(false);
            }
        });
        mRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));

        refreshData();
        super.onViewCreated(view, savedInstanceState);
    }

    private void refreshData(){
        List<DayData> data = mHelper.getAllData();
        int averageTime = 0;
        int averageChecks = 0;
        for (DayData day : data){
            averageTime = averageTime + day.getSeconds();
            averageChecks = averageChecks + day.getChecks();
        }
        averageTime = averageTime/data.size();
        averageChecks = averageChecks/data.size();

        int totalSeconds = averageTime;
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        String timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        mAverageTimeView.setText("Average Time: "+ timeFormatted);
        mAverageChecksView.setText("Average Checks: "+averageChecks);
    }

    @Override
    public void onResume() {
        refreshData();
        super.onResume();
    }
}
