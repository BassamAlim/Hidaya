package bassamalim.hidaya.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import bassamalim.hidaya.R;
import bassamalim.hidaya.adapters.SurahButtonAdapter;
import bassamalim.hidaya.other.Constants;

public class CollectionQuranFragment extends Fragment {
    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    private QAdapter qAdapter;
    private ViewPager2 viewPager;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_collection_quran, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        qAdapter = new QAdapter(this);
        viewPager = view.findViewById(R.id.pager);
        viewPager.setAdapter(qAdapter);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);

        String[] tabs = new String[] {"جميع السور", "المفضلة"};
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabs[position])
        ).attach();
    }
}

class QAdapter extends FragmentStateAdapter {
    public QAdapter(Fragment fragment) {
        super(fragment);
    }

    @NonNull @Override
    public Fragment createFragment(int position) {
        // Return a NEW fragment instance in createFragment(int)
        Fragment fragment;

        if (position == 0)
            fragment = new MainQuranFragment();
        else
            fragment = new SavedQuranFragment();

        //Fragment fragment = new DemoObjectFragment();
        //Bundle args = new Bundle();
        // Our object is just an integer :-P
        //args.putInt(DemoObjectFragment.ARG_OBJECT, position + 1);
        //fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
