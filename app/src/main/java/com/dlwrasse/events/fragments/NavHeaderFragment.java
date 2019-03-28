package com.dlwrasse.events.fragments;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dlwrasse.events.R;
import com.dlwrasse.events.activities.MainActivity;
import com.dlwrasse.events.activities.PreferencesActivity;
import com.dlwrasse.events.helpers.GoogleSignInHelper;
import com.dlwrasse.events.persistence.db.entity.Event;
import com.dlwrasse.events.persistence.viewmodel.EventListViewModel;
import com.dlwrasse.events.utils.EventDateUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

public class NavHeaderFragment extends Fragment implements View.OnClickListener {
    private TextView mUsernameTextView;
    private TextView mPremiumTextView;
    private TextView mEventCountTextView;
    private TextView mDateCountTextView;
    private SignInButton mSignInButton;
    private ImageView mAvatar;
    private ConstraintLayout mProfileLayout;

    private Target mPhotoTarget;
    private OnGoogleSignInUpdateListener mListener;

    @Override
    public void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());
        updateLayout(account);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_nav_header, container, false);

        mUsernameTextView = rootView.findViewById(R.id.text_username);
        mPremiumTextView = rootView.findViewById(R.id.text_premium);
        mEventCountTextView = rootView.findViewById(R.id.text_event_count);
        mDateCountTextView = rootView.findViewById(R.id.text_date_count);
        mSignInButton = rootView.findViewById(R.id.button_googleSignIn);
        mAvatar = rootView.findViewById(R.id.avatar);
        mProfileLayout = rootView.findViewById(R.id.layout_profile);

        mProfileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), PreferencesActivity.class);
                getActivity().startActivityForResult(intent, MainActivity.REQUEST_CODE_PREFS);
            }
        });

        mSignInButton.setOnClickListener(this);

        ViewModelProviders.of(this).get(EventListViewModel.class).getEventsByTimestamp()
                .observe(this, new Observer<List<Event>>() {
            @Override
            public void onChanged(@Nullable List<Event> eventList) {
                int eventCount = eventList.size();
                String eventCountText = getResources()
                        .getQuantityString(R.plurals.text_event_count, eventCount, eventCount);
                mEventCountTextView.setText(eventCountText);

                if (eventCount == 0) {
                    mDateCountTextView.setText("");
                    mDateCountTextView.setVisibility(View.GONE);
                }else {
                    long newestEventTimeInMillis = eventList.get(0).getTimestampInMillis();
                    String newestDateText = DateUtils.formatDateTime(getContext(),
                            newestEventTimeInMillis, EventDateUtils.DATE_FLAGS_SIMPLE);

                    int diffDateCount = 1;
                    String oldestDateText = "";
                    if (eventCount > 1) {
                        long oldestEventTimeInMillis = eventList.get(eventCount - 1).getTimestampInMillis();
                        oldestDateText = DateUtils.formatDateTime(getContext(),
                                oldestEventTimeInMillis, EventDateUtils.DATE_FLAGS_SIMPLE);
                        if (!newestDateText.equals(oldestDateText)) {
                            diffDateCount++;
                        }
                    }

                    String dateCountText = getResources()
                            .getQuantityString(R.plurals.text_events_period, diffDateCount, newestDateText, oldestDateText);
                    mDateCountTextView.setText(dateCountText);

                    if (mDateCountTextView.getVisibility() != View.VISIBLE) {
                        mDateCountTextView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        mPhotoTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                RoundedBitmapDrawable accountPhoto = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                accountPhoto.setCircular(true);
                mAvatar.setImageDrawable(accountPhoto);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {}
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {}
        };
        mAvatar.setTag(mPhotoTarget);

        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_googleSignIn:
                mListener.onGoogleSignInClicked();
                break;
            case R.id.button_backup:
                mListener.onBackupButtonClicked();
                break;
            default:
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (OnGoogleSignInUpdateListener) context;
        }catch (ClassCastException e) {
            throw new ClassCastException(getString(R.string.error_mustImplement,
                    context.toString(), "OnGoogleSignInUpdateListener"));

        }
    }

    public void updateLayout(final GoogleSignInAccount account) {
        if (account != null) {
            mSignInButton.setVisibility(View.GONE);

            int photoSize = getResources().getDimensionPixelSize(R.dimen.avatar_size);
            Picasso.get().load(account.getPhotoUrl()).resize(photoSize, photoSize).into(mPhotoTarget);

            mUsernameTextView.setText(account.getEmail());
            mProfileLayout.setVisibility(View.VISIBLE);
        }else {
            mProfileLayout.setVisibility(View.GONE);
            mSignInButton.setVisibility(View.VISIBLE);
        }
    }

    public void updatePremium(boolean premium) {
        if (premium) {
            mPremiumTextView.setVisibility(View.VISIBLE);
        }else {
            mPremiumTextView.setVisibility(View.GONE);
        }
    }

    public interface OnGoogleSignInUpdateListener {
        void onGoogleSignInClicked();
        void onBackupButtonClicked();
    }
}
