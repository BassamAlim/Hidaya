package bassamalim.hidaya.activities;

import android.content.Intent;
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
import bassamalim.hidaya.databinding.ActivityCollectionTelawatSuarBinding;
import bassamalim.hidaya.enums.ListType;
import bassamalim.hidaya.fragments.TelawatSuarFragment;
import bassamalim.hidaya.other.Utils;

public class TelawatSuarCollectionActivity extends FragmentActivity {

    private ActivityCollectionTelawatSuarBinding binding;
    private FragmentStateAdapter adapter;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        binding = ActivityCollectionTelawatSuarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.home.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        int reciterId = intent.getIntExtra("reciter_id", 0);
        String reciterName = intent.getStringExtra("reciter_name");
        int versionId = intent.getIntExtra("version_id", 0);

        binding.topBarTitle.setText(reciterName);

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
        ListType type;

        if (position == 0)
            type = ListType.All;
        else if (position == 1)
            type = ListType.Favorite;
        else
            type = ListType.Downloaded;

        return new TelawatSuarFragment(type, reciterId, versionId);
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}