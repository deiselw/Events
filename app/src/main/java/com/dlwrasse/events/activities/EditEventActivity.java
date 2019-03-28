package com.dlwrasse.events.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.dlwrasse.events.R;
import com.dlwrasse.events.persistence.db.entity.Event;
import com.dlwrasse.events.persistence.db.entity.utils.Tag;
import com.dlwrasse.events.persistence.viewmodel.EventViewModel;
import com.dlwrasse.events.persistence.viewmodel.TagListViewModel;
import com.dlwrasse.events.utils.TagTextWatcher;
import com.dlwrasse.events.utils.TagTokenizer;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Deise on 21/02/2018.
 */

public class EditEventActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {
    public static final String EXTRA_EVENT_ID = "com.dlwrasse.events.EVENT_ID";

    private AlertDialog mConfirmDialog;
    private DatePickerDialog mDatePickerDialog;
    private TimePickerDialog mTimePickerDialog;
    private MultiAutoCompleteTextView mEventEditText;
    private TextView mDateTextView;
    private TextView mTimeTextView;
    private Calendar mCalendar;
    private EventViewModel mEventViewModel;
    private Event mEvent;
    private TagTextWatcher mTextWatcher;
    private ArrayAdapter<String> mTagAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        mTextWatcher = new TagTextWatcher();

        Intent intent = getIntent();
        int eventId = intent.getIntExtra(EXTRA_EVENT_ID, -1);
        if (eventId == -1) {
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.activity_edit_event);
        toolbar.setTitle(R.string.title_edit_event);

        mEventEditText = findViewById(R.id.edit_text_event);
        mDateTextView = findViewById(R.id.text_date);
        mTimeTextView = findViewById(R.id.text_time);

        mTagAdapter = new ArrayAdapter<>(this, R.layout.dropdown_item_compact);
        mEventEditText.setAdapter(mTagAdapter);
        mEventEditText.setThreshold(1);
        mEventEditText.setTokenizer(new TagTokenizer());

        TagListViewModel tagListViewModel = ViewModelProviders.of(this).get(TagListViewModel.class);
        tagListViewModel.getTags().observe(this, new Observer<List<Tag>>() {
            @Override
            public void onChanged(@Nullable List<Tag> tagList) {
                String[] tagNames = new String[tagList.size()];
                for (int i = 0; i < tagList.size(); i++) {
                    tagNames[i] = tagList.get(i).getName();
                }
                mTagAdapter.addAll(tagNames);
            }
        });

        mCalendar = Calendar.getInstance();

        EventViewModel.Factory factory = new EventViewModel.Factory(getApplication(), eventId);
        mEventViewModel = ViewModelProviders.of(this, factory).get(EventViewModel.class);
        mEventViewModel.getEvent().observe(this, new Observer<Event>() {
            @Override
            public void onChanged(@Nullable Event event) {
                mEvent = event;

                Editable text = event.getTaggedText();
                mEventEditText.setText(text);
                mEventEditText.setSelection(text.length());

                long timeInMillis = event.getTimestampInMillis();
                mCalendar.setTimeInMillis(timeInMillis);
                mDateTextView.setText(DateUtils.formatDateTime(EditEventActivity.this, timeInMillis, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
                mTimeTextView.setText(DateUtils.formatDateTime(EditEventActivity.this, timeInMillis, DateUtils.FORMAT_SHOW_TIME));

                mDatePickerDialog.updateDate(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
                mTimePickerDialog.updateTime(mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE));
            }
        });

        // date picker dialog
        mDatePickerDialog = new DatePickerDialog(this, this, mCalendar.get(Calendar.YEAR),
                mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
        mDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatePickerDialog.show();
            }
        });

        // time ticker dialog
        mTimePickerDialog = new TimePickerDialog(this, this, mCalendar.get(Calendar.HOUR_OF_DAY),
                mCalendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(this));
        mTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimePickerDialog.show();
            }
        });

        // confirm close without saving dialog
        AlertDialog.Builder confirmDialogBuilder = new AlertDialog.Builder(this);
        confirmDialogBuilder.setMessage(R.string.text_discard_changes)
                .setPositiveButton(R.string.action_discard, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        mConfirmDialog = confirmDialogBuilder.create();

        // close button
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (changed()) {
                    mConfirmDialog.show();
                }else {
                    finish();
                }
            }
        });

        // save button
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item_save:
                        update();
                        finish();
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mEventEditText.addTextChangedListener(mTextWatcher);
    }

    @Override
    public void onStop() {
        super.onStop();
        mEventEditText.removeTextChangedListener(mTextWatcher);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String dateText = DateUtils.formatDateTime(this, mCalendar.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_DATE);
        mDateTextView.setText(dateText);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mCalendar.set(Calendar.MINUTE, minute);
        String timeText = DateUtils.formatDateTime(this, mCalendar.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_TIME);
        mTimeTextView.setText(timeText);
    }

    @Override
    public void onBackPressed() {
        if (changed()) {
            mConfirmDialog.show();
        }else {
            super.onBackPressed();
        }
    }

    private String getText() {
        return mEventEditText.getText().toString().trim();
    }

    private boolean textChanged() {
        return !mEvent.getText().equals(getText());
    }

    private boolean timestampChanged() {
        return mEvent.getTimestampInMillis() != mCalendar.getTimeInMillis();
    }

    private boolean changed() {
        return textChanged() || timestampChanged();
    }

    private void update() {
        String text = mEventEditText.getText().toString().trim();
        boolean updateText = !text.isEmpty() && !mEvent.getText().equals(text);

        boolean updateTimestamp = timestampChanged();

        if (updateText && updateTimestamp) {
            mEvent.setText(text);
            mEvent.setTimestamp(mCalendar);
            mEventViewModel.update(mEvent);
        }else if (updateText) {
            mEvent.setText(text);
            mEventViewModel.updateText(mEvent);
        }else if (updateTimestamp) {
            mEvent.setTimestamp(mCalendar);
            mEventViewModel.updateTimestamp(mEvent);
        }
    }
}
