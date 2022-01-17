package bassamalim.hidaya.fragments;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Objects;

import bassamalim.hidaya.R;
import bassamalim.hidaya.activities.MainActivity;
import bassamalim.hidaya.databinding.FragmentCollectionTelawatBinding;
import bassamalim.hidaya.helpers.Keeper;

public class CollectionTelawatFragment extends FragmentActivity {

    private FragmentCollectionTelawatBinding binding;
    private FragmentStateAdapter adapter;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = FragmentCollectionTelawatBinding.inflate(getLayoutInflater());
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(binding.getRoot());

        setActionBar(binding.topBar);
        Objects.requireNonNull(getActionBar()).setDisplayShowTitleEnabled(false);
        binding.barTitle.setText("تلاوات");

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

            if (isTaskRoot()) {
                Intent intent = new Intent(this, MainActivity.class);
                Location location = new Keeper(this).retrieveLocation();
                intent.putExtra("located", location != null);
                intent.putExtra("location", location);
                startActivity(intent);
                finish();
            }
        }
        else {
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