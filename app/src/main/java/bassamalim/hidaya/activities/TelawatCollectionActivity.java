package bassamalim.hidaya.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Objects;

import bassamalim.hidaya.R;
import bassamalim.hidaya.databinding.ActivityCollectionTelawatBinding;
import bassamalim.hidaya.enums.ListType;
import bassamalim.hidaya.fragments.TelawatFragment;
import bassamalim.hidaya.helpers.Keeper;
import bassamalim.hidaya.other.Utils;

public class TelawatCollectionActivity extends FragmentActivity {

    private ActivityCollectionTelawatBinding binding;
    private FragmentStateAdapter adapter;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        binding = ActivityCollectionTelawatBinding.inflate(getLayoutInflater());
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(binding.getRoot());

        viewPager = findViewById(R.id.telawat_pager);
        adapter = new TAdapter(this);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tab_layout);

        String[] tabs = new String[] {"الكل", "المفضلة", "المحملة"};
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabs[position])
        ).attach();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupContinue();
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

    private void setupContinue() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String text = pref.getString("last_played_text", "");

        if (text.length() == 0)
            text = "لم يتم تشغيل أي تلاوة بعد";
        else {
            text = "آخر تشغيل: " + text;

            binding.continueListening.setOnClickListener(v -> {
                String lastMediaId = pref.getString("last_played_media_id", "");

                Intent intent = new Intent(this, TelawatClient.class);
                intent.setAction("continue");
                intent.putExtra("media_id", lastMediaId);
                startActivity(intent);
            });
        }

        binding.continueListening.setText(text);
    }
}

class TAdapter extends FragmentStateAdapter {

    public TAdapter(FragmentActivity fragment) {
        super(fragment);
    }

    @NonNull @Override
    public Fragment createFragment(int position) {
        ListType type;

        if (position == 0)
            type = ListType.All;
        else if (position == 1)
            type = ListType.Favorite;
        else
            type = ListType.Downloaded;

        return new TelawatFragment(type);
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}