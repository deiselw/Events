package com.dlwrasse.events.utils;

import java.util.Calendar;

public class Date {
    private int mDayOfMonth;
    private int mMonth;
    private int mYear;

    public Date() {
        init(Calendar.getInstance());
    }

    private void init(Calendar calendar) {
        mDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        mMonth = calendar.get(Calendar.MONTH);
        mYear = calendar.get(Calendar.YEAR);
    }

    public void setDate(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        init(calendar);
    }

    public void setDate(int year, int month, int dayOfMonth) {
        setYear(year);
        setMonth(month);
        setDayOfMonth(dayOfMonth);
    }

    public int getDayOfMonth() {
        return mDayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        mDayOfMonth = dayOfMonth;
    }

    public int getMonth() {
        return mMonth;
    }

    public void setMonth(int month) {
        mMonth = month;
    }

    public int getYear() {
        return mYear;
    }

    public void setYear(int year) {
        mYear = year;
    }

    public boolean same(Date date) {
        return mDayOfMonth == date.getDayOfMonth() &&
                mMonth == date.getMonth() &&
                mYear == date.getYear();
    }

    public boolean after(Date date) {
        int dateYear = date.getYear();
        boolean futureYear = mYear > dateYear;
        boolean sameYear = mYear == dateYear;

        int dateMonth = date.getMonth();
        boolean futureMonth = mMonth > dateMonth;
        boolean sameMonth = mMonth == dateMonth;

        boolean futureDay = mDayOfMonth > date.getDayOfMonth();

        return futureYear || (sameYear && futureMonth) || (sameYear && sameMonth && futureDay);
    }

    public boolean before(Date date) {
        int dateYear = date.getYear();
        boolean previousYear = mYear < dateYear;
        boolean sameYear = mYear == dateYear;

        int dateMonth = date.getMonth();
        boolean previousMonth = mMonth < dateMonth;
        boolean sameMonth = mMonth == dateMonth;

        boolean previousDay = mDayOfMonth < date.getDayOfMonth();

        return previousYear || (sameYear && previousMonth) || (sameYear && sameMonth && previousDay);
    }

    public boolean future() {
        Date today = new Date();
        return after(today);
    }

    public boolean today() {
        Date today = new Date();
        return same(today);
    }

    public boolean past() {
        Date today = new Date();
        return before(today);
    }
}
