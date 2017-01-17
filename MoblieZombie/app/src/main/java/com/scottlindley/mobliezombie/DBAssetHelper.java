package com.scottlindley.mobliezombie;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by Scott Lindley on 1/17/2017.
 */

public class DBAssetHelper extends SQLiteAssetHelper {
    public static final String DATA_BASE_NAME = "phonedata.db";
    public static final int VERSION_NUMBER = 1;

    public DBAssetHelper(Context context) {
        super(context,DATA_BASE_NAME, null,VERSION_NUMBER);
    }
}
