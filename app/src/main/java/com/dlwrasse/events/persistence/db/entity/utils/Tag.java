package com.dlwrasse.events.persistence.db.entity.utils;

import java.util.ArrayList;
import java.util.Comparator;

public class Tag {
    private String mName;
    private ArrayList<Integer> mEventIds;
    private int mCount = 1;

    public static final Comparator<Tag> COMPARATOR = new Comparator<Tag>() {
        @Override
        public int compare(Tag tag0, Tag tag1) {
            return tag0.getName().compareTo(tag1.getName());
        }
    };

    public Tag(String name) {
        mName = name;
        mEventIds = new ArrayList<>();
    }

    public String getName() { return mName; }
    public ArrayList<Integer> getEventIds() { return mEventIds; }
    public int getEventCount() { return mEventIds.size(); }
    public void incTagCount() {
        mCount++;
    }
    public int getCount() { return mCount; }

    public void addEventId(int eventId) {
        mEventIds.add(eventId);
    }
}
