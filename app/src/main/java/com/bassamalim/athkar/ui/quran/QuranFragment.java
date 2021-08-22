package com.bassamalim.athkar.ui.quran;

import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import android.animation.Animator;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class QuranFragment extends Fragment {

    private QuranViewModel quranViewModel;
    private QuranFragmentBinding binding;
    JSONObject jsonObject;
    JSONObject data;
    JSONArray surahs;
    String jsonFileString;
    MyRecyclerAdapter adapter;
    public static ArrayList<String> surahNames;
    private Animator animator;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        quranViewModel = new ViewModelProvider(this).get(QuranViewModel.class);

        binding = QuranFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupJson();

        surahNames = getSurahNames();

        setupRecycler();

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

        return root;
    }

    private void setupRecycler() {
        RecyclerView recyclerView = binding.recycler;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MyRecyclerAdapter(getContext(), surahNames);
        recyclerView.setAdapter(adapter);

        //divider between buttons
        /*DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);*/
    }

    private void setupJson() {
        jsonFileString = Utils.getJsonFromAssets(requireContext(), "quran.json");
        try {
            assert jsonFileString != null;
            jsonObject = new JSONObject(jsonFileString);
            data = jsonObject.getJSONObject("data");
            surahs = data.getJSONArray("surahs");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getSurahNames() {
        ArrayList<String> names = new ArrayList<>();
        String surahNamesJson = Utils.getJsonFromAssets(requireContext(), "surah_names.json");
        try {
            assert surahNamesJson != null;
            jsonObject = new JSONObject(surahNamesJson);
            JSONArray array = jsonObject.getJSONArray("names");
            for (int i=0; i<Constants.NUMBER_OF_SURAHS; i++) {
                JSONObject object = array.getJSONObject(i);
                names.add(object.getString("name"));
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return names;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
