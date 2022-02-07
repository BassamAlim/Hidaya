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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import bassamalim.hidaya.activities.TelawatClient;
import bassamalim.hidaya.adapters.TelawatSuarAdapter;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.database.dbs.SuraDB;
import bassamalim.hidaya.databinding.FragmentTelawatSuarBinding;
import bassamalim.hidaya.models.ReciterSuraCard;

public class FavoriteTelawatSuarFragment extends Fragment {

    private FragmentTelawatSuarBinding binding;
    private RecyclerView recycler;
    private TelawatSuarAdapter adapter;
    private final int reciterId;
    private final int versionId;
    private String availableSurahs;
    private ArrayList<String> surahNames;
    private String[] searchNames;
    private List<Integer> favorites;

    public FavoriteTelawatSuarFragment(int reciterId, int versionId) {
        this.reciterId = reciterId;
        this.versionId = versionId;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentTelawatSuarBinding.inflate(inflater, container, false);

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

        favorites = db.suraDao().getFav();
    }

    private ArrayList<ReciterSuraCard> makeCards() {
        getData();

        ArrayList<ReciterSuraCard> cards = new ArrayList<>();
        for (int i = 0; i < 114; i++) {
            if (availableSurahs.contains("," + (i+1) + ",") && favorites.get(i) == 1) {
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

                cards.add(new ReciterSuraCard(i, name, searchName, 1, listener));
            }
        }
        return cards;
    }

    private void setupRecycler() {
        recycler = binding.reciterSurahsRecycler;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler.setLayoutManager(layoutManager);
        adapter = new TelawatSuarAdapter(getContext(), makeCards(), reciterId, versionId);
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