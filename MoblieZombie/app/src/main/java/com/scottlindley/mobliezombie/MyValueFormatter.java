package com.scottlindley.mobliezombie;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

/**
 * Created by Scott Lindley on 1/18/2017.
 */

public class MyValueFormatter implements IValueFormatter {
    public MyValueFormatter() {}

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        return "";
    }
}