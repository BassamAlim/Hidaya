package bassamalim.hidaya.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
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

import bassamalim.hidaya.activities.QuranActivity;
import bassamalim.hidaya.adapters.QuranFragmentAdapter;
import bassamalim.hidaya.databinding.FragmentMainQuranBinding;
import bassamalim.hidaya.database.dbs.SuraDB;
import bassamalim.hidaya.models.SurahCard;
import bassamalim.hidaya.database.AppDatabase;

public class MainQuranFragment extends Fragment {

    private FragmentMainQuranBinding binding;
    private RecyclerView recyclerView;
    private QuranFragmentAdapter adapter;
    private ArrayList<SurahCard> surahCards;
    private static Bundle mBundleRecyclerViewState;
    private Parcelable mListState = null;
    private GridLayoutManager gridLayoutManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        gridLayoutManager = new GridLayoutManager(getContext(), 1);

        binding = FragmentMainQuranBinding.inflate(inflater, container, false);

        surahCards = makeSurahButtons();

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
        setupContinue();

        if (mBundleRecyclerViewState != null) {
            new Handler().postDelayed(() -> {
                mListState = mBundleRecyclerViewState.getParcelable("recycler_state");
                Objects.requireNonNull(recyclerView.getLayoutManager())
                        .onRestoreInstanceState(mListState);
            }, 50);
        }
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    private void setupContinue() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(requireContext());
        int page = pref.getInt("bookmarked_page", -1);
        String text = pref.getString("bookmarked_text", "");
        if (page == -1)
            text = "لا يوجد صفحة محفوظة";
        else
            text = "الصفحة المحفوظة:  " + text;
        binding.continueReading.setText(text);

        binding.continueReading.setOnClickListener(v -> {
            if (page != -1) {
                Intent intent = new Intent(getContext(), QuranActivity.class);
                intent.setAction("by_page");
                intent.putExtra("page", page);
                requireContext().startActivity(intent);
            }
        });
    }

    private void setSearchListeners() {
        binding.searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

    private void setupRecycler() {
        recyclerView = binding.recycler;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new QuranFragmentAdapter(getContext(), surahCards);
        recyclerView.setAdapter(adapter);
    }

    public ArrayList<SurahCard> makeSurahButtons() {
        final int NUMBER_OF_SURAHS = 114;
        ArrayList<SurahCard> buttons = new ArrayList<>();

        AppDatabase db = Room.databaseBuilder(requireContext(), AppDatabase.class, "HidayaDB")
                .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build();
        List<Integer> favs = db.suraDao().getFav();
        List<SuraDB> suras = db.suraDao().getAll();

        for (int i = 0; i < NUMBER_OF_SURAHS; i++) {
            String name = suras.get(i).getSura_name();
            String searchName = suras.get(i).getSearch_name();
            int tanzeel = suras.get(i).getTanzeel();

            int finalI = i;
            View.OnClickListener cardListener = v -> {
                Intent intent = new Intent(getContext(), QuranActivity.class);
                intent.setAction("by_surah");
                intent.putExtra("surah_id", finalI);
                requireContext().startActivity(intent);
            };

            buttons.add(new SurahCard(i,"سُورَة " + name, searchName, tanzeel,
                    favs.get(i), cardListener));
        }
        return buttons;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        recyclerView.setAdapter(null);
        adapter = null;
    }
}
