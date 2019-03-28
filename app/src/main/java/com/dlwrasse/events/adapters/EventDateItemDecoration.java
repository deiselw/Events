package com.dlwrasse.events.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dlwrasse.events.R;
import com.dlwrasse.events.utils.CalendarUtils;
import com.dlwrasse.events.helpers.EventsDateItemDecorationHelper;
import com.dlwrasse.events.utils.EventDateUtils;

import java.util.Calendar;

public class EventDateItemDecoration extends RecyclerView.ItemDecoration {
    public static final String TAG = "EventsDateItemDec";

    private View mLayout;
    private TextView mTextViewLeft;
    private TextView mTextViewRight;
    private EventsDateItemDecorationHelper mHelper;

    public EventDateItemDecoration(Resources resources, RecyclerView parent) {
        mLayout = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_decoration_event, parent, false);

        mTextViewLeft = mLayout.findViewById(R.id.text_left);
        mTextViewRight = mLayout.findViewById(R.id.text_right);

        mHelper = new EventsDateItemDecorationHelper(resources);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        int position = parent.getChildAdapterPosition(view);
        if (EventViewHolderType.isHeader(parent.getAdapter().getItemViewType(position))) {
            measureLayout(parent);
            outRect.top = mLayout.getHeight();
        }
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);

        measureLayout(parent);

        EventAdapter adapter = (EventAdapter) parent.getAdapter();
        Context context = parent.getContext();
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(child);

            if (position == RecyclerView.NO_POSITION) {
                continue;
            }
            if (!EventViewHolderType.isHeader(adapter.getItemViewType(position))) {
                continue;
            }

            Calendar timestamp = adapter.getEvent(position).getTimestamp();
            mTextViewLeft.setText(DateUtils.formatDateTime(context,
                    timestamp.getTimeInMillis(), EventDateUtils.DATE_FLAGS));

            if (CalendarUtils.today(timestamp)) {
                mTextViewRight.setText(context.getString(R.string.label_today));
            }else {
                mTextViewRight.setText(mHelper.timeUntil(timestamp));
            }

            c.save();
            c.translate(0, child.getTop() - mLayout.getHeight()); // static header
            mLayout.draw(c);
            c.restore();
        }
    }

    private void measureLayout(ViewGroup parent) {
        int widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.UNSPECIFIED);

        mLayout.measure(widthSpec, heightSpec);

        mLayout.layout(0, 0, mLayout.getMeasuredWidth(), mLayout.getMeasuredHeight());
    }
}