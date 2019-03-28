package com.dlwrasse.events.utils;

import android.widget.MultiAutoCompleteTextView;

public class TagTokenizer implements MultiAutoCompleteTextView.Tokenizer {
    @Override
    public int findTokenStart(CharSequence text, int cursor) {
        if (text.length() == 0) {
            return 0;
        }

        int i = cursor;
        while (i > 0 && text.charAt(i - 1) != ' ') {
            i--;
        }
        if (i >= text.length()) {
            return 0;
        }
        if (text.charAt(i) == '#') {
            return i;
        }

        return 0;
    }

    @Override
    public int findTokenEnd(CharSequence text, int cursor) {
        int i = cursor;
        int len = text.length();

        while (i < len) {
            if (text.charAt(i) == ' ') {
                return i;
            } else {
                i++;
            }
        }

        return len;
    }

    @Override
    public CharSequence terminateToken(CharSequence text) {
        if (text.length() == 0) {
            return text;
        }

        if (text.charAt(text.length() - 1) == ' ') {
            return text;
        }

        return text + " ";
    }
}
