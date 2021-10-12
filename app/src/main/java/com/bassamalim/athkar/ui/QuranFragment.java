package com.bassamalim.athkar.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bassamalim.athkar.Constants;
import com.bassamalim.athkar.helpers.Utils;
import com.bassamalim.athkar.adapters.SurahButtonAdapter;
import com.bassamalim.athkar.databinding.QuranFragmentBinding;
import com.bassamalim.athkar.models.SurahButton;
import com.bassamalim.athkar.activities.QuranActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class QuranFragment extends Fragment {

    private QuranFragmentBinding binding;
    private RecyclerView recyclerView;
    private SurahButtonAdapter adapter;
    private ArrayList<SurahButton> surahButtons;
    private static Bundle mBundleRecyclerViewState;
    private Parcelable mListState = null;
    private GridLayoutManager gridLayoutManager;
    private String[] tanzeelArr;

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

        setupContinue();

        if (mBundleRecyclerViewState != null) {
            new Handler().postDelayed(() -> {
                mListState = mBundleRecyclerViewState.getParcelable("recycler_state");
                Objects.requireNonNull(recyclerView.getLayoutManager())
                        .onRestoreInstanceState(mListState);
            }, 50);
        }
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    private void setupContinue() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(requireContext());
        int page = pref.getInt("bookmarked_page", -1);
        String text = pref.getString("bookmarked_text", "");
        if (page == -1)
            text = "لا يوجد صفحة محفوظة";
        else
            text = "الصفحة المحفوظة:  " + text;
        binding.continueReading.setText(text);

        binding.continueReading.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), QuranActivity.class);
            intent.setAction("bookmark");
            intent.putExtra("page", page);
            requireContext().startActivity(intent);
        });
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
        String buttonJson = Utils.getJsonFromAssets(requireContext(), "surah_button.json");
        tanzeelArr = new String[Constants.NUMBER_OF_SURAHS];
        try {
            JSONArray array = new JSONArray(buttonJson);

            for (int i=0; i<Constants.NUMBER_OF_SURAHS; i++) {
                JSONObject obj = array.getJSONObject(i);
                String name = obj.getString("name");
                String searchName = obj.getString("search_name");
                String tanzeel = obj.getString("tanzeel");

                int finalI = i;
                View.OnClickListener clickListener = v -> {
                    Intent intent = new Intent(getContext(), QuranActivity.class);
                    intent.setAction("specific");
                    intent.putExtra("surah_index", finalI);
                    requireContext().startActivity(intent);
                };
                tanzeelArr[i] = tanzeel;
                SurahButton button = new SurahButton(i,"سُورَة " + name,
                        searchName, tanzeel ,clickListener);
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
