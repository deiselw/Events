package com.dlwrasse.events.adapters;

public class EventViewHolderType {
    public static final int VIEW_TYPE_DEFAULT = 0;
    public static final int VIEW_TYPE_HEADER = 1 << 0;
    public static final int VIEW_TYPE_FOOTER = 1 << 1;

    public static boolean isHeader(int viewType) {
        return (viewType & VIEW_TYPE_HEADER) != 0;
    }

    public static boolean isFooter(int viewType) {
        return (viewType & VIEW_TYPE_FOOTER) != 0;
    }
}
