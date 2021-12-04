package com.bassamalim.hidaya.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bassamalim.hidaya.R;
import com.bassamalim.hidaya.adapters.RadioRecitersAdapter;
import com.bassamalim.hidaya.databinding.ActivityRadioBinding;
import com.bassamalim.hidaya.helpers.Utils;
import com.bassamalim.hidaya.models.RecitationVersion;
import com.bassamalim.hidaya.models.ReciterCard;
import com.bassamalim.hidaya.other.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class RadioActivity extends AppCompatActivity {

    private ActivityRadioBinding binding;
    private JSONArray arr;
    private RecyclerView recycler;
    private RadioRecitersAdapter adapter;
    private ArrayList<ReciterCard> cards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRadioBinding.inflate(getLayoutInflater());

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(binding.getRoot());
        setSupportActionBar(binding.nameBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        setupJson();

        cards = makeCards();

        setupRecycler();

        setSearchListeners();
    }

    private void setupJson() {
        String json = Utils.getJsonFromAssets(this, "mp3quran.json");
        try {
            assert json != null;
            arr = new JSONArray(json);
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "Problems in setupJson() in RadioActivity");
        }
    }

    private ArrayList<ReciterCard> makeCards() {
        ArrayList<ReciterCard> cards = new ArrayList<>();
        try {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject reciter = arr.getJSONObject(i);
                String name = reciter.getString("name");
                JSONArray versions = reciter.getJSONArray("versions");
                RecitationVersion[] versionsArr = new RecitationVersion[versions.length()];
                for (int j = 0; j < versions.length(); j++) {
                    JSONObject ver = versions.getJSONObject(j);
                    int finalI = i, finalJ = j;
                    View.OnClickListener listener = v -> {
                        Intent intent = new Intent(v.getContext(), SurahsActivity.class);
                        intent.putExtra("reciter", finalI);
                        intent.putExtra("version", finalJ);
                        startActivity(intent);
                    };
                    RecitationVersion obj = new RecitationVersion(j, ver.getString("server"),
                            ver.getString("rewaya"), ver.getString("count"),
                            ver.getString("suras"), listener);
                    versionsArr[j] = obj;
                }
                cards.add(new ReciterCard(i, name, versionsArr));
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "Problems in makeCards() in RadioActivity");
        }
        return cards;
    }

    private void setupRecycler() {
        recycler = findViewById(R.id.radio_reciters_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);
        adapter = new RadioRecitersAdapter(cards);
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