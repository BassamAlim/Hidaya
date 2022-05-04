package bassamalim.hidaya.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import bassamalim.hidaya.R;
import bassamalim.hidaya.databinding.ActivityWelcomeBinding;
import bassamalim.hidaya.other.Utils;

public class WelcomeActivity extends AppCompatActivity {

    private ActivityWelcomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        Utils.setLocale(this, null);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState == null)
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.settings, new Settings.SettingsFragment()).commit();

        setListeners();
    }

    private void setListeners() {
        binding.saveBtn.setOnClickListener(v -> {
            binding.settings.setVisibility(View.GONE);
            binding.saveBtn.setVisibility(View.GONE);
            binding.disclaimerSpace.setVisibility(View.VISIBLE);
        });

        binding.agreed.setOnClickListener(view -> {
            Intent intent = new Intent(this, Splash.class);
            startActivity(intent);
            finish();
        });
        binding.rejected.setOnClickListener(view -> launchAnyWay());
    }

    private void launchAnyWay() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("located", false);
        startActivity(intent);
        finish();
    }
}