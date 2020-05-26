package com.ackrotech.healthplus.Utility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "health_plus.db";
    public static final String CONTACTED_USERS_TABLE_NAME = "contacted_users";
    public static final String CONTACTED_USERS_UUID = "u_id";
    private Context mCxt;
    private static DBHelper mInstance = null;


    public synchronized static DBHelper getInstance(Context ctx) {

        if (mInstance == null) {
            mInstance = new DBHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DB", "Table created");
        db.execSQL(
                "create table " + CONTACTED_USERS_TABLE_NAME+ "("+CONTACTED_USERS_UUID+" VARCHAR primary key)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS "+CONTACTED_USERS_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertContact(String uuid) {
        Log.d("DB", "inserted"+uuid);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
            contentValues.put(CONTACTED_USERS_UUID, uuid);
        db.insert(CONTACTED_USERS_TABLE_NAME, null, contentValues);
        return true;
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from "+CONTACTED_USERS_TABLE_NAME+" where "+CONTACTED_USERS_UUID+"=" + id + "", null);
        return res;
    }

    public int numberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, CONTACTED_USERS_TABLE_NAME);
        return numRows;
    }


    public Integer deleteContact(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(CONTACTED_USERS_TABLE_NAME,
                CONTACTED_USERS_UUID+" = ? ",
                new String[]{Integer.toString(id)});
    }

    public ArrayList<String> getAllContactedUser() {
        ArrayList<String> array_list = new ArrayList<String>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from "+CONTACTED_USERS_TABLE_NAME, null);
        res.moveToFirst();

        while (res.isAfterLast() == false) {
            array_list.add(res.getString(res.getColumnIndex(CONTACTED_USERS_TABLE_NAME)));
            res.moveToNext();
        }
        return array_list;
    }
}