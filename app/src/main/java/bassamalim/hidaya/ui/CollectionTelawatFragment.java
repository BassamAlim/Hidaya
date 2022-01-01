package bassamalim.hidaya.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import bassamalim.hidaya.R;

public class CollectionTelawatFragment extends FragmentActivity {

    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    private FragmentStateAdapter adapter;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_collection_telawat);

        viewPager = findViewById(R.id.telawat_pager);
        adapter = new TAdapter(this);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tab_layout);

        String[] tabs = new String[] {"جميع القراء", "المفضلة"};
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabs[position])
        ).attach();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (viewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }
}

class TAdapter extends FragmentStateAdapter {

    public TAdapter(FragmentActivity fragment) {
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