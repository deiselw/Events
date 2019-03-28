package com.dlwrasse.events.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.dlwrasse.events.utils.Date;

public class DateTickBroadcastReceiver extends BroadcastReceiver {
    private IntentFilter mFilter;
    private Date mDate;
    private OnDateTickListener mCallback;

    public interface OnDateTickListener {
        void onDateTick();
    }

    public DateTickBroadcastReceiver(OnDateTickListener callback) {
        mCallback = callback;

        mDate = new Date();

        mFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mFilter.addAction(Intent.ACTION_TIME_TICK);
    }

    public IntentFilter getFilter() {
        return mFilter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == Intent.ACTION_TIME_TICK) {
            Date newDate = new Date();
            if (mDate.before(newDate)) {
                mCallback.onDateTick();
                mDate = newDate;
            }
        }
    }
}