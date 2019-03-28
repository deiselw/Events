package com.dlwrasse.events.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.appcompat.widget.Toolbar;

import com.dlwrasse.events.R;
import com.dlwrasse.events.helpers.GoogleSignInHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Objects;

public class PreferencesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prefs);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        boolean premium = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.pref_key_premium), false);
        if (!premium) {
            Button backupButton = findViewById(R.id.button_backup);
            backupButton.setVisibility(View.VISIBLE);
            backupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.putExtra(MainActivity.KEY_BACKUP, true);
                    setResult(MainActivity.REQUEST_CODE_PREFS, intent);
                    finish();
                }
            });
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_prefs, new PreferencesFragment()).commit();
    }

    public static class PreferencesFragment extends PreferenceFragmentCompat
            implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
        private static final int REQUEST_CODE_GOOGLE_SIGN_IN = 0;

        private SwitchPreference mGoogleSignInPref;
        private Preference mBackupPref;
        private boolean mPremium;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.preferences);

            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(
                    Objects.requireNonNull(getContext()));
            mGoogleSignInPref = findPreference(getString(R.string.pref_key_googleSignIn));
            mGoogleSignInPref.setOnPreferenceChangeListener(this);
            setGoogleSignInPref(account);

            mPremium = PreferenceManager.getDefaultSharedPreferences(getContext())
                    .getBoolean(getString(R.string.pref_key_premium), false);

            mBackupPref = findPreference(getString(R.string.pref_key_backup));
            mBackupPref.setOnPreferenceClickListener(this);
            if (mPremium && mGoogleSignInPref.isChecked()) {
                mBackupPref.setSummary(R.string.pref_summary_on);
            }else {
                mBackupPref.setSummary(R.string.pref_summary_off);
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            if (preference.getKey() == mGoogleSignInPref.getKey()) {
                if ((Boolean) value) {
                    loadGoogleSignIn();
                }else {
                    unloadGoogleSignIn();
                }
                return false;
            }
            return false;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (preference.getKey() == mBackupPref.getKey()) {
                if (mPremium) {
                    if (!mGoogleSignInPref.isChecked()) {
                        loadGoogleSignIn();
                    }
                } else {
                    intentBackup();
                }
            }
            return false;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN) {
                GoogleSignInAccount account = GoogleSignInHelper.getAccount(data);
                if (account != null) {
                    setGoogleSignInPref(account);
                    if (mPremium) {
                        mBackupPref.setSummary(R.string.pref_summary_on);
                        intentBackup();
                    }else {
                        showPremiumDialog(account.getGivenName());
                    }
                }
            }
        }

        private void intentBackup() {
            Intent intent = new Intent();
            intent.putExtra(MainActivity.KEY_BACKUP, true);
            getActivity().setResult(MainActivity.REQUEST_CODE_PREFS, intent);
            getActivity().finish();
        }

        private void loadGoogleSignIn() {
            startActivityForResult(GoogleSignInHelper.getSignInIntent(getActivity()), REQUEST_CODE_GOOGLE_SIGN_IN);
        }

        private void unloadGoogleSignIn() {
            if (mPremium) {
                showSignOutConfirmationDialog();
            }else {
                requestGoogleSignOut();
            }
        }

        private void showPremiumDialog(String name) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            dialogBuilder
                    .setView(inflater.inflate(R.layout.dialog_premium, null))
                    .setTitle(getString(R.string.dialog_title_premiumOffer, name))
                    .setMessage(R.string.dialog_message_premiumOffer)
                    .setPositiveButton(R.string.dialog_positive_premiumOffer,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    intentBackup();
                                }
                            })
                    .setNeutralButton(R.string.dialog_negative_premiumOffer,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {}
                            });
            dialogBuilder.create().show();
        }

        private void showSignOutConfirmationDialog() {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
            dialogBuilder
                    .setTitle(R.string.dialog_title_googleSignOut)
                    .setMessage(R.string.dialog_message_googleSignOut_backupOn)
                    .setPositiveButton(R.string.dialog_positive_googleSignOut,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    requestGoogleSignOut();
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {}
                            });
            dialogBuilder.create().show();
        }

        private void requestGoogleSignOut() {
            mGoogleSignInPref.setEnabled(false);
            GoogleSignInHelper.getSignInClient(getActivity()).revokeAccess()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mGoogleSignInPref.setEnabled(true);
                            mBackupPref.setSummary(R.string.pref_summary_off);
                            setGoogleSignInPref(null);
                        }
                    });
        }

        private void setGoogleSignInPref(GoogleSignInAccount account) {
            boolean signed = account != null;

            mGoogleSignInPref.setChecked(signed);

            if (signed) {
                mGoogleSignInPref.setSummaryOn(account.getEmail());

                int photoSize = getResources().getDimensionPixelSize(R.dimen.avatar_size);
                Picasso.get()
                        .load(account.getPhotoUrl())
                        .resize(photoSize, photoSize)
                        .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        RoundedBitmapDrawable accountPhoto = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                        accountPhoto.setCircular(true);
                        mGoogleSignInPref.setIcon(accountPhoto);
                    }
                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {}
                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {}
                });
            }else {
                mGoogleSignInPref.setIcon(null);
            }
        }
    }
}
