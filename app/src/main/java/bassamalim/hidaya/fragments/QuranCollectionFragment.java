package bassamalim.hidaya.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import bassamalim.hidaya.R;
import bassamalim.hidaya.activities.QuranActivity;
import bassamalim.hidaya.activities.QuranSearcherActivity;
import bassamalim.hidaya.databinding.FragmentCollectionQuranBinding;

public class QuranCollectionFragment extends Fragment {

    private FragmentCollectionQuranBinding binding;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentCollectionQuranBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        QAdapter qAdapter = new QAdapter(this);
        ViewPager2 viewPager = view.findViewById(R.id.quran_pager);
        viewPager.setAdapter(qAdapter);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);

        String[] tabs = new String[] {"الكل", "المفضلة"};
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabs[position])
        ).attach();

        setListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        setupContinue();
    }

    private void setListeners() {
        binding.fab.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), QuranSearcherActivity.class);
            startActivity(intent);
        });
    }

    private void setupContinue() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(requireContext());
        int page = pref.getInt("bookmarked_page", -1);
        String text = pref.getString("bookmarked_text", "");

        if (page == -1)
            text = "لا يوجد صفحة محفوظة";
        else {
            text = "الصفحة المحفوظة:  " + text;

            binding.continueReading.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), QuranActivity.class);
                intent.setAction("by_page");
                intent.putExtra("page", page);
                requireContext().startActivity(intent);
            });
        }

        binding.continueReading.setText(text);
    }
}

class QAdapter extends FragmentStateAdapter {
    public QAdapter(Fragment fragment) {
        super(fragment);
    }

    @NonNull @Override
    public Fragment createFragment(int position) {
        Fragment fragment;

        if (position == 0)
            fragment = new AllQuranFragment();
        else
            fragment = new FavoriteQuranFragment();

        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
