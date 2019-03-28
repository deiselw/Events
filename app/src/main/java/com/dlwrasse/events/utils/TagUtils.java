package com.dlwrasse.events.utils;

import android.graphics.Color;
import android.text.Editable;
import android.text.style.ForegroundColorSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagUtils {
    private static int mTagColor = Color.parseColor("#6ec6ff");
    private static final Pattern mTagPattern = Pattern.compile("(?<=^|\\s)(#\\w+)(?=$|\\s)");
    public static Matcher getTagMatcher(CharSequence input) {
        return mTagPattern.matcher(input);
    }

    public static void updateSpans(Editable s) {
        removeSpans(s);
        addSpans(s);
    }

    public static void removeSpans(Editable s) {
        ForegroundColorSpan[] spans = s.getSpans(0, s.length(), ForegroundColorSpan.class);
        for (int i = 0; i < spans.length; i++) {
            s.removeSpan(spans[i]);
        }
    }

    public static void addSpans(Editable s) {
        Matcher matcher = getTagMatcher(s);
        while (matcher.find()) {
            s.setSpan(new ForegroundColorSpan(mTagColor), matcher.start(), matcher.end(),
                    Editable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}
