package com.dlwrasse.events.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dlwrasse.events.helpers.GoogleSignInHelper;
import com.dlwrasse.events.helpers.PremiumHelper;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.Fragment;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.MenuItem;

import com.dlwrasse.events.R;
import com.dlwrasse.events.adapters.EventDateItemDecoration;
import com.dlwrasse.events.adapters.EventDividerItemDecoration;
import com.dlwrasse.events.adapters.EventAdapter;
import com.dlwrasse.events.fragments.NavHeaderFragment;
import com.dlwrasse.events.fragments.NoEventFragment;
import com.dlwrasse.events.fragments.SearchFragment;
import com.dlwrasse.events.helpers.ActionModeHelper;
import com.dlwrasse.events.helpers.GoogleDriveAppFolderHelper;
import com.dlwrasse.events.helpers.TagMenuHelper;
import com.dlwrasse.events.interfaces.OnViewHolderInteractionListener;
import com.dlwrasse.events.persistence.db.entity.Event;
import com.dlwrasse.events.persistence.db.entity.utils.Tag;
import com.dlwrasse.events.persistence.viewmodel.EventListViewModel;
import com.dlwrasse.events.persistence.viewmodel.TagListViewModel;
import com.dlwrasse.events.utils.EventDateUtils;
import com.dlwrasse.events.utils.IntentUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements SearchFragment.OnSearchTextChangeListener, PremiumHelper.OnPremiumUpdateListener,
        GoogleDriveAppFolderHelper.OnDataUploadListener, ActionModeHelper.OnActionModeListener,
        OnViewHolderInteractionListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        View.OnClickListener, NavigationView.OnNavigationItemSelectedListener,
        NavHeaderFragment.OnGoogleSignInUpdateListener {
    public static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_CREATE_EVENT = 0;
    public static final int REQUEST_CODE_PREFS = 1;
    private static final int REQUEST_CODE_GOOGLE_SIGN_IN = 2;
    public static final String KEY_RECREATE = "dlwrasse.events.recreate";
    public static final String KEY_BACKUP = "dlwrasse.events.backup";

    private EventAdapter mAdapter;
    private EventListViewModel mEventListViewModel;

    private TagMenuHelper mTagMenuHelper;
    private ActionModeHelper mActionModeHelper;
    private GoogleDriveAppFolderHelper mGoogleDriveHelper;
    private PremiumHelper mPremiumHelper;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavView;

    private boolean mBackToMain = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) return;

        if (requestCode == REQUEST_CODE_PREFS) {
            if (data.getBooleanExtra(KEY_RECREATE, false)) {
                restart();
            }else if (data.getBooleanExtra(KEY_BACKUP, false)) {

                if (isPremium()) { // from gsi flow
                    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                    if (GoogleSignInHelper.isAccountValid(account)) {

                        mGoogleDriveHelper.loadFromDrive(account);
                    }
                }else { // from backup bt or premium offer dialog
                    mPremiumHelper.launchPremiumPurchase();
                }
            }
        }else if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN) {
            GoogleSignInAccount account = GoogleSignInHelper.getAccount(data);
            if (GoogleSignInHelper.isAccountValid(account)) {
                NavHeaderFragment navHeaderFragment = (NavHeaderFragment)
                        getSupportFragmentManager().findFragmentById(R.id.fragment_navHeaderContent);
                if (navHeaderFragment != null) {
                    navHeaderFragment.updateLayout(account);
                }

                if (isPremium()) {
                    mGoogleDriveHelper.loadFromDrive(account);
                }else {
                    showPremiumDialog(account.getGivenName());
                }
            }
        }

    }

    private void showPremiumDialog(String name) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder
                .setView(getLayoutInflater().inflate(R.layout.dialog_premium, null))
                .setTitle(getString(R.string.dialog_title_premiumOffer, name))
                .setMessage(R.string.dialog_message_premiumOffer)
                .setPositiveButton(R.string.dialog_positive_premiumOffer,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mPremiumHelper.launchPremiumPurchase();
                            }
                        })
                .setNeutralButton(R.string.dialog_negative_premiumOffer,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {}
                        });
        dialogBuilder.create().show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Views
        setActionBar();
        mDrawerLayout = findViewById(R.id.drawer);
        mNavView = findViewById(R.id.nav);
        mNavView.setNavigationItemSelectedListener(this);

        mActionModeHelper = new ActionModeHelper(this);
        mActionModeHelper.setOnActionModeListener(this);
        mAdapter = new EventAdapter(this, mActionModeHelper);
        setRecyclerView();

        findViewById(R.id.fab_add).setOnClickListener(this);

        // Helpers
        mTagMenuHelper = new TagMenuHelper(this, mNavView.getMenu().findItem(R.id.item_tags).getSubMenu());
        String dbFileName = getString(R.string.database_name);
        mGoogleDriveHelper = new GoogleDriveAppFolderHelper(this,
                getDatabasePath(dbFileName).getAbsolutePath(), dbFileName, this);
        mPremiumHelper = new PremiumHelper(this, this);

        // region View Model
        mEventListViewModel = ViewModelProviders.of(this).get(EventListViewModel.class);
        mEventListViewModel.getEventsByTimestamp().observe(this, new Observer<List<Event>>() {
            @Override
            public void onChanged(@Nullable List<Event> eventList) {
                setEventList(eventList);
            }
        });
        ViewModelProviders.of(this).get(TagListViewModel.class).getTags()
                .observe(this, new Observer<List<Tag>>() {
            @Override
            public void onChanged(@Nullable List<Tag> tagList) {
                setTagList(tagList);
            }
        });
        // endregion

        // Fragments
        SearchFragment searchFragment = new SearchFragment();
        getSupportFragmentManager().beginTransaction().add(searchFragment, SearchFragment.TAG).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTitle();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (shouldSyncWithDrive()) {
            mGoogleDriveHelper.saveToDrive(GoogleSignIn.getLastSignedInAccount(this));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mBackToMain) {
            onNavigationItemSelected(mNavView.getMenu().findItem(R.id.item_main));
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public void onSearchTextChanged(String text) {
        mAdapter.getFilter().filter(text);
    }

    private void startContactEmailActivity() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(IntentUtils.getEmailUri(getString(R.string.contact_email_address),
                getString(R.string.contact_email_subject)));
        startActivity(intent);
    }

    private void startAppPageActivity() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(IntentUtils.getAppMarketPageUri(getPackageName()));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            intent.setData(IntentUtils.getAppPageUri(getPackageName()));
            startActivity(intent);
        }
    }

    //region Google Sign In
    @Override
    public void onGoogleSignInClicked() {
        startActivityForResult(GoogleSignInHelper.getSignInIntent(this),
                REQUEST_CODE_GOOGLE_SIGN_IN);
    }
    @Override
    public void onBackupButtonClicked() {
        mPremiumHelper.launchPremiumPurchase();
    }

    private boolean shouldSyncWithDrive() {
        return mAdapter.getFullListCount() != 0 && isPremium();
    }
    //endregion

    //region Premium
    @Override
    public void onPremiumUpdate(boolean premium) {
        setPremium(premium);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (!GoogleSignInHelper.isAccountValid(account)) {
            startActivityForResult(GoogleSignInHelper.getSignInIntent(this),
                    REQUEST_CODE_GOOGLE_SIGN_IN);
            return;
        }
        mGoogleDriveHelper.loadFromDrive(account);
    }
    @Override
    public void onPremiumStatus(boolean premium) {
        setPremium(premium);
    }

    private void setPremium(boolean premium) {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putBoolean(getString(R.string.pref_key_premium), premium).apply();

        NavHeaderFragment navHeaderFragment = (NavHeaderFragment) getSupportFragmentManager().
                findFragmentById(R.id.fragment_navHeaderContent);
        if (navHeaderFragment != null) {
            navHeaderFragment.updatePremium(premium);
        }
    }
    //endregion

    // region SharedPrefs
    private boolean isPremium() {
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.pref_key_premium), false);
    }
    // endregion

    // region GoogleDriveDataUploadListener
    @Override
    public void onDataLoaded(boolean success) {
        restart();
    }

    private void restart() {
        startActivity(getIntent());
        finish();
        Runtime.getRuntime().exit(0);
    }

    @Override
    public void onDataSaved(boolean success) {

    }
    // endregion

    // region ActionModeHelper
    @Override
    public void onActionModeDeleteClicked() {
        mEventListViewModel.delete(mAdapter.getSelectedIds());
    }
    @Override
    public void onActionModeDestroyed() {
        mAdapter.endMultiSelect();
        onNavigationItemSelected(mNavView.getMenu().findItem(R.id.item_main));
    }
    // endregion

    // region OnViewHolderInteractionListener
    @Override
    public void onViewHolderItemClick(int position) {
        Intent intent = new Intent(MainActivity.this, TimelineActivity.class);
        intent.putExtra(TimelineActivity.EXTRA_POS, position);
        startActivity(intent);
    }
    // endregion

    // region OnClickListener
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_add:
                Intent intent = new Intent(MainActivity.this, CreateEventActivity.class);
                startActivityForResult(intent, REQUEST_CODE_CREATE_EVENT);
                break;
            default:
                break;
        }
    }
    // endregion

    // region OnNavigationItemSelectedListener
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        mDrawerLayout.closeDrawers();

        mBackToMain = false;
        switch (item.getItemId()) {
            case R.id.item_main:
                item.setChecked(true);
                updateTitle();
                mAdapter.clearFilter();
                break;
            case R.id.item_rate:
                startAppPageActivity();
                break;
            case R.id.item_contact:
                startContactEmailActivity();
                break;
            case R.id.item_prefs:
                startActivityForResult(new Intent(MainActivity.this, PreferencesActivity.class), REQUEST_CODE_PREFS);
                break;
            default:
                if (item.getGroupId() == R.id.group_tags) {
                    mBackToMain = true;

                    item.setChecked(true);

                    setTitle(item.getTitle().toString());
                    mAdapter.filterByTag(item.getOrder());
                }
                break;
        }
        return true;
    }
    // endregion

    // region Action Bar
    private void setActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_outline_menu_24px);
        }
    }

    private void updateTitle() {
        setTitle(R.string.menu_all);
    }
    // endregion

    private void setRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);

        Drawable divider = getResources().getDrawable(R.drawable.divider_padding_top); // android.R.drawable.divider_horizontal_bright
        recyclerView.addItemDecoration(new EventDividerItemDecoration(divider));
        recyclerView.addItemDecoration(new EventDateItemDecoration(getResources(), recyclerView));
    }

    // region View Model
    private void setEventList(List<Event> eventList) {
        if (eventList != null && eventList.isEmpty()) {
            getSupportFragmentManager().beginTransaction().replace(R.id.layout_no_content,
                    new NoEventFragment(), NoEventFragment.TAG).commit();
        }else {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(NoEventFragment.TAG);
            if (fragment != null) {
                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            }
        }
        mAdapter.setEventList(eventList);
    }

    private void setTagList(List<Tag> tagList) {
        mAdapter.setTagList(tagList);
        mTagMenuHelper.addTagsToMenu(tagList);
    }
    // endregion
}