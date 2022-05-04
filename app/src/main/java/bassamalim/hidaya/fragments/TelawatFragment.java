package bassamalim.hidaya.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import bassamalim.hidaya.R;
import bassamalim.hidaya.activities.TelawatSuarCollectionActivity;
import bassamalim.hidaya.adapters.TelawatAdapter;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.database.dbs.TelawatDB;
import bassamalim.hidaya.database.dbs.TelawatRecitersDB;
import bassamalim.hidaya.databinding.FragmentTelawatBinding;
import bassamalim.hidaya.dialogs.FilterDialog;
import bassamalim.hidaya.enums.ListType;
import bassamalim.hidaya.models.ReciterCard;

@RequiresApi(api = Build.VERSION_CODES.O)
public class TelawatFragment extends Fragment {

    private FragmentTelawatBinding binding;
    private SharedPreferences pref;
    private Gson gson;
    private String[] rewayat;
    private RecyclerView recycler;
    private TelawatAdapter adapter;
    private List<TelawatDB> telawat;
    private List<TelawatRecitersDB> reciters;
    private boolean[] downloaded;
    private boolean[] selectedRewayat;
    private ListType type;

    public TelawatFragment() {}

    public TelawatFragment(ListType type) {
        this.type = type;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentTelawatBinding.inflate(inflater, container, false);

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext());
        gson = new Gson();
        rewayat = getResources().getStringArray(R.array.rewayat);

        reciters = getData();

        if (type == ListType.Downloaded)
            checkDownloaded();

        setupRecycler();
        filter();

        setListeners();

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
        cleanup();

        downloaded = new boolean[telawat.size()];

        String prefix = "/Telawat/";

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

    private void filter() {
        selectedRewayat = getSelectedRewayat();
        adapter.filter(null, selectedRewayat);
        for (Boolean aBoolean : selectedRewayat) {
            if (!aBoolean) {
                binding.filterIb.setImageDrawable(AppCompatResources.getDrawable(
                        requireContext(), R.drawable.ic_filtered));
                break;
            }
        }
    }

    private void setListeners() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query, null);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText, null);
                return true;
            }
        });

        binding.filterIb.setOnClickListener(v ->
                new FilterDialog<>(getContext(), v,
                        getResources().getString(R.string.choose_rewaya), rewayat, selectedRewayat,
                        adapter, binding.filterIb, "selected_rewayat"));
    }

    private boolean[] getSelectedRewayat() {
        boolean[] defArr = new boolean[rewayat.length];
        Arrays.fill(defArr, true);
        String defStr = gson.toJson(defArr);

        return gson.fromJson(pref.getString("selected_rewayat", defStr), boolean[].class);
    }

    private void cleanup() {
        String path = requireContext().getExternalFilesDir(null) + "/Telawat/";
        File file = new File(path);

        File[] rFiles = file.listFiles();
        if (rFiles == null)
            return;

        for (File rFile : rFiles) {
            String rfName = rFile.getName();
            try {
                Integer.parseInt(rfName);
                File[] vFiles = rFile.listFiles();
                if (vFiles == null)
                    continue;

                for (File vFile : vFiles) {
                    if (Objects.requireNonNull(vFile.listFiles()).length == 0)
                        vFile.delete();
                }

                vFiles = rFile.listFiles();
                if (vFiles == null)
                    continue;

                if (vFiles.length == 0)
                    rFile.delete();
            } catch (NumberFormatException ignored) {}
        }
    }

    public void onDestroy() {
        super.onDestroy();
        binding = null;
        recycler.setAdapter(null);
        adapter = null;
    }
}
