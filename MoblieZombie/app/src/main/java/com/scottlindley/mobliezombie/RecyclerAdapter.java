package com.scottlindley.mobliezombie;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Scott Lindley on 1/17/2017.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyclerViewHolder>{
    private List<DayData> mDayDataList;

    public RecyclerAdapter(List<DayData> dayDataList) {
        mDayDataList = dayDataList;
    }

    public void refreshData(List<DayData> updatedData){
        mDayDataList = updatedData;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_items, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        int totalSeconds = mDayDataList.get(position).getSeconds();
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        String timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        holder.mDate.setText(mDayDataList.get(position).getDate());
        holder.mTime.setText(timeFormatted);
        holder.mChecks.setText(mDayDataList.get(position).getChecks()+"");
    }

    @Override
    public int getItemCount() {
        return mDayDataList.size();
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder{
        public TextView mDate, mTime, mChecks;
        public RecyclerViewHolder(View itemView) {
            super(itemView);
            mDate = (TextView)itemView.findViewById(R.id.recycler_date);
            mTime = (TextView)itemView.findViewById(R.id.recycler_time);
            mChecks = (TextView)itemView.findViewById(R.id.recycler_checks);
        }
    }
}
