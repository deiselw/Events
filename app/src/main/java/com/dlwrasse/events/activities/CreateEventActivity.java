package com.dlwrasse.events.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import com.dlwrasse.events.persistence.EventRepository;
import com.dlwrasse.events.persistence.db.entity.Event;
import com.dlwrasse.events.persistence.db.entity.utils.Tag;
import com.dlwrasse.events.persistence.viewmodel.TagListViewModel;
import com.dlwrasse.events.utils.TagTextWatcher;
import com.dlwrasse.events.utils.TagTokenizer;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Deise on 21/02/2018.
 */

public class CreateEventActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {
    public static final String TAG = "CreateEventActivity";

    private AlertDialog mConfirmDialog;
    private DatePickerDialog mDatePickerDialog;
    private TimePickerDialog mTimePickerDialog;
    private MultiAutoCompleteTextView mEventEditText;
    private TextView mDateTextView;
    private TextView mTimeTextView;
    private Calendar mTimestamp;
    private TagTextWatcher mTextWatcher;
    private ArrayAdapter<String> mTagAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        mTextWatcher = new TagTextWatcher();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.activity_edit_event);
        toolbar.setTitle(R.string.title_create_event);

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

        mTimestamp = Calendar.getInstance();
        long timeInMillis = mTimestamp.getTimeInMillis();

        // date picker dialog
        mDatePickerDialog = new DatePickerDialog(this, this, mTimestamp.get(Calendar.YEAR),
                mTimestamp.get(Calendar.MONTH), mTimestamp.get(Calendar.DAY_OF_MONTH));
        String dateText = DateUtils.formatDateTime(this, timeInMillis,
                DateUtils.FORMAT_SHOW_DATE);
        mDateTextView.setText(dateText);
        mDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatePickerDialog.show();
            }
        });

        // time ticker dialog
        mTimePickerDialog = new TimePickerDialog(this, this,
                mTimestamp.get(Calendar.HOUR_OF_DAY), mTimestamp.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(this));
        String timeText = DateUtils.formatDateTime(this, timeInMillis,
                DateUtils.FORMAT_SHOW_TIME);
        mTimeTextView.setText(timeText);
        mTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimePickerDialog.show();
            }
        });

        // confirm close without save dialog
        AlertDialog.Builder confirmDialogBuilder = new AlertDialog.Builder(this);
        confirmDialogBuilder.setMessage(R.string.text_discard_new_event)
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
                if (mEventEditText.getText().toString().trim().length() > 0) {
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
                        String eventText = mEventEditText.getText().toString().trim();
                        if (eventText.length() > 0) {
                            EventRepository.getInstance(getApplication()).insert(
                                    new Event(eventText, mTimestamp.getTimeInMillis()));
                        }
                        finish();
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        mTimestamp.set(Calendar.YEAR, year);
        mTimestamp.set(Calendar.MONTH, month);
        mTimestamp.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String dateText = DateUtils.formatDateTime(null, mTimestamp.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_DATE);
        mDateTextView.setText(dateText);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mTimestamp.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mTimestamp.set(Calendar.MINUTE, minute);
        String timeText = DateUtils.formatDateTime(this, mTimestamp.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_TIME);
        mTimeTextView.setText(timeText);
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
}
