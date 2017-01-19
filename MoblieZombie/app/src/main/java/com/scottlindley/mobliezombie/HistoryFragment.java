package com.scottlindley.mobliezombie;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private RecyclerAdapter mAdapter;
    private SwipeRefreshLayout mRefreshLayout;
    private LineChart mLineChart;
    private TextView mGraphKey;
    private DBHelper mHelper;
    private boolean mRefreshedHistoryFragment;
    private BroadcastReceiver mReceiver;

    public HistoryFragment() {
        // Required empty public constructor
    }

    public static HistoryFragment newInstance() {
        HistoryFragment fragment = new HistoryFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRefreshedHistoryFragment = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mHelper = DBHelper.getInstance(view.getContext());

        mGraphKey = (TextView)view.findViewById(R.id.graph_key);

        mRecyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(
                new LinearLayoutManager(view.getContext()));
        List<DayData> dayDataList = mHelper.getAllData();
        mAdapter = new RecyclerAdapter(dayDataList);
        mRecyclerView.setAdapter(mAdapter);

        mLineChart = (LineChart)view.findViewById(R.id.history_line_chart);
        mLineChart.getLegend().setEnabled(false);
        mLineChart.getAxisRight().setDrawGridLines(false);
        mLineChart.getAxisRight().setDrawLabels(false);
        mLineChart.setDrawGridBackground(false);
        mLineChart.setDescription(null);
        mLineChart.setTouchEnabled(false);

        mRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.recycler_swipe_refresh);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mRefreshedHistoryFragment = true;
                refreshFragmentData();
                mRefreshLayout.setRefreshing(false);
                Intent intent = new Intent(MainActivity.FRAGMENT_REFRESH_INTENT);
                getActivity().sendBroadcast(intent);
            }
        });
        mRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));

        setUpReceiver();

        super.onViewCreated(view, savedInstanceState);
    }

    private void setUpReceiver(){
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refreshFragmentData();
            }
        };
        getActivity().registerReceiver(mReceiver, new IntentFilter(MainActivity.ACTIVITY_TO_FRAGMENT_REFRESH));
    }

    private void refreshFragmentData(){
        List<DayData> data = mHelper.getAllData();
        if (data.size() < 2){
           mGraphKey.setText("Log more days to plot usage graph");
        } else {
            mGraphKey.setText("Usage over Days");
        }

        List<Entry> entries = new ArrayList<>();
        int maximumTime = 0;
        for (int i=0; i<data.size(); i++){
            if (data.get(i).getSeconds() > maximumTime){
                maximumTime = data.get(i).getSeconds();
            }
            entries.add(new Entry(i+1, data.get(i).getSeconds()));
        }
        Collections.reverse(data);
        mAdapter.refreshData(data);

        mLineChart.getAxisLeft().setAxisMaximum((float)maximumTime*1.125f);
        mLineChart.getAxisLeft().setDrawLabels(false);
        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawLabels(true);
        xAxis.setLabelCount(data.size());
        xAxis.setAxisMaximum((float)data.size());
        xAxis.setAxisMinimum(1f);

        LineDataSet set = new LineDataSet(entries, "Tombie Time");
        set.setColor(getResources().getColor(R.color.colorAccent));
        set.setValueFormatter(new MyValueFormatter());
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setDrawFilled(true);
        set.setFillAlpha(220);
        set.setFillColor(getResources().getColor(R.color.colorAccent));
        LineData lineData = new LineData(set);
        mLineChart.setData(lineData);
        if (mRefreshedHistoryFragment){
            mLineChart.animateXY(1000, 1000);
            mRefreshedHistoryFragment = false;
        } else {
            mLineChart.invalidate();
        }
    }

    @Override
    public void onResume() {
        refreshFragmentData();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mReceiver);
        super.onDestroy();
    }
}
