package bassamalim.hidaya.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import bassamalim.hidaya.activities.TelawatClient;
import bassamalim.hidaya.adapters.TelawatSuarAdapter;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.database.dbs.SuraDB;
import bassamalim.hidaya.databinding.FragmentTelawatSuarBinding;
import bassamalim.hidaya.enums.ListType;
import bassamalim.hidaya.models.ReciterSuraCard;

public class TelawatSuarFragment extends Fragment {

    private FragmentTelawatSuarBinding binding;
    private RecyclerView recycler;
    private TelawatSuarAdapter adapter;
    private int reciterId;
    private int versionId;
    private ListType type;
    private String availableSurahs;
    private ArrayList<String> surahNames;
    private String[] searchNames;
    private List<Integer> favs;
    private boolean[] downloaded;

    public TelawatSuarFragment() {}

    public TelawatSuarFragment(ListType type, int reciterId, int versionId) {
        this.type = type;
        this.reciterId = reciterId;
        this.versionId = versionId;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentTelawatSuarBinding.inflate(
                inflater, container, false);

        if (type == ListType.Downloaded)
            checkDownloaded();

        setupRecycler();

        setSearchListeners();

        return binding.getRoot();
    }

    private void getData() {
        AppDatabase db = Room.databaseBuilder(requireContext(), AppDatabase.class, "HidayaDB")
                .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build();

        List<SuraDB> suras = db.suraDao().getAll();

        surahNames = new ArrayList<>();
        searchNames = new String[114];
        for (int i = 0; i < 114; i++) {
            surahNames.add(suras.get(i).getSura_name());
            searchNames[i] = suras.get(i).getSearch_name();
        }

        availableSurahs = db.telawatVersionsDao().getSuras(reciterId, versionId);

        favs = db.suraDao().getFav();
    }

    private ArrayList<ReciterSuraCard> makeCards() {
        getData();

        ArrayList<ReciterSuraCard> cards = new ArrayList<>();
        for (int i = 0; i < 114; i++) {
            if (!availableSurahs.contains("," + (i+1) + ",")  ||
                    (type == ListType.Favorite && favs.get(i) == 0) ||
                    (type == ListType.Downloaded && !downloaded[i]))
                continue;

            String name = surahNames.get(i);
            String searchName = searchNames[i];

            int finalI = i;
            View.OnClickListener listener = v -> {
                Intent intent = new Intent(getContext(), TelawatClient.class);
                intent.setAction("start");

                String rId = String.format(Locale.US, "%03d", reciterId);
                String vId = String.format(Locale.US, "%02d", versionId);
                String sId = String.format(Locale.US, "%03d", finalI);
                String mediaId = rId + vId + sId;
                intent.putExtra("media_id", mediaId);

                startActivity(intent);
            };

            cards.add(new ReciterSuraCard(i, name, searchName, favs.get(i), listener));
        }
        return cards;
    }

    private void setupRecycler() {
        recycler = binding.recycler;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler.setLayoutManager(layoutManager);
        adapter = new TelawatSuarAdapter(getContext(), makeCards(), reciterId, versionId);
        recycler.setAdapter(adapter);
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

    private void checkDownloaded() {
        downloaded = new boolean[114];

        String prefix = "/Telawat Downloads/" + reciterId + "/" + versionId;

        File dir = new File(requireContext().getExternalFilesDir(null) + prefix);

        if (!dir.exists())
            return;

        File[] files = dir.listFiles();

        for (int i = 0; i < Objects.requireNonNull(files).length; i++) {
            File file = files[i];

            String name = file.getName();
            String n = name.substring(0, name.length()-4);
            try {
                int num = Integer.parseInt(n);
                downloaded[num] = true;
            }
            catch (NumberFormatException ignored) {}
        }
    }

    public void onDestroy() {
        super.onDestroy();
        binding = null;
        recycler.setAdapter(null);
        adapter = null;
    }
}