package com.dlwrasse.events.activities;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import com.dlwrasse.events.persistence.db.virtual.DatabaseTable;
import com.dlwrasse.events.adapters.SearchAdapter;

public class SearchableActivity extends ListActivity {
    private SearchAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            DatabaseTable db = new DatabaseTable(this);
            Cursor cursor = db.getTextMatches(query, null);

            mAdapter = new SearchAdapter(getApplicationContext(), cursor, 0);
            setListAdapter(mAdapter);
        }
    }
}
