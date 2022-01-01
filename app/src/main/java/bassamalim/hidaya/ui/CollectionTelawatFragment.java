package bassamalim.hidaya.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import bassamalim.hidaya.R;

public class CollectionTelawatFragment extends Fragment {

    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    private TAdapter tAdapter;
    private ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_collection_telawat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tAdapter = new TAdapter(this);
        viewPager = view.findViewById(R.id.pager);
        viewPager.setAdapter(tAdapter);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);

        String[] tabs = new String[] {"جميع القراء", "المفضلة"};
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabs[position])
        ).attach();
    }
}

class TAdapter extends FragmentStateAdapter {
    public TAdapter(Fragment fragment) {
        super(fragment);
    }

    @NonNull @Override
    public Fragment createFragment(int position) {
        // Return a NEW fragment instance in createFragment(int)
        Fragment fragment;

        if (position == 0)
            fragment = new MainTelawatFragment();
        else
            fragment = new FavoriteTelawatFragment();

        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}