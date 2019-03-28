package com.dlwrasse.events.utils;

import java.util.Calendar;

public class CalendarUtils {

    public static boolean sameDate(Calendar calendar0, Calendar calendar1) {
        return calendar0.get(Calendar.YEAR) == calendar1.get(Calendar.YEAR) &&
                calendar0.get(Calendar.MONTH) == calendar1.get(Calendar.MONTH) &&
                calendar0.get(Calendar.DAY_OF_MONTH) == calendar1.get(Calendar.DAY_OF_MONTH);
    }

    public static boolean today(Calendar calendar) {
        return sameDate(calendar, Calendar.getInstance());
    }
}
