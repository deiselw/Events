package com.dlwrasse.events.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;

import com.dlwrasse.events.adapters.EventPagerAdapter;
import com.dlwrasse.events.R;
import com.dlwrasse.events.persistence.viewmodel.EventIdListViewModel;

import java.util.List;

/**
 * Created by Deise on 09/04/2018.
 */

public class TimelineActivity extends AppCompatActivity {
    public static final String EXTRA_POS = "com.dlwrasse.events.POS";
    private static final String STATE_POS = "pos";

    private EventIdListViewModel mEventIdListViewModel;
    private EventPagerAdapter mAdapter;
    private ViewPager mPager;
    private AlertDialog mConfirmDialog;
    private ImageButton mButtonPrev;
    private ImageButton mButtonNext;
    private int mCurrentPosition;

    private final ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            mCurrentPosition = position;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        if (savedInstanceState == null) {
            mCurrentPosition = getIntent().getIntExtra(EXTRA_POS, 0);
        }else {
            mCurrentPosition = savedInstanceState.getInt(STATE_POS);
        }

        mPager = findViewById(R.id.pager);
        mAdapter = new EventPagerAdapter(getSupportFragmentManager());
        mAdapter.setOnDataSetReadyListener(new EventPagerAdapter.OnDataSetFirstChangeListener() {
            @Override
            public void onDataSetFirstChange() {
                mCurrentPosition = mAdapter.getTimelinePosition(mCurrentPosition); // correct pos first time
                mPager.setCurrentItem(mCurrentPosition);
            }
        });
        mPager.setAdapter(mAdapter);

        mEventIdListViewModel = ViewModelProviders.of(this).get(EventIdListViewModel.class);
        mEventIdListViewModel.getEventIdsByTimestamp().observe(this, new Observer<List<Integer>>() {
            @Override
            public void onChanged(@Nullable List<Integer> eventIdList) {
                mAdapter.setEventList(eventIdList);
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String confirmDeleteText = getResources()
                .getQuantityString(R.plurals.text_delete_event, 1);

        AlertDialog.Builder confirmDialogBuilder = new AlertDialog.Builder(this);
        confirmDialogBuilder.setMessage(confirmDeleteText)
                .setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                        int eventId = mAdapter.getEventId(mPager.getCurrentItem());
                        mEventIdListViewModel.delete(eventId);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        mConfirmDialog = confirmDialogBuilder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_POS, mCurrentPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mPager.addOnPageChangeListener(mOnPageChangeListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        mPager.removeOnPageChangeListener(mOnPageChangeListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.item_edit) {
            startEditActivity();
            return true;
        }else if (id == R.id.item_delete) {
            mConfirmDialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startEditActivity() {
        int eventId = mAdapter.getEventId(mPager.getCurrentItem());

        Intent intent = new Intent(this, EditEventActivity.class);
        intent.putExtra(EditEventActivity.EXTRA_EVENT_ID, eventId);
        startActivity(intent);
    }
}