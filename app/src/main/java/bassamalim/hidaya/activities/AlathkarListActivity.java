package bassamalim.hidaya.activities;

import android.content.Intent;
import android.os.Bundle;
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
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.database.dbs.AthkarDB;
import bassamalim.hidaya.databinding.ActivityAlathkarListBinding;
import bassamalim.hidaya.models.AlathkarButton;

public class AlathkarListActivity extends AppCompatActivity {

    private ActivityAlathkarListBinding binding;
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
        binding = ActivityAlathkarListBinding.inflate(getLayoutInflater());

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
        else if (action.equals("favorite"))
            binding.categoryName.setText("الأذكار المفضلة");
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
        switch (action) {
            case "all": return db.athkarDao().getAll();
            case "favorite": return db.athkarDao().getFavorites();
            default: return db.athkarDao().getList(category);
        }
    }

    private ArrayList<AlathkarButton> makeButtons() {
        ArrayList<AlathkarButton> buttons = new ArrayList<>();

        List<Integer> favs = db.athkarDao().getFavs();

        for (int i = 0; i < athkar.size(); i++) {
            int cat = category, id = i;
            if (action.equals("all") || action.equals("favorite")) {
                cat = athkar.get(i).getCategory_id();
                id = athkar.get(i).getAthkar_id();
            }

            int finalCat = cat;
            int finalId = id;
            View.OnClickListener clickListener = v -> {
                Intent intent = new Intent(this, AlathkarActivity.class);
                intent.setAction(action);
                intent.putExtra("category", finalCat);
                intent.putExtra("thikrs_index", finalId);
                startActivity(intent);
            };
            buttons.add(new AlathkarButton(id, cat, athkar.get(i).getAthkar_name(),
                    favs.get(i), clickListener));
        }
        return buttons;
    }

    private void setupRecycler() {
        recyclerView = findViewById(R.id.alathkar_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new AlathkarAdapter(this, alathkarButtons);
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
