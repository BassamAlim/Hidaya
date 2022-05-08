package bassamalim.hidaya.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import bassamalim.hidaya.R;
import bassamalim.hidaya.activities.QuranActivity;
import bassamalim.hidaya.activities.QuranSearcherActivity;
import bassamalim.hidaya.adapters.QuranFragmentAdapter;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.database.dbs.SuarDB;
import bassamalim.hidaya.databinding.FragmentQuranBinding;
import bassamalim.hidaya.models.Sura;

public class FavoriteQuranFragment extends Fragment {

    private FragmentQuranBinding binding;
    private RecyclerView recyclerView;
    private QuranFragmentAdapter adapter;
    private static Bundle mBundleRecyclerViewState;
    private Parcelable mListState = null;
    private GridLayoutManager gridLayoutManager;
    private List<String> names;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        gridLayoutManager = new GridLayoutManager(getContext(), 1);

        binding = FragmentQuranBinding.inflate(inflater, container, false);

        setListeners();

        setupRecycler();

        setSearchListeners();

        return binding.getRoot();
    }

    @Override
    public void onPause() {
        super.onPause();
        mBundleRecyclerViewState = new Bundle();
        mListState = Objects.requireNonNull(recyclerView.getLayoutManager()).onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable("recycler_state", mListState);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mBundleRecyclerViewState != null) {
            new Handler().postDelayed(() -> {
                mListState = mBundleRecyclerViewState.getParcelable("recycler_state");
                Objects.requireNonNull(recyclerView.getLayoutManager())
                        .onRestoreInstanceState(mListState);
            }, 50);
        }
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);

        if (menuVisible) {
            adapter = new QuranFragmentAdapter(getContext(), makeCards());
            recyclerView.setAdapter(adapter);
        }
    }

    private void setListeners() {
        binding.fab.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), QuranSearcherActivity.class);
            startActivity(intent);
        });

        binding.recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 || dy < 0 && binding.fab.isShown())
                    binding.fab.hide();
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE &&
                        recyclerView.canScrollVertically(1))
                    binding.fab.show();
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    private void setupRecycler() {
        recyclerView = binding.recycler;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new QuranFragmentAdapter(getContext(), makeCards());
        recyclerView.setAdapter(adapter);
    }

    public ArrayList<Sura> makeCards() {
        ArrayList<Sura> cards = new ArrayList<>();
        List<SuarDB> suras = getSuras();

        String surat = getString(R.string.sura);
        for (int i = 0; i < suras.size(); i++) {
            SuarDB sura = suras.get(i);

            View.OnClickListener cardListener = v -> {
                Intent intent = new Intent(getContext(), QuranActivity.class);
                intent.setAction("by_surah");
                intent.putExtra("surah_id", sura.getSura_id());
                requireContext().startActivity(intent);
            };

            cards.add(new Sura(sura.getSura_id(), surat + " " + names.get(i),
                    sura.getSearch_name(), sura.getTanzeel(), 1, cardListener));
        }
        return cards;
    }

    private List<SuarDB> getSuras() {
        AppDatabase db = Room.databaseBuilder(requireContext(), AppDatabase.class, "HidayaDB")
                .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build();

        boolean en = PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getString(getString(R.string.language_key), getString(R.string.default_language))
                .equals("en");
        names = en ? db.suarDao().getNamesEn() : db.suarDao().getNames();

        return db.suarDao().getFavorites();
    }

    private void setSearchListeners() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filterNumber(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filterName(newText);
                return true;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        recyclerView.setAdapter(null);
        adapter = null;
    }
}
