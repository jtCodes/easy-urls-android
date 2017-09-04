package com.example.jt.urlshortener.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.jt.urlshortener.model.Link;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by JT on 8/28/17.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "links.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_LINKS = "links";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_SHORTENED = "shortened";
    private static final String COLUMN_ORIGINAL = "original";
    private static final String COLUMN_TIME = "sqltime";

    private static DBHelper sInstance;

    public static synchronized DBHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DBHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LINKS_TABLE = "CREATE TABLE " + TABLE_LINKS +
                "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY," +
                COLUMN_SHORTENED + " TEXT," +
                COLUMN_ORIGINAL + " TEXT," +
                COLUMN_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL" +
                ")";

        db.execSQL(CREATE_LINKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_LINKS);
            onCreate(db);
        }
    }

    public void addLink(Link link) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_SHORTENED, link.shortened);
            values.put(COLUMN_ORIGINAL, link.original);
            values.put(COLUMN_TIME, link.time);

            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            db.insertOrThrow(TABLE_LINKS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add post to database");
        } finally {
            db.endTransaction();
        }
    }

    public List<Link> getAllLinks() {
        List<Link> links = new ArrayList<>();

        // SELECT * FROM LINKS
        // LEFT OUTER JOIN USERS
        // ON POSTS.KEY_POST_USER_ID_FK = USERS.KEY_USER_ID
        String POSTS_SELECT_QUERY =
                String.format("SELECT * FROM %s",
                        TABLE_LINKS);

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(POSTS_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Link newLink = new Link();
                    newLink.shortened = cursor.getString(cursor.getColumnIndex(COLUMN_SHORTENED));
                    newLink.original = cursor.getString(cursor.getColumnIndex(COLUMN_ORIGINAL));
                    newLink.time = cursor.getString(cursor.getColumnIndex(COLUMN_TIME));

                    links.add(newLink);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get posts from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return links;
    }

    public void deleteAllLinks() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // Order of deletions is important when foreign key relationships exist.
            db.delete(TABLE_LINKS, null, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to delete all posts and users");
        } finally {
            db.endTransaction();
        }
    }

    public void deleteLink(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(TABLE_LINKS, "_id = ?", new String[]{Integer.toString(id)});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }


}
