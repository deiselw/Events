package com.dlwrasse.events.utils;

import android.text.format.DateUtils;

public class EventDateUtils {
    public static final int DATE_FLAGS = DateUtils.FORMAT_SHOW_DATE|DateUtils.FORMAT_SHOW_YEAR|
            DateUtils.FORMAT_SHOW_WEEKDAY|DateUtils.FORMAT_ABBREV_ALL;
    public static final int TIME_FLAGS = DateUtils.FORMAT_SHOW_TIME;
    public static final int DATE_FLAGS_SIMPLE = DateUtils.FORMAT_SHOW_DATE|
            DateUtils.FORMAT_SHOW_YEAR|DateUtils.FORMAT_ABBREV_ALL;
}
