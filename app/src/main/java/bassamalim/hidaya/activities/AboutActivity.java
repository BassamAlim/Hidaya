package bassamalim.hidaya.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import bassamalim.hidaya.databinding.ActivityAboutBinding;
import bassamalim.hidaya.other.Global;
import bassamalim.hidaya.other.Utils;

public class AboutActivity extends AppCompatActivity {

    private ActivityAboutBinding binding;
    private int counter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.home.setOnClickListener(v -> onBackPressed());

        setListener();
    }

    private void setListener() {
        binding.titleTv.setOnClickListener(v -> {
            counter++;
            if (counter == 5)
                voala();
        });

        binding.rebuildDb.setOnClickListener(v -> {
            deleteDatabase("HidayaDB");

            Log.i(Global.TAG, "Database Rebuilt");

            Utils.reviveDb(this);

            Toast.makeText(this, "تمت إعادة بناء قاعدة البيانات",
                    Toast.LENGTH_SHORT).show();
        });
        binding.driveUpdate.setOnClickListener(v -> {
            String url = FirebaseRemoteConfig.getInstance().getString(Global.UPDATE_URL);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
    }

    private void voala() {
        Toast.makeText(this, "مرحبا بك في قسم الشخصيات المهمة", Toast.LENGTH_SHORT).show();
        binding.driveUpdate.setVisibility(View.VISIBLE);
        binding.rebuildDb.setVisibility(View.VISIBLE);
    }
}
