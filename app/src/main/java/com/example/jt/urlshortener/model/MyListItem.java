package com.example.jt.urlshortener.model;

import android.database.Cursor;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by JT on 8/29/17.
 */

public class MyListItem {
    private String shortened;
    private String time;
    private String original;
    private int id;

    public MyListItem(String shortened, String original, String time, int id) {
        this.shortened = shortened;
        this.original = original;
        this.time = time;
        this.id = id;
    }

    public void setShortened(String shortened) {
        this.shortened = shortened;
    }

    public String getShortened() {
        return shortened;
    }
    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }
    public void setID(int id) {
        this.id = id;
    }

    public int getID() {
        return id;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public String getOriginal() {
        return original;
    }

    public static MyListItem fromCursor(Cursor cursor) {
        MyListItem listItem = new MyListItem(cursor.getString(cursor.getColumnIndex("shortened")),
                cursor.getString(cursor.getColumnIndex("original")),
                cursor.getString(cursor.getColumnIndex("sqltime")),
                cursor.getInt(cursor.getColumnIndex("_id")));
        Log.d(TAG,  cursor.getString(cursor.getColumnIndex("original")));
        return listItem;
    }
}
