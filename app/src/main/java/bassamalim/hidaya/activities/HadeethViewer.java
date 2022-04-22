package bassamalim.hidaya.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;

import bassamalim.hidaya.adapters.HadeethViewerAdapter;
import bassamalim.hidaya.databinding.ActivityHadeethViewerBinding;
import bassamalim.hidaya.models.HadeethBook;
import bassamalim.hidaya.models.HadeethDoorCard;
import bassamalim.hidaya.other.Utils;

public class HadeethViewer extends AppCompatActivity {

    private ActivityHadeethViewerBinding binding;
    private int bookId;
    private int chapterId;
    private HadeethBook.BookChapter.BookDoor[] doors;
    private boolean[] favs;
    private RecyclerView recycler;
    private HadeethViewerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        binding = ActivityHadeethViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.home.setOnClickListener(v -> onBackPressed());


        Intent intent = getIntent();
        bookId = intent.getIntExtra("book_id", 0);
        chapterId = intent.getIntExtra("chapter_id", 0);
        binding.topBarTitle.setText(intent.getStringExtra("book_title"));

        getData();

        setupRecycler();
    }

    private void getData() {
        String path = getExternalFilesDir(null) + "/Hadeeth Downloads/" + bookId  + ".json";
        String jsonStr = Utils.getJsonFromDownloads(path);
        Gson gson = new Gson();
        HadeethBook book = gson.fromJson(jsonStr, HadeethBook.class);
        doors = book.getChapters()[chapterId].getDoors();

        String favsStr = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("book" + bookId + "_chapter" + chapterId + "_favs", "");
        if (favsStr.length() == 0)
            favs = new boolean[doors.length];
        else
            favs = gson.fromJson(favsStr, boolean[].class);
    }

    private ArrayList<HadeethDoorCard> makeCards() {
        ArrayList<HadeethDoorCard> cards = new ArrayList<>();
        for (int i = 0; i < doors.length; i++) {
            HadeethBook.BookChapter.BookDoor door = doors[i];
            cards.add(new HadeethDoorCard(door.getDoorId(), door.getDoorTitle(),
                    door.getText(), favs[i]));
        }
        return cards;
    }

    private void setupRecycler() {
        recycler = binding.recycler;
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);
        adapter = new HadeethViewerAdapter(this, makeCards(), bookId, chapterId);
        recycler.setAdapter(adapter);
    }

    public void onDestroy() {
        super.onDestroy();
        binding = null;
        recycler.setAdapter(null);
        adapter = null;
    }
}