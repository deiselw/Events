package com.dlwrasse.events.adapters;

import android.content.Context;
import android.database.Cursor;
import androidx.cursoradapter.widget.CursorAdapter;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dlwrasse.events.R;
import com.dlwrasse.events.persistence.db.virtual.DatabaseTable;

public class SearchAdapter extends CursorAdapter {

    public SearchAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.adapter_item_event, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        long timeInMillis = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseTable.COL_TIMESTAMP));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTable.COL_NAME));

        String timeText = DateUtils.formatDateTime(mContext, timeInMillis, DateUtils.FORMAT_SHOW_TIME);
        ((TextView) view.findViewById(R.id.text_time)).setText(timeText);
        ((TextView) view.findViewById(R.id.text_event)).setText(name);
    }
}
