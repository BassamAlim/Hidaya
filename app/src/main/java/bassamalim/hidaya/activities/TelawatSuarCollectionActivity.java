package bassamalim.hidaya.activities;

import android.content.Intent;
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
import bassamalim.hidaya.databinding.ActivityTelawatSuarCollectionBinding;
import bassamalim.hidaya.fragments.AllTelawatSuarFragment;
import bassamalim.hidaya.fragments.DownloadedTelawatSuarFragment;
import bassamalim.hidaya.fragments.FavoriteTelawatSuarFragment;

public class TelawatSuarCollectionActivity extends FragmentActivity {

    private ActivityTelawatSuarCollectionBinding binding;
    private FragmentStateAdapter adapter;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTelawatSuarCollectionBinding.inflate(getLayoutInflater());
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        int reciterId = intent.getIntExtra("reciter_id", 0);
        String reciterName = intent.getStringExtra("reciter_name");
        int versionId = intent.getIntExtra("version_id", 0);

        setActionBar(binding.topBar);
        Objects.requireNonNull(getActionBar()).setDisplayShowTitleEnabled(false);
        binding.barTitle.setText(reciterName);

        viewPager = findViewById(R.id.telawat_pager);
        adapter = new TSAdapter(this, reciterId, versionId);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tab_layout);

        String[] tabs = new String[] {"الكل", "المفضلة", "المحملة"};
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
                Intent intent = new Intent(this, TelawatCollectionActivity.class);
                startActivity(intent);
            }
        }
        else        // Otherwise, select the previous step.
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
    }
}

class TSAdapter extends FragmentStateAdapter {

    private final int reciterId;
    private final int versionId;

    public TSAdapter(FragmentActivity fragment, int reciterId, int versionId) {
        super(fragment);
        this.reciterId = reciterId;
        this.versionId = versionId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;

        if (position == 0)
            fragment = new AllTelawatSuarFragment(reciterId, versionId);
        else if (position == 1)
            fragment = new FavoriteTelawatSuarFragment(reciterId, versionId);
        else
            fragment = new DownloadedTelawatSuarFragment(reciterId, versionId);

        return fragment;
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}