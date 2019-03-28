package com.dlwrasse.events.fragments;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dlwrasse.events.R;
import com.dlwrasse.events.persistence.db.entity.Event;
import com.dlwrasse.events.persistence.viewmodel.EventViewModel;
import com.dlwrasse.events.utils.EventDateUtils;

public class EventFragment extends Fragment {
    private static final String ARG_EVENT_ID = "id";

    private TextView mDateTextView;
    private TextView mTimeTextView;
    private TextView mEventTextView;

    public static EventFragment newInstance(int eventId) {
        EventFragment f = new EventFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_EVENT_ID, eventId);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_event, container, false);
        Bundle args = getArguments();

        mDateTextView = rootView.findViewById(R.id.text_date);
        mTimeTextView = rootView.findViewById(R.id.text_time);
        mEventTextView = rootView.findViewById(R.id.text_event);

        EventViewModel.Factory factory = new EventViewModel.Factory(getActivity().getApplication(),
                args.getInt(ARG_EVENT_ID));
        EventViewModel eventViewModel = ViewModelProviders.of(this, factory).get(EventViewModel.class);
        eventViewModel.getEvent().observe(this, new Observer<Event>() {
                    @Override
                    public void onChanged(@Nullable Event event) {
                        if (event == null) {
                            return;
                        }

                        long timestamp = event.getTimestampInMillis();
                        String dateText = DateUtils.formatDateTime(getContext(), timestamp,
                                EventDateUtils.DATE_FLAGS);
                        mDateTextView.setText(dateText);

                        String timeText = DateUtils.formatDateTime(getContext(), timestamp,
                                EventDateUtils.TIME_FLAGS);
                        mTimeTextView.setText(timeText);

                        mEventTextView.setText(event.getTaggedText());
                    }
        });

        return rootView;
    }
}
