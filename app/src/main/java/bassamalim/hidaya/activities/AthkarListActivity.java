package bassamalim.hidaya.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;

import bassamalim.hidaya.R;
import bassamalim.hidaya.adapters.AthkarListAdapter;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.database.dbs.AthkarDB;
import bassamalim.hidaya.database.dbs.ThikrsDB;
import bassamalim.hidaya.databinding.ActivityAthkarListBinding;
import bassamalim.hidaya.models.AthkarItem;
import bassamalim.hidaya.other.Global;
import bassamalim.hidaya.other.Utils;

public class AthkarListActivity extends AppCompatActivity {

    private ActivityAthkarListBinding binding;
    private RecyclerView recyclerView;
    private AthkarListAdapter adapter;
    private int category;
    private String action;
    private AppDatabase db;
    private String language;
    private List<String> names;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        language = Utils.onActivityCreateSetLocale(this);
        binding = ActivityAthkarListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.home.setOnClickListener(v -> onBackPressed());

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,
                "HidayaDB").createFromAsset("databases/HidayaDB.db").allowMainThreadQueries()
                .build();

        action = getIntent().getAction();
        if (action.equals("all"))
            binding.topBarTitle.setText(getString(R.string.all_athkar));
        else if (action.equals("favorite"))
            binding.topBarTitle.setText(getString(R.string.favorite_athkar));
        else {
            category = getIntent().getIntExtra("category", 0);

            String topBarTitle = language.equals("en") ? db.athkarCategoryDao()
                    .getNameEn(category) : db.athkarCategoryDao().getName(category);
            binding.topBarTitle.setText(topBarTitle);
        }

        setupRecycler();

        setSearchListeners();
    }

    private List<AthkarDB> getData() {
        names = language.equals("en") ? db.athkarDao().getNamesEn() : db.athkarDao().getNames();

        switch (action) {
            case "all": return db.athkarDao().getAll();
            case "favorite": return db.athkarDao().getFavorites();
            default: return db.athkarDao().getList(category);
        }
    }

    private List<AthkarItem> makeButtons(List<AthkarDB> athkar) {
        Log.d(Global.TAG, "SIZE: " + athkar.size());
        List<AthkarItem> buttons = new ArrayList<>();

        List<Integer> favs = db.athkarDao().getFavs();

        for (int i = 0; i < athkar.size(); i++) {
            AthkarDB thikr = athkar.get(i);

            if (language.equals("en") && !hasEn(thikr))
                continue;

            View.OnClickListener clickListener = v -> {
                Intent intent = new Intent(this, AthkarViewer.class);
                intent.setAction(action);
                intent.putExtra("thikr_id", thikr.getAthkar_id());
                startActivity(intent);
            };

            int fav = action.equals("favorite") ? 1 : favs.get(thikr.getAthkar_id());

            buttons.add(new AthkarItem(thikr.getAthkar_id(), thikr.getCategory_id(),
                    names.get(thikr.getAthkar_id()), fav, clickListener));
        }
        return buttons;
    }

    private boolean hasEn(AthkarDB thikr) {
        List<ThikrsDB> ts = db.thikrsDao().getThikrs(thikr.getAthkar_id());
        for (int i = 0; i < ts.size(); i++) {
            ThikrsDB t = ts.get(i);
            if (t.getText_en() != null && t.getText_en().length() > 1)
                return true;
        }
        return false;
    }

    private void setupRecycler() {
        recyclerView = findViewById(R.id.recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new AthkarListAdapter(this, makeButtons(getData()));
        recyclerView.setAdapter(adapter);
    }

    private void setSearchListeners() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
