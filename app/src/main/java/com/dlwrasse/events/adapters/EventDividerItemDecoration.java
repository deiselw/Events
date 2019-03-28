package com.dlwrasse.events.adapters;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;

public class EventDividerItemDecoration extends RecyclerView.ItemDecoration {
    private static final String TAG = "EventsDividerItemDec";

    private Drawable mDivider;
    private final Rect mBounds = new Rect();

    public EventDividerItemDecoration(Drawable divider) {
        mDivider = divider;
        if (mDivider == null) {
            Log.w(TAG, "Divider is null");
        }
    }

    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        if (parent.getLayoutManager() == null || mDivider == null) {
            return;
        }
        drawHorizontal(canvas, parent);
    }

    private void drawHorizontal(Canvas canvas, RecyclerView parent) {
        canvas.save();
        final int left;
        final int right;

        if (parent.getClipToPadding()) {
            left = parent.getPaddingLeft();
            right = parent.getWidth() - parent.getPaddingRight();
            canvas.clipRect(left, parent.getPaddingTop(), right,
                    parent.getHeight() - parent.getPaddingBottom());
        }else {
            left = 0;
            right = parent.getWidth();
        }

        EventAdapter adapter = (EventAdapter) parent.getAdapter();

        final int count = parent.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(child);

            if (position == RecyclerView.NO_POSITION) {
                continue;
            }

            if (!EventViewHolderType.isFooter(adapter.getItemViewType(position))) {
                continue;
            }

            parent.getDecoratedBoundsWithMargins(child, mBounds);
            final int bottom = mBounds.bottom;
            final int top = bottom - mDivider.getIntrinsicHeight();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(canvas);
        }
        canvas.restore();
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (position == RecyclerView.NO_POSITION) {
            return;
        }

        if (EventViewHolderType.isFooter(parent.getAdapter().getItemViewType(position))) {
            outRect.set(0, 0, mDivider.getIntrinsicWidth(), mDivider.getIntrinsicHeight());
        }else {
            outRect.set(0, 0, 0, 0);
        }
    }
}
