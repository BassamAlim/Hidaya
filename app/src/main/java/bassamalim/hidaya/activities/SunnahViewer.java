package bassamalim.hidaya.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Objects;

import bassamalim.hidaya.adapters.SunnahViewerAdapter;
import bassamalim.hidaya.databinding.ActivitySunnahViewerBinding;
import bassamalim.hidaya.models.SunnahBook;
import bassamalim.hidaya.models.SunnahDoorCard;
import bassamalim.hidaya.other.Utils;

public class SunnahViewer extends AppCompatActivity {

    private ActivitySunnahViewerBinding binding;
    private int bookId;
    private int chapterId;
    private SunnahBook.BookChapter.BookDoor[] doors;
    private boolean[] favs;
    private RecyclerView recycler;
    private SunnahViewerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        binding = ActivitySunnahViewerBinding.inflate(getLayoutInflater());
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        Intent intent = getIntent();
        bookId = intent.getIntExtra("book_id", 0);
        chapterId = intent.getIntExtra("chapter_id", 0);
        binding.topBarTitle.setText(intent.getStringExtra("book_title"));

        getData();

        setupRecycler();
    }

    private void getData() {
        String path = getExternalFilesDir(null) + "/Sunnah Downloads/" + bookId  + ".json";
        String jsonStr = Utils.getJsonFromDownloads(path);
        Gson gson = new Gson();
        SunnahBook book = gson.fromJson(jsonStr, SunnahBook.class);
        doors = book.getChapters()[chapterId].getDoors();

        String favsStr = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("book" + bookId + "_chapter" + chapterId + "_favs", "");
        if (favsStr.length() == 0)
            favs = new boolean[doors.length];
        else
            favs = gson.fromJson(favsStr, boolean[].class);
    }

    private ArrayList<SunnahDoorCard> makeCards() {
        ArrayList<SunnahDoorCard> cards = new ArrayList<>();
        for (int i = 0; i < doors.length; i++) {
            SunnahBook.BookChapter.BookDoor door = doors[i];
            cards.add(new SunnahDoorCard(door.getDoorId(), door.getDoorTitle(),
                    door.getText(), favs[i]));
        }
        return cards;
    }

    private void setupRecycler() {
        recycler = binding.recycler;
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);
        adapter = new SunnahViewerAdapter(this, makeCards(), bookId, chapterId);
        recycler.setAdapter(adapter);
    }

    public void onDestroy() {
        super.onDestroy();
        binding = null;
        recycler.setAdapter(null);
        adapter = null;
    }
}