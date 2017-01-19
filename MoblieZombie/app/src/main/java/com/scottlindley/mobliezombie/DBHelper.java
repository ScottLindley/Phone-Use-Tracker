package com.scottlindley.mobliezombie;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Scott Lindley on 1/17/2017.
 */

public class DBHelper extends SQLiteOpenHelper{
    public static final String DATABASE_NAME = DBAssetHelper.DATA_BASE_NAME;
    public static final int VERSION_NUMBER = 1;

    public static final String DAY_TABLE = "Day";

    public static final String COL_DATE = "date";
    public static final String COL_SECONDS = "seconds";
    public static final String COL_CHECKS = "checks";

    public static final String CREATE_DAY_TABLE =
            "CREATE TABLE "+DAY_TABLE+" ("+
                    COL_DATE+" TEXT, "+
                    COL_SECONDS+" INTEGER, "+
                    COL_CHECKS+" INTEGER)";

    private static DBHelper sInstance;
    private Context mContext;

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION_NUMBER);
        mContext = context;
    }

    public static DBHelper getInstance(Context context){
        if(sInstance == null){
            sInstance = new DBHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_DAY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+DAY_TABLE);
        this.onCreate(sqLiteDatabase);
    }

    public int updateChecks (String day, int checks){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CHECKS, checks);
        int rowsAffected = db.update(DAY_TABLE, values, "date = ?", new String[]{day});
        db.close();
        return rowsAffected;
    }

    public int updateSeconds (String day, int seconds){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SECONDS, seconds);
        int rowsAffected = db.update(DAY_TABLE, values, "date = ?", new String[]{day});
        db.close();
        return rowsAffected;
    }

    public int updateYesterday (int seconds, int checks){
        List<DayData> dayDataList = getAllData();
        String yesterday = dayDataList.get(dayDataList.size()-1).getDate();

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SECONDS, seconds);
        values.put(COL_CHECKS, checks);
        int rowsAffected = db.update(DAY_TABLE, values, "date = ?", new String[]{yesterday});
        db.close();
        return rowsAffected;
    }

    public void addNewDateEntry(String day, int seconds, int checks){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DATE, day);
        values.put(COL_SECONDS, seconds);
        values.put(COL_CHECKS, checks);
        db.insert(DAY_TABLE, null, values);
        db.close();
    }

    public DayData getDaysData(String day){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                DAY_TABLE, null,
                COL_DATE+" LIKE ?", new String[]{day}, null, null, null);
        if (cursor.moveToFirst()){
            DayData data = new DayData(
                    cursor.getInt(cursor.getColumnIndex(COL_SECONDS)),
                    cursor.getInt(cursor.getColumnIndex(COL_CHECKS)),
                    day);
            cursor.close();
            return data;
        }
        cursor.close();
        return null;
    }

    public List<DayData> getAllData(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                DAY_TABLE, null, null, null, null, null, null);
        List<DayData> data = new ArrayList<>();
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                data.add(new DayData(
                        cursor.getInt(cursor.getColumnIndex(COL_SECONDS)),
                        cursor.getInt(cursor.getColumnIndex(COL_CHECKS)),
                        cursor.getString(cursor.getColumnIndex(COL_DATE))
                ));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return data;
    }
}
