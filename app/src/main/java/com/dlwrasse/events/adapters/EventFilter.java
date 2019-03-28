package com.dlwrasse.events.adapters;

import android.widget.Filter;

import com.dlwrasse.events.persistence.db.entity.Event;
import com.dlwrasse.events.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class EventFilter extends Filter {
    private List<Event> mSearchableList;
    private String mLastQuery = "";
    private OnFilterResultsListener mListener;

    public EventFilter(OnFilterResultsListener listener) {
        super();
        mListener = listener;
    }
	
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        if (mSearchableList.isEmpty()) {
            return null;
        }
        String query = constraint.toString().trim();
        if (mLastQuery.equals(query)) {
            return null;
        }
        mLastQuery = query;
        List<Event> resultList = getResultList(query);
        FilterResults results = new FilterResults();
        results.count = resultList.size();
        results.values = resultList;
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        if (results == null) return;
        mListener.onFilterResults((List<Event>) results.values);
    }

    private List<Event> getResultList(String text) {
        if (text.isEmpty()) {
            return new ArrayList<>(mSearchableList);
        }

        String[] words = StringUtils.removeAccents(text).toLowerCase().split("\\s");

        List<Event> resultList = new ArrayList<Event>();
        for (int i = 0; i < mSearchableList.size(); i++) {
            Event event = mSearchableList.get(i);
            String eventName = event.getText().toLowerCase();
            eventName = StringUtils.removeAccents(eventName);
            for (int j = 0; j < words.length; j++) {
                if (eventName.contains(words[j])) {
                    resultList.add(event);
                    break;
                }
            }
        }
        return resultList;
    }

    public void reset(final List<Event> list) {
        mSearchableList = list;
        mLastQuery = "";
    }

    public void setContentList(final List<Event> list) {
        mSearchableList = list;
    }

    protected interface OnFilterResultsListener {
        void onFilterResults(List<Event> results);
    }
}
