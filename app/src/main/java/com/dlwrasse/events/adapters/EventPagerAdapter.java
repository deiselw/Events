package com.dlwrasse.events.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.dlwrasse.events.fragments.EventFragment;

import java.util.List;

public class EventPagerAdapter extends FragmentStatePagerAdapter {
    private List<Integer> mEventIdList;
    private OnDataSetFirstChangeListener mOnDataSetFirstChangeListener;
    private boolean mFirstTime = true;

    public EventPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void setEventList(List<Integer> eventList) {
        mEventIdList = eventList;

        if (mFirstTime && mOnDataSetFirstChangeListener != null) {
            mFirstTime = false;
            mOnDataSetFirstChangeListener.onDataSetFirstChange();
        }

        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int i) {
        int eventId = mEventIdList.get(getTimelinePosition(i));
        return EventFragment.newInstance(eventId);
    }

    @Override
    public int getCount() {
        if (mEventIdList == null) {
            return 0;
        }
        return mEventIdList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }

    public void setOnDataSetReadyListener(OnDataSetFirstChangeListener listener) {
        mOnDataSetFirstChangeListener = listener;
    }

    public int getEventId(int position) {
        return mEventIdList.get(getTimelinePosition(position));
    }

    public int getTimelinePosition(int position) {
        return mEventIdList.size() - position - 1;
    }

    public interface OnDataSetFirstChangeListener {
        void onDataSetFirstChange();
    }
}
