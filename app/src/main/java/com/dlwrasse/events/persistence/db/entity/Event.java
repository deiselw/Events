package com.dlwrasse.events.persistence.db.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import android.text.Editable;

import com.dlwrasse.events.persistence.db.entity.utils.TaggedText;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Deise on 19/02/2018.
 */

@Entity(tableName = "event")
public class Event {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String text;
    private Calendar timestamp;

    @Ignore
    private TaggedText mTaggedText;

    public Event(String text, long timeInMillis) {
        Calendar timestamp = Calendar.getInstance();
        timestamp.setTimeInMillis(timeInMillis);

        init(text, timestamp);
    }

    public Event(String text, Calendar timestamp) {
        init(text, timestamp);
    }

    private void init(String text, Calendar timestamp) {
        this.text = text;
        this.timestamp = timestamp;

        mTaggedText = new TaggedText(this.text);
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) { this.id = id; }

    public String getText() {
        return this.text;
    }

    public Calendar getTimestamp() {
        return this.timestamp;
    }

    public long getTimestampInMillis() {
        return this.timestamp.getTimeInMillis();
    }

    public void setText(String text) {
        this.text = text;

        mTaggedText.setInput(this.text);
    }

    public void setTimestamp(Calendar timestamp) {
        this.timestamp = timestamp;
    }

    public Editable getTaggedText() {
        return mTaggedText.getText();
    }

    public ArrayList<String> getTags() {
        return mTaggedText.getTags();
    }
}