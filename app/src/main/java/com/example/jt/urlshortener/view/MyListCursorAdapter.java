package com.example.jt.urlshortener.view;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.jt.urlshortener.R;
import com.example.jt.urlshortener.model.MyListItem;

import java.util.List;

import co.dift.ui.SwipeToAction;

import static android.content.ContentValues.TAG;

/**
 * Created by skyfishjy on 10/31/14.
 */
public class MyListCursorAdapter extends CursorRecyclerViewAdapter<MyListCursorAdapter.ViewHolder> {
    private List<MyListItem> items;

    public MyListCursorAdapter(Context context, Cursor cursor, List<MyListItem> items) {
        super(context, cursor);
        this.items = items;
    }

    public static class ViewHolder extends SwipeToAction.ViewHolder<MyListItem> {

        public TextView mTextView;
        public TextView dTextView;
        public TextView oTextView;

        public ViewHolder(View view) {
            super(view);
            dTextView = (TextView) view.findViewById(R.id.tvPriority);
            mTextView = (TextView) view.findViewById(R.id.tvBody);
            oTextView = (TextView) view.findViewById(R.id.original);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        MyListItem myListItem = MyListItem.fromCursor(cursor);
        ViewHolder vh = (ViewHolder) viewHolder;
        viewHolder.dTextView.setText(myListItem.getTime());
        viewHolder.mTextView.setText(myListItem.getShortened());
        viewHolder.oTextView.setText(myListItem.getOriginal());
        vh.data = myListItem;
        Log.d(TAG,"called" + myListItem.getOriginal());
    }
}