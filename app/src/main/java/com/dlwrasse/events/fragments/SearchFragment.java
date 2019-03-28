package com.dlwrasse.events.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;

import com.dlwrasse.events.R;

public class SearchFragment extends Fragment {
    public static final String TAG = "SearchFragment";

    private OnSearchTextChangeListener mListener;

    private final SearchView.OnQueryTextListener mOnQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            mListener.onSearchTextChanged(query);
            return false;
        }

        @Override
        public boolean onQueryTextChange(String query) {
            mListener.onSearchTextChanged(query);
            return false;
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (OnSearchTextChangeListener) context;
        }catch (ClassCastException e) {
            throw new ClassCastException(getString(R.string.error_mustImplement,
                    context.toString(), "OnSearchTextChangeListener"));
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.fragment_search, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.item_search).getActionView();
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(mOnQueryTextListener);
    }

    public interface OnSearchTextChangeListener {
        void onSearchTextChanged(String text);
    }
}
