package com.dlwrasse.events.helpers;

import android.content.res.Resources;
import androidx.annotation.NonNull;

import com.dlwrasse.events.R;

import java.util.Calendar;

public class EventsDateItemDecorationHelper {
    public static final String TAG = "EventsDateItemDecHelper";

    private Calendar mCalendar;
    private Resources mResources;

    public EventsDateItemDecorationHelper(Resources resources) {
        mResources = resources;
        mCalendar = Calendar.getInstance();
    }

    public String timeUntil(final Calendar calendar) {
        long currentTimeInMillis = System.currentTimeMillis();
        long timeInMillis = calendar.getTimeInMillis();
        if (currentTimeInMillis == timeInMillis) return "";

        boolean futureDate = timeInMillis > currentTimeInMillis;

        mCalendar.setTimeInMillis(currentTimeInMillis);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        int currentYear = mCalendar.get(Calendar.YEAR);
        int currentMonth = mCalendar.get(Calendar.MONTH);
        int currentDay = mCalendar.get(Calendar.DAY_OF_MONTH);

        if (year == currentYear && month == currentMonth && day == currentDay) { // today
            return "";
        }

        int diffYears = futureDate ? year - currentYear : currentYear - year;
        if (futureDate) {
            if (month < currentMonth || (month == currentMonth && day < currentDay)) {
                diffYears--;
            }
        }else { // past
            if (month > currentMonth || (month == currentMonth && day > currentDay)) {
                diffYears--;
            }
        }

        int yearMonths = mCalendar.getActualMaximum(Calendar.MONTH);

        int diffMonths;
        if (futureDate) {
            if (month > currentMonth) {
                diffMonths = month - currentMonth;
            }else {
                diffMonths = yearMonths + month - currentMonth;
            }
            if (diffMonths > 0 && day < currentDay) {
                diffMonths--;
            }
            diffMonths = diffMonths%yearMonths;
        }else { // past
            if (month < currentMonth) {
                diffMonths = currentMonth - month;
            }else {
                diffMonths = yearMonths + currentMonth - month;
            }
            if (diffMonths > 0 && day > currentDay) {
                diffMonths--;
            }
            diffMonths = diffMonths%yearMonths;
        }

        int diffDays;
        if (futureDate) {
            if (day >= currentDay) {
                diffDays = day - currentDay;
            }else {
                Calendar prevMonthCal = Calendar.getInstance();
                int prevMonth = calendar.get(Calendar.MONTH) - 1;
                if (prevMonth < Calendar.JANUARY) {
                    prevMonth += yearMonths;
                }
                prevMonthCal.set(Calendar.MONTH, prevMonth);
                diffDays = day + prevMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH) - currentDay;
            }
        }else {
            if (currentDay >= day) {
                diffDays = currentDay - day;

            }else {
                Calendar pastMonthCal = Calendar.getInstance();
                int prevMonth = currentMonth - 1;
                if (prevMonth < Calendar.JANUARY) {
                    prevMonth += yearMonths;
                }
                pastMonthCal.set(Calendar.MONTH, prevMonth);
                diffDays = currentDay + pastMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH) - day;
            }
        }

        String textYears = getTextTime(R.plurals.text_years, diffYears);
        String textMonths = getTextTime(R.plurals.text_months, diffMonths);
        String textDays = getTextTime(R.plurals.text_days, diffDays);

        String text = "";
        if (!textYears.isEmpty()) {
            text = textYears;
        }
        if (!textMonths.isEmpty()) {
            text += text.isEmpty() ? textMonths : " " + textMonths;
        }
        if (!textDays.isEmpty()) {
            text += text.isEmpty() ? textDays : " " + textDays;
        }

        if (futureDate) {
            text = mResources.getString(R.string.text_time_in, text);
        }else {
            text = mResources.getString(R.string.text_time_ago, text);
        }

        return text;
    }

    @NonNull
    private String getTextTime(int resId, int diffTime) {
        if (diffTime == 0) {
            return "";
        }
        return mResources.getQuantityString(resId, diffTime, diffTime);
    }
}
