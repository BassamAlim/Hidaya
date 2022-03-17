package bassamalim.hidaya.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import bassamalim.hidaya.databinding.ActivityAboutBinding;
import bassamalim.hidaya.other.Const;
import bassamalim.hidaya.other.Utils;

public class AboutActivity extends AppCompatActivity {

    private ActivityAboutBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setListener();
    }

    private void setListener() {
        binding.rebuildDb.setOnClickListener(v -> {
            deleteDatabase("HidayaDB");

            Log.i(Const.TAG, "Database Rebuilt");

            Utils.reviveDb(this);

            Toast.makeText(this, "تمت إعادة بناء قاعدة البيانات",
                    Toast.LENGTH_SHORT).show();
        });
        binding.driveUpdate.setOnClickListener(v -> {
            String url = FirebaseRemoteConfig.getInstance().getString(Const.UPDATE_URL);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
    }

}
