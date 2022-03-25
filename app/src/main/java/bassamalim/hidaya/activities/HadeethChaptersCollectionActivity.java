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
import bassamalim.hidaya.databinding.ActivityCollectionHadeethChaptersBinding;
import bassamalim.hidaya.enums.ListType;
import bassamalim.hidaya.fragments.HadeethChaptersFragment;
import bassamalim.hidaya.other.Utils;

public class HadeethChaptersCollectionActivity extends FragmentActivity {

    private ActivityCollectionHadeethChaptersBinding binding;
    private FragmentStateAdapter adapter;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        binding = ActivityCollectionHadeethChaptersBinding.inflate(getLayoutInflater());
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        int bookId = intent.getIntExtra("book_id", 0);
        String bookTitle = intent.getStringExtra("book_title");

        setActionBar(binding.topBar);
        Objects.requireNonNull(getActionBar()).setDisplayShowTitleEnabled(false);
        binding.barTitle.setText(bookTitle);

        viewPager = findViewById(R.id.pager);
        adapter = new SCAdapter(this, bookId);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tab_layout);

        String[] tabs = new String[] {"الكل", "المفضلة"};
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
        }
        else        // Otherwise, select the previous step.
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
    }
}

class SCAdapter extends FragmentStateAdapter {

    private final int bookId;

    public SCAdapter(FragmentActivity fragment, int bookId) {
        super(fragment);
        this.bookId = bookId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        ListType type;

        if (position == 0)
            type = ListType.All;
        else
            type = ListType.Favorite;

        return new HadeethChaptersFragment(type, bookId);
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}