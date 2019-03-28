package com.dlwrasse.events.persistence.db.entity.utils;

import android.text.Editable;
import android.text.SpannableStringBuilder;

import com.dlwrasse.events.utils.TagUtils;

import java.util.ArrayList;
import java.util.regex.Matcher;

public class TaggedText {
    private String mInput;
    private Editable mText;
    private ArrayList<String> mTags;
    private boolean mTagsDirty;
    private boolean mTextDirty;

    public TaggedText(String input) {
        setInput(input);
    }

    public void setInput(String input) {
        mInput = input;
        mTagsDirty = true;
        mTextDirty = true;
    }

    public Editable getText() {
        if (mTextDirty) {
            updateText();
            mTextDirty = false;
        }
        return mText;
    }

    public ArrayList<String> getTags() {
        if (mTagsDirty) {
            updateTags();
            mTagsDirty = false;
        }
        return mTags;
    }

    private void updateText() {
        if (mText == null) {
            mText = new SpannableStringBuilder(mInput);
        }else {
            TagUtils.removeSpans(mText);
        }

        TagUtils.addSpans(mText);
    }

    private void updateTags() {
        if (mTags == null) {
            mTags = new ArrayList<>();
        }else {
            mTags.clear();
        }

        Matcher matcher = TagUtils.getTagMatcher(mInput);
        while (matcher.find()) {
            String tag = mInput.substring(matcher.start(), matcher.end());
            if (!mTags.contains(tag)) {
                mTags.add(mInput.substring(matcher.start(), matcher.end()));
            }
        }
    }
}
