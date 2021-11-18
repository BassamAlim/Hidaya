package com.bassamalim.athkar.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bassamalim.athkar.R;
import com.bassamalim.athkar.adapters.ReciterSurahsAdapter;
import com.bassamalim.athkar.databinding.ActivityReciterSurahsBinding;
import com.bassamalim.athkar.helpers.Utils;
import com.bassamalim.athkar.models.ReciterSurahCard;
import com.bassamalim.athkar.other.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class ReciterSurahsActivity extends AppCompatActivity {

    private ActivityReciterSurahsBinding binding;
    private JSONObject reciterObj;
    private RecyclerView recycler;
    private ReciterSurahsAdapter adapter;
    private ArrayList<ReciterSurahCard> cards;
    private int reciterIndex;
    private int version;
    private String availableSurahs;
    private ArrayList<String> surahNames;
    private String[] searchNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReciterSurahsBinding.inflate(getLayoutInflater());

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(binding.getRoot());
        setSupportActionBar(binding.nameBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        reciterIndex = getIntent().getIntExtra("reciter", 0);
        version = getIntent().getIntExtra("version", 0);

        setupJson();

        cards = makeCards();

        setupRecycler();

        setSearchListeners();
    }

    private void setupJson() {
        String RecitersJson = Utils.getJsonFromAssets(this, "mp3quran.json");
        String surahsJson = Utils.getJsonFromAssets(this, "surah_button.json");
        try {
            assert RecitersJson != null;
            JSONArray arr = new JSONArray(RecitersJson);
            reciterObj = arr.getJSONObject(reciterIndex);
            JSONArray versions = reciterObj.getJSONArray("versions");
            availableSurahs = versions.getString(version);

            assert surahsJson != null;
            JSONArray array = new JSONArray(surahsJson);

            surahNames = new ArrayList<>();
            searchNames = new String[114];
            for (int i = 0; i < searchNames.length; i++) {
                JSONObject obj = array.getJSONObject(i);
                surahNames.add(obj.getString("name"));
                searchNames[i] = obj.getString("search_name");
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "Problems in setupJson() in RadioActivity");
        }
    }

    private ArrayList<ReciterSurahCard> makeCards() {
        ArrayList<ReciterSurahCard> cards = new ArrayList<>();
        for (int i = 0; i < 114; i++) {
            if (availableSurahs.contains("," + (i+1) + ",")) {
                String name = surahNames.get(i);
                String searchName = searchNames[i];
                int finalI = i;
                View.OnClickListener listener = v -> {
                    Intent intent = new Intent(this, RadioClient.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("reciter", reciterIndex);
                    intent.putExtra("version", version);
                    intent.putExtra("surah_index", finalI);
                    intent.putStringArrayListExtra("surah_names", surahNames);
                    startActivity(intent);
                };
                cards.add(new ReciterSurahCard(i, name, searchName, listener));
            }
        }
        return cards;
    }

    private void setupRecycler() {
        recycler = findViewById(R.id.reciter_surahs_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);
        adapter = new ReciterSurahsAdapter(cards);
        recycler.setAdapter(adapter);
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

    public void onDestroy() {
        super.onDestroy();
        binding = null;
        recycler.setAdapter(null);
        adapter = null;
    }
}