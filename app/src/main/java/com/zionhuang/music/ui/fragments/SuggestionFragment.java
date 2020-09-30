package com.zionhuang.music.ui.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jakewharton.rxbinding4.appcompat.RxSearchView;
import com.zionhuang.music.R;
import com.zionhuang.music.ui.adapters.SearchSuggestionAdapter;
import com.zionhuang.music.ui.fragments.SuggestionFragmentDirections.ActionSuggestionFragmentToSearchResultFragment;
import com.zionhuang.music.ui.widgets.RecyclerViewClickManager;
import com.zionhuang.music.viewmodels.SuggestionViewModel;

import java.util.concurrent.TimeUnit;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;

import static autodispose2.AutoDispose.autoDisposable;
import static com.zionhuang.music.ui.fragments.SuggestionFragmentDirections.actionSuggestionFragmentToSearchResultFragment;

public class SuggestionFragment extends BaseFragment {
    private static final String TAG = "SearchSuggestionFragment";
    private SuggestionViewModel mViewModel;
    private SearchView mSearchView;

    @Override
    protected int layoutId() {
        return R.layout.fragment_search_suggestion;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SuggestionViewModel.class);

        RecyclerView recyclerView = findViewById(R.id.suggestion_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        SearchSuggestionAdapter adapter = new SearchSuggestionAdapter(mViewModel);
        recyclerView.setAdapter(adapter);
        RecyclerViewClickManager.setup(recyclerView, (i, v) -> {
            mSearchView.clearFocus();
            ActionSuggestionFragmentToSearchResultFragment action = actionSuggestionFragmentToSearchResultFragment(adapter.getQueryByPosition(i));
            NavHostFragment.findNavController(this).navigate(action);
        }, null);

        setupSearchView();
        mViewModel.onFillQuery().observe(getViewLifecycleOwner(), q -> mSearchView.setQuery(q, false));
        mViewModel.getQuery().observe(getViewLifecycleOwner(), mViewModel::fetchSuggestions);
        mViewModel.getSuggestions().observe(getViewLifecycleOwner(), adapter::setDataSet);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_search_view, menu);
        MenuItem searchItem = menu.findItem(R.id.search_view);
        searchItem.setActionView(mSearchView);
    }

    private void setupSearchView() {
        mSearchView = new SearchView(requireContext());
        mSearchView.setIconified(false);
        SearchManager searchManager = (SearchManager) requireContext().getSystemService(Context.SEARCH_SERVICE);
        if (searchManager != null) {
            mSearchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));
        }
        mSearchView.setSubmitButtonEnabled(false);
        mSearchView.setMaxWidth(Integer.MAX_VALUE);
        mSearchView.setOnCloseListener(() -> true);
        EditText searchEditText = mSearchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setPadding(0, 2, 0, 2);
        RxSearchView.queryTextChanges(mSearchView)
                .debounce(100, TimeUnit.MILLISECONDS)
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(q -> mViewModel.setQuery(q.toString()));
        mSearchView.setQuery(mViewModel.getQuery().getValue(), false);
    }
}