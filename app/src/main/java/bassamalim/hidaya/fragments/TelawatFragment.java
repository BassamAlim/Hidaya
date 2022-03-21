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
import java.util.Objects;

import bassamalim.hidaya.activities.TelawatSuarCollectionActivity;
import bassamalim.hidaya.adapters.TelawatAdapter;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.database.dbs.TelawatDB;
import bassamalim.hidaya.database.dbs.TelawatRecitersDB;
import bassamalim.hidaya.databinding.FragmentTelawatBinding;
import bassamalim.hidaya.enums.ListType;
import bassamalim.hidaya.models.ReciterCard;

public class TelawatFragment extends Fragment {

    private FragmentTelawatBinding binding;
    private RecyclerView recycler;
    private TelawatAdapter adapter;
    private List<TelawatDB> telawat;
    private List<TelawatRecitersDB> reciters;
    private boolean[] downloaded;
    private final ListType type;

    public TelawatFragment(ListType type) {
        this.type = type;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentTelawatBinding.inflate(inflater, container, false);

        reciters = getData();

        if (type == ListType.Downloaded)
            checkDownloaded();

        setupRecycler();

        setSearchListeners();

        return binding.getRoot();
    }

    private List<TelawatRecitersDB> getData() {
        AppDatabase db = Room.databaseBuilder(requireContext().getApplicationContext(),
                AppDatabase.class, "HidayaDB").createFromAsset("databases/HidayaDB.db")
                .allowMainThreadQueries().build();

        telawat = db.telawatDao().getAll();

        if (type == ListType.Favorite)
            return db.telawatRecitersDao().getFavorites();
        return db.telawatRecitersDao().getAll();
    }

    private void checkDownloaded() {
        downloaded = new boolean[telawat.size()];

        String prefix = "/Telawat Downloads/";

        File dir = new File(requireContext().getExternalFilesDir(null) + prefix);

        if (!dir.exists())
            return;

        File[] files = dir.listFiles();

        if (files == null || files.length == 0)
            return;

        for (int i = 0; i < Objects.requireNonNull(files).length; i++) {
            String name = files[i].getName();
            try {
                int num = Integer.parseInt(name);
                downloaded[num] = true;
            } catch (NumberFormatException ignored) {}
        }
    }

    private ArrayList<ReciterCard> makeCards() {
        ArrayList<ReciterCard> cards = new ArrayList<>();
        for (int i = 0; i < reciters.size(); i++) {
            if (type == ListType.Downloaded && !downloaded[i])
                continue;

            TelawatRecitersDB reciter = reciters.get(i);

            List<TelawatDB> versions = getVersions(reciter.getReciter_id());

            List<ReciterCard.RecitationVersion> versionsList = new ArrayList<>();

            for (int j = 0; j < versions.size(); j++) {
                TelawatDB telawa = versions.get(j);

                View.OnClickListener listener = v -> {
                    Intent intent = new Intent(v.getContext(),
                            TelawatSuarCollectionActivity.class);
                    intent.putExtra("reciter_id", telawa.getReciter_id());
                    intent.putExtra("reciter_name", telawa.getReciter_name());
                    intent.putExtra("version_id", telawa.getVersion_id());
                    startActivity(intent);
                };

                versionsList.add(new ReciterCard.RecitationVersion(telawa.getVersion_id(),
                        telawa.getUrl(), telawa.getRewaya(), telawa.getCount(),
                        telawa.getSuras(), listener));
            }
            cards.add(new ReciterCard(reciter.getReciter_id(), reciter.getReciter_name(),
                    reciter.getFavorite(), versionsList));
        }

        return cards;
    }

    private List<TelawatDB> getVersions(int id) {
        List<TelawatDB> result = new ArrayList<>();
        for (int i = 0; i < telawat.size(); i++) {
            TelawatDB telawa = telawat.get(i);
            if (telawa.getReciter_id() == id)
                result.add(telawa);
        }
        return result;
    }

    private void setupRecycler() {
        recycler = binding.recycler;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler.setLayoutManager(layoutManager);
        adapter = new TelawatAdapter(getContext(), makeCards());
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

    public void onDestroy() {
        super.onDestroy();
        binding = null;
        recycler.setAdapter(null);
        adapter = null;
    }
}