package com.bassamalim.athkar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bassamalim.athkar.adapters.AlathkarAdapter;
import com.bassamalim.athkar.databinding.AlathkarListActivityBinding;
import com.bassamalim.athkar.models.AlathkarButton;
import com.bassamalim.athkar.views.AlathkarView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class AlathkarList extends AppCompatActivity {

    private AlathkarListActivityBinding binding;
    private boolean all;
    private RecyclerView recyclerView;
    private AlathkarAdapter adapter;
    private ArrayList<AlathkarButton> alathkarButtons;
    private JSONArray mainArray;
    private JSONObject category;
    private int index;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = AlathkarListActivityBinding.inflate(getLayoutInflater());

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(binding.getRoot());
        setSupportActionBar(binding.nameBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        index = getIntent().getIntExtra("index", 0);
        if (index == 0)
            all = true;

        setupJson();

        binding.categoryName.setText(name);

        if (all)
            alathkarButtons = makeAllButtons();
        else
            alathkarButtons = makeAlathkarButtons();

        setupRecycler();

        setSearchListeners();
    }

    private void setupJson() {
        String json = Utils.getJsonFromAssets(this, "alathkar.json");
        try {
            assert json != null;
            mainArray = new JSONArray(json);
            category = mainArray.getJSONObject(index);
            name = category.getString("category_name_ar");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<AlathkarButton> makeAlathkarButtons() {
        ArrayList<AlathkarButton> buttons = new ArrayList<>();

        try {
            JSONArray array = category.getJSONArray("alathkar");

            for (int i = 0; i < array.length(); i++) {
                JSONObject alathkar = array.getJSONObject(i);
                String name = alathkar.getString("name_ar");

                int finalI = i;
                View.OnClickListener clickListener = v -> {
                    Intent intent = new Intent(this, AlathkarView.class);
                    intent.putExtra("category", index);
                    intent.putExtra("thikrs_index", finalI);
                    startActivity(intent);
                };

                AlathkarButton button = new AlathkarButton(finalI, name, clickListener);
                buttons.add(button);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "Problems in making alathkar buttons");
        }
        return buttons;
    }

    private ArrayList<AlathkarButton> makeAllButtons() {
        ArrayList<AlathkarButton> buttons = new ArrayList<>();

        try {
            for (int i = 1; i < mainArray.length(); i++) {
                JSONObject category = mainArray.getJSONObject(i);

                JSONArray array = category.getJSONArray("alathkar");

                for (int j = 0; j < array.length(); j++) {
                    JSONObject alathkar = array.getJSONObject(j);
                    String name = alathkar.getString("name_ar");

                    int finalI = i;
                    int finalJ = j;
                    View.OnClickListener clickListener = v -> {
                        Intent intent = new Intent(this, AlathkarView.class);
                        intent.putExtra("category", finalI);
                        intent.putExtra("thikrs_index", finalJ);
                        startActivity(intent);
                    };

                    AlathkarButton button = new AlathkarButton(finalJ, name, clickListener);
                    buttons.add(button);
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "Problems in making alathkar buttons");
        }
        return buttons;
    }

    private void setupRecycler() {
        recyclerView = findViewById(R.id.alathkar_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new AlathkarAdapter(alathkarButtons);
        recyclerView.setAdapter(adapter);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
        recyclerView.setAdapter(null);
        adapter = null;
    }
}
