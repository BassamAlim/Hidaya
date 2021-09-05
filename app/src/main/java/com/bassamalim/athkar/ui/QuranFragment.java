package com.bassamalim.athkar.ui;

import androidx.appcompat.widget.SearchView;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Handler;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bassamalim.athkar.Constants;
import com.bassamalim.athkar.adapters.SurahButtonAdapter;
import com.bassamalim.athkar.Utils;
import com.bassamalim.athkar.databinding.QuranFragmentBinding;
import com.bassamalim.athkar.models.SurahButton;
import com.bassamalim.athkar.views.QuranView;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.Objects;

public class QuranFragment extends Fragment {

    private QuranFragmentBinding binding;
    private SurahButtonAdapter adapter;
    private RecyclerView recyclerView;
    private ArrayList<SurahButton> surahButtons;
    private static Bundle mBundleRecyclerViewState;
    private Parcelable mListState = null;
    GridLayoutManager gridLayoutManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        gridLayoutManager = new GridLayoutManager(getContext(), 1);

        binding = QuranFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        surahButtons = makeSurahButtons();

        setupRecycler();

        setSearchListeners();

        return root;
    }

    @Override
    public void onPause() {
        super.onPause();
        mBundleRecyclerViewState = new Bundle();
        mListState = Objects.requireNonNull(recyclerView.getLayoutManager()).onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable("recycler_state", mListState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBundleRecyclerViewState != null) {
            new Handler().postDelayed(() -> {
                mListState = mBundleRecyclerViewState.getParcelable("recycler_state");
                Objects.requireNonNull(recyclerView.getLayoutManager())
                        .onRestoreInstanceState(mListState);
            }, 50);
        }
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    private void setSearchListeners() {
        binding.searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
    }

    private void setupRecycler() {
        recyclerView = binding.recycler;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SurahButtonAdapter(surahButtons);
        recyclerView.setAdapter(adapter);
    }

    public ArrayList<SurahButton> makeSurahButtons() {
        ArrayList<SurahButton> buttons = new ArrayList<>();
        String surahNamesJson = Utils.getJsonFromAssets(requireContext(), "sura_names.json");
        String searchJson = Utils.getJsonFromAssets(requireContext(), "search_names.json");
        try {
            JSONArray namesArray = new JSONArray(surahNamesJson);
            JSONArray searchNames = new JSONArray(searchJson);

            for (int i=0; i<Constants.NUMBER_OF_SURAHS; i++) {
                String name = namesArray.getString(i);
                String searchName = searchNames.getString(i);

                int finalI = i;
                View.OnClickListener clickListener = v -> {
                    Intent intent = new Intent(getContext(), QuranView.class);
                    intent.setAction("specific");
                    intent.putExtra("surah_index", finalI);
                    requireContext().startActivity(intent);
                };
                SurahButton button = new SurahButton(i,"سُورَة " + name, searchName ,clickListener);
                buttons.add(button);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return buttons;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        recyclerView.setAdapter(null);
        adapter = null;
    }
}
