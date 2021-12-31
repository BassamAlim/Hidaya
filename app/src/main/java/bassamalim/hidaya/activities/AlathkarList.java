package bassamalim.hidaya.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import bassamalim.hidaya.R;
import bassamalim.hidaya.adapters.AlathkarAdapter;
import bassamalim.hidaya.databinding.AlathkarListActivityBinding;
import bassamalim.hidaya.models.AlathkarButton;
import bassamalim.hidaya.database.AthkarDB;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.other.Constants;

public class AlathkarList extends AppCompatActivity {

    private AlathkarListActivityBinding binding;
    private RecyclerView recyclerView;
    private AlathkarAdapter adapter;
    private ArrayList<AlathkarButton> alathkarButtons;
    private int category;
    private String action;
    private AppDatabase db;
    private List<AthkarDB> athkar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AlathkarListActivityBinding.inflate(getLayoutInflater());

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(binding.getRoot());
        setSupportActionBar(binding.nameBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,
                "HidayaDB").createFromAsset("databases/HidayaDB.db").allowMainThreadQueries()
                .build();

        action = getIntent().getAction();
        if (action.equals("all"))
            binding.categoryName.setText("جميع الاذكار");
        else {
            category = getIntent().getIntExtra("category", 0);
            binding.categoryName.setText(db.athkarCategoryDao().getName(category));
        }

        athkar = getData();

        alathkarButtons = makeButtons();

        setupRecycler();

        setSearchListeners();
    }

    private List<AthkarDB> getData() {
        if (action.equals("all"))
            return db.athkarDao().getAll();
        else
            return db.athkarDao().getList(category);
    }

    private ArrayList<AlathkarButton> makeButtons() {
        ArrayList<AlathkarButton> buttons = new ArrayList<>();
        for (int i = 0; i < athkar.size(); i++) {

            int cat = category;
            int index = i;
            if (action.equals("all")) {
                cat = athkar.get(i).getCategory_id();
                index = athkar.get(i).getAthkar_id();
            }

            int finalCat = cat;
            int finalIndex = index;
            View.OnClickListener clickListener = v -> {
                Intent intent = new Intent(this, AlathkarActivity.class);
                intent.setAction(action);

                Log.i(Constants.TAG, "its: " + category);

                intent.putExtra("category", finalCat);
                intent.putExtra("thikrs_index", finalIndex);
                startActivity(intent);
            };
            buttons.add(new AlathkarButton(i, athkar.get(i).getAthkar_name(), clickListener));
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
