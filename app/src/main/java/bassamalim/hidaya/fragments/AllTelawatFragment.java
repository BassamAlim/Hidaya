package bassamalim.hidaya.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;

import bassamalim.hidaya.activities.TelawatSuarCollectionActivity;
import bassamalim.hidaya.adapters.TelawatAdapter;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.database.dbs.TelawatDB;
import bassamalim.hidaya.database.dbs.TelawatRecitersDB;
import bassamalim.hidaya.databinding.FragmentTelawatBinding;
import bassamalim.hidaya.models.ReciterCard;

public class AllTelawatFragment extends Fragment {

    private FragmentTelawatBinding binding;
    private RecyclerView recycler;
    private TelawatAdapter adapter;
    private List<TelawatDB> telawat;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentTelawatBinding.inflate(inflater, container, false);

        setupRecycler();

        setSearchListeners();

        return binding.getRoot();
    }

    private ArrayList<ReciterCard> makeCards() {
        AppDatabase db = Room.databaseBuilder(requireContext().getApplicationContext(),
                AppDatabase.class, "HidayaDB").createFromAsset("databases/HidayaDB.db")
                .allowMainThreadQueries().build();

        telawat = db.telawatDao().getAll();
        List<TelawatRecitersDB> reciters = db.telawatRecitersDao().getAll();
        List<Integer> favs = db.telawatRecitersDao().getFavs();

        ArrayList<ReciterCard> cards = new ArrayList<>();
        for (int i = 0; i < reciters.size(); i++) {
            TelawatRecitersDB reciter = reciters.get(i);

            String name = reciter.getReciter_name();

            List<TelawatDB> versions = getVersions(reciter.getReciter_id());

            List<ReciterCard.RecitationVersion> versionsList = new ArrayList<>();

            for (int j = 0; j < versions.size(); j++) {
                TelawatDB telawa = versions.get(j);

                View.OnClickListener listener = v -> {
                    Intent intent = new Intent(v.getContext(), TelawatSuarCollectionActivity.class);
                    intent.putExtra("reciter_id", telawa.getReciter_id());
                    intent.putExtra("reciter_name", telawa.getReciter_name());
                    intent.putExtra("version_id", telawa.getVersion_id());
                    startActivity(intent);
                };

                versionsList.add(new ReciterCard.RecitationVersion(telawa.getVersion_id(),
                        telawa.getUrl(), telawa.getRewaya(), telawa.getCount(),
                        telawa.getSuras(), listener));
            }
            cards.add(new ReciterCard(reciter.getReciter_id(), name, favs.get(i), versionsList));
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
        recycler = binding.telawatRecycler;
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recycler.setLayoutManager(layoutManager);
        adapter = new TelawatAdapter(getContext(), makeCards());
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