package com.scottlindley.mobliezombie;

/**
 * Created by Scott Lindley on 1/17/2017.
 */

public class DayData {
    private int mSeconds;
    private int mChecks;
    private String mDate;

    public DayData(int seconds, int checks, String date) {
        mSeconds = seconds;
        mChecks = checks;
        mDate = date;
    }

    public int getSeconds() {
        return mSeconds;
    }

    public int getChecks() {
        return mChecks;
    }

    public String getDate() {
        return mDate;
    }
}
