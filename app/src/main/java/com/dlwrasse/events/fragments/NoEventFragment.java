package com.dlwrasse.events.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dlwrasse.events.R;
import com.dlwrasse.events.utils.EventDateUtils;

import java.util.Calendar;

public class NoEventFragment extends Fragment {
    public static final String TAG = "NoEventFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_no_event, container, false);

        Calendar calendar = Calendar.getInstance();

        ((TextView) rootView.findViewById(R.id.text_left))
                .setText(DateUtils.formatDateTime(getContext(), calendar.getTimeInMillis(),
                        EventDateUtils.DATE_FLAGS));

        ((TextView) rootView.findViewById(R.id.text_right)).setText(getString(R.string.label_today));

        return rootView;
    }
}
