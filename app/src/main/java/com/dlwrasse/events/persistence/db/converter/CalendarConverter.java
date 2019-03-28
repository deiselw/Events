package com.dlwrasse.events.persistence.db.converter;

import androidx.room.TypeConverter;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by Deise on 26/02/2018.
 */

public class CalendarConverter {
    static final String FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    @TypeConverter
    public static String calendarToTimestamp(Calendar calendar) {
        if (calendar == null) return null;

        SimpleDateFormat format = new SimpleDateFormat(FORMAT_ISO8601);
        format.setTimeZone(calendar.getTimeZone());
        return format.format(calendar.getTime());
    }

    @TypeConverter
    public static Calendar fromTimestamp(String timestamp) {
        if (timestamp == null) return null;

        SimpleDateFormat format = new SimpleDateFormat(FORMAT_ISO8601);
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(format.parse(timestamp));
            calendar.setTimeZone(TimeZone.getTimeZone("GMT" + timestamp.substring(23)));
        }catch (Exception e) {
            Log.e("CalendarConverter", e.getMessage() + " - timestamp = " + timestamp);
            return null;
        }
        return calendar;
    }
}
