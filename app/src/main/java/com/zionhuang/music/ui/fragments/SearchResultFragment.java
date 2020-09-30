package com.zionhuang.music.ui.fragments;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.api.services.youtube.model.SearchResult;
import com.zionhuang.music.R;
import com.zionhuang.music.models.SongParcel;
import com.zionhuang.music.ui.adapters.SearchResultAdapter;
import com.zionhuang.music.ui.widgets.RecyclerViewClickManager;
import com.zionhuang.music.viewmodels.PlaybackViewModel;
import com.zionhuang.music.viewmodels.SearchViewModel;

import java.util.Objects;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;

import static autodispose2.AutoDispose.autoDisposable;

public class SearchResultFragment extends BaseFragment {
    private static final String TAG = "SearchResultFragment";
    private PlaybackViewModel mPlaybackViewModel;

    @Override
    protected int layoutId() {
        return R.layout.fragment_search_result;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SearchViewModel mSearchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        mPlaybackViewModel = new ViewModelProvider(requireActivity()).get(PlaybackViewModel.class);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());

        RecyclerView recyclerView = findViewById(R.id.result_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        SearchResultAdapter adapter = new SearchResultAdapter();
        recyclerView.setAdapter(adapter);
        RecyclerViewClickManager.setup(recyclerView, (i, v) -> {
            SearchResult item = adapter.getItemByPosition(i);
            if (!"youtube#video".equals(item.getId().getKind())) {
                return;
            }
            mPlaybackViewModel.playMedia(SongParcel.fromSearchResult(item));
        }, null);

        String query = SearchResultFragmentArgs.fromBundle(requireArguments()).getSearchQuery();
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(query);
        mSearchViewModel.search(query)
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(pagingData -> adapter.submitData(getLifecycle(), pagingData));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_search_icon, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            NavHostFragment.findNavController(this).navigate(R.id.action_searchResultFragment_to_searchSuggestionFragment);
        }
        return true;
    }
}