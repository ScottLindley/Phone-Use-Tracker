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

import java.util.List;

public class HistoryFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private RecyclerAdapter mAdapter;
    private SwipeRefreshLayout mRefreshLayout;
    private DBHelper mHelper;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mHelper = DBHelper.getInstance(view.getContext());

        mRecyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(
                new LinearLayoutManager(view.getContext(),LinearLayoutManager.VERTICAL, true));
        List<DayData> dayDataList = mHelper.getAllData();
        mAdapter = new RecyclerAdapter(dayDataList);
        mRecyclerView.setAdapter(mAdapter);

        mRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.recycler_swipe_refresh);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
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
        mAdapter.refreshData(data);
    }

    @Override
    public void onResume() {
        refreshFragmentData();
        super.onResume();
    }
}
