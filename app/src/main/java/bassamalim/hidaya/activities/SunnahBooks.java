package bassamalim.hidaya.activities;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.io.File;
import java.util.Objects;

import bassamalim.hidaya.R;
import bassamalim.hidaya.databinding.ActivitySunnahBooksBinding;
import bassamalim.hidaya.other.Const;
import bassamalim.hidaya.other.Utils;

public class SunnahBooks extends AppCompatActivity {

    private ActivitySunnahBooksBinding binding;
    private final String prefix = "/Sunnah Downloads/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        binding = ActivitySunnahBooksBinding.inflate(getLayoutInflater());
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        initViews();

        setListeners();
    }

    private void initViews() {
        for (int i = 0; i < 1; i++)
            updateUI(i, downloaded(i));
    }

    private void setListeners() {
        binding.bokhariCard.setOnClickListener(v -> {
            if (downloaded(0)) {
                Intent intent = new Intent(this,
                        SunnahChaptersCollectionActivity.class);
                intent.putExtra("book_id", 0);
                intent.putExtra("book_title", "صحيح البخاري");
                startActivity(intent);
            }
            else
                getLinkAndRequestDownload(0);
        });
        binding.bokhariDownloadBtn.setOnClickListener(v -> {
            if (downloaded(0)) {
                Utils.deleteFile(this, prefix + "0.json");
                updateUI(0, false);
            }
            else {
                getLinkAndRequestDownload(0);
                updateUI(0, true);
            }
        });
    }

    private boolean downloaded(int id) {
        File dir = new File(getExternalFilesDir(null) + prefix);

        if (!dir.exists())
            return false;

        File[] files = dir.listFiles();

        for (int i = 0; i < Objects.requireNonNull(files).length; i++) {
            File file = files[i];

            String name = file.getName();
            String n = name.substring(0, name.length()-5);
            try {
                int num = Integer.parseInt(n);
                if (num == id)
                    return true;
            }
            catch (NumberFormatException ignored) {}
        }
        return false;
    }

    private void updateUI(int id, boolean downloaded) {
        ImageButton ib = null;

        switch (id) {
            case 0:
                ib = binding.bokhariDownloadBtn;
                break;
        }

        assert ib != null;
        if (downloaded)
            ib.setImageDrawable(AppCompatResources.getDrawable(
                    this, R.drawable.ic_downloaded));
        else
            ib.setImageDrawable(AppCompatResources.getDrawable(
                    this, R.drawable.ic_download));
    }

    private void getLinkAndRequestDownload(int id) {
        String[] links = new String[1];
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        Log.d(Const.TAG, "Here");
        remoteConfig.fetchAndActivate().addOnCompleteListener(this, task -> {
            Log.d(Const.TAG, "There");
            if (task.isSuccessful()) {
                links[0] = remoteConfig.getString("sahih_albokhari_url");

                Log.d(Const.TAG, "Config params updated");
                Log.d(Const.TAG, "Sahih alBokhari URL: " + links[0]);

                download(id, links[id]);
            }
            else
                Log.d(Const.TAG, "Fetch failed");
        });
    }

    private void download(int id, String link) {
        Log.d(Const.TAG, link);

        DownloadManager downloadManager = (DownloadManager)
                getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(link);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle("تحميل الكتاب");
        request.setVisibleInDownloadsUi(true);
        Utils.createDir(this, prefix);
        request.setDestinationInExternalFilesDir(this, prefix, id + ".json");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        downloadManager.enqueue(request);
    }

}