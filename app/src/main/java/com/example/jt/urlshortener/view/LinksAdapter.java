package com.example.jt.urlshortener.view;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.jt.urlshortener.R;

/**
 * Created by JT on 8/29/17.
 */

public class LinksAdapter extends CursorAdapter {
    public LinksAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.row, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView tvBody = (TextView) view.findViewById(R.id.tvBody);
        TextView tvPriority = (TextView) view.findViewById(R.id.tvPriority);
        // Extract properties from cursor
        String shortened = cursor.getString(cursor.getColumnIndexOrThrow("shortened"));
        String original = cursor.getString(cursor.getColumnIndexOrThrow("original"));
        // Populate fields with extracted properties
        tvBody.setText(shortened);
        tvPriority.setText(original);
    }
}
