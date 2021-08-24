package com.bassamalim.athkar.ui;

import androidx.appcompat.widget.SearchView;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bassamalim.athkar.Constants;
import com.bassamalim.athkar.adapters.MyRecyclerAdapter;
import com.bassamalim.athkar.Utils;
import com.bassamalim.athkar.databinding.QuranFragmentBinding;
import com.bassamalim.athkar.models.SurahButton;
import com.bassamalim.athkar.views.QuranView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class QuranFragment extends Fragment {

    private QuranFragmentBinding binding;
    private JSONObject jsonObject;
    private MyRecyclerAdapter adapter;
    private ArrayList<SurahButton> surahButtons;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = QuranFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupJson();

        surahButtons = makeSurahButtons();

        setupRecycler();

        setSearchListeners();

        return root;
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
        RecyclerView recyclerView = binding.recycler;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MyRecyclerAdapter(surahButtons);
        recyclerView.setAdapter(adapter);
    }

    private void setupJson() {
        String jsonFileString = Utils.getJsonFromAssets(requireContext(), "quran.json");
        try {
            assert jsonFileString != null;
            jsonObject = new JSONObject(jsonFileString);
            JSONObject data = jsonObject.getJSONObject("data");
            JSONArray surahs = data.getJSONArray("surahs");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<SurahButton> makeSurahButtons() {
        ArrayList<SurahButton> buttons = new ArrayList<>();
        String surahNamesJson = Utils.getJsonFromAssets(requireContext(), "surah_names.json");
        try {
            assert surahNamesJson != null;
            jsonObject = new JSONObject(surahNamesJson);
            JSONArray array = jsonObject.getJSONArray("names");
            for (int i=0; i<Constants.NUMBER_OF_SURAHS; i++) {
                JSONObject object = array.getJSONObject(i);

                int finalI = i;
                View.OnClickListener clickListener = v -> {
                    Intent intent = new Intent(getContext(), QuranView.class);
                    intent.putExtra("surah index", finalI);
                    requireContext().startActivity(intent);
                };
                SurahButton button = new SurahButton(i, object.getString("name"), clickListener);
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

    }
}
