package bassamalim.hidaya.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import bassamalim.hidaya.R;
import bassamalim.hidaya.databinding.AboutActivityBinding;
import bassamalim.hidaya.other.Constants;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class AboutActivity extends AppCompatActivity {

    AboutActivityBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AboutActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setListener();
    }

    private void setListener() {
        binding.driveUpdate.setOnClickListener(v -> {
            String url = FirebaseRemoteConfig.getInstance().getString(Constants.UPDATE_URL);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
    }

}
