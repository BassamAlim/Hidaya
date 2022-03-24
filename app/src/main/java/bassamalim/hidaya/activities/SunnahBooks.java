package bassamalim.hidaya.activities;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;

import java.io.File;
import java.util.Objects;

import bassamalim.hidaya.R;
import bassamalim.hidaya.databinding.ActivitySunnahBooksBinding;
import bassamalim.hidaya.models.SunnahBook;
import bassamalim.hidaya.other.Const;
import bassamalim.hidaya.other.Utils;

public class SunnahBooks extends AppCompatActivity {

    private ActivitySunnahBooksBinding binding;
    private final int NUM_OF_BOOKS = 2;
    private final String[] names = new String[] {"صحيح البخاري", "صحيح مسلم"};
    private final String prefix = "/Sunnah Downloads/";
    private final boolean[] downloaded = new boolean[NUM_OF_BOOKS];
    private CardView[] cards;
    private ImageButton[] favBtns;
    private SunnahBook.BookInfo[] infos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        binding = ActivitySunnahBooksBinding.inflate(getLayoutInflater());
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        checkDownloaded();

        getInfos();

        initViews();

        setListeners();
    }

    private void checkDownloaded() {
        File dir = new File(getExternalFilesDir(null) + prefix);

        if (!dir.exists())
            return;

        File[] files = dir.listFiles();

        for (int i = 0; i < Objects.requireNonNull(files).length; i++) {
            File file = files[i];

            String name = file.getName();
            String n = name.substring(0, name.length()-5);
            try {
                int num = Integer.parseInt(n);
                downloaded[num] = true;
            }
            catch (NumberFormatException ignored) {}
        }
    }

    private void getInfos() {
        infos = new SunnahBook.BookInfo[NUM_OF_BOOKS];
        for (int i = 0; i < NUM_OF_BOOKS; i++) {
            if (downloaded[i]) {
                String path = getExternalFilesDir(null) + "/Sunnah Downloads/" + i + ".json";
                String jsonStr = Utils.getJsonFromDownloads(path);
                Gson gson = new Gson();
                SunnahBook book = gson.fromJson(jsonStr, SunnahBook.class);
                infos[i] = book.getBookInfo();
            }
        }
    }

    private void initViews() {
        cards = new CardView[]{binding.bokhariCard, binding.muslimCard};
        TextView[] titleTvs = new TextView[]{binding.bokhariTitleTv, binding.muslimTitleTv};
        favBtns = new ImageButton[]{binding.bokhariDownloadBtn, binding.muslimDownloadBtn};

        for (int i = 0; i < NUM_OF_BOOKS; i++) {
            if (downloaded[i]) {
                titleTvs[i].setText(infos[i].getBookTitle());
                // ... other info
            }
        }

        for (int i = 0; i < NUM_OF_BOOKS; i++)
            updateUI(i, downloaded[i]);
    }

    private void setListeners() {
        for (int i = 0; i < NUM_OF_BOOKS; i++) {
            int finalI = i;
            cards[i].setOnClickListener(v -> {
                if (downloaded(finalI)) {
                    Intent intent = new Intent(this,
                            SunnahChaptersCollectionActivity.class);
                    intent.putExtra("book_id", finalI);
                    intent.putExtra("book_title", names[finalI]);
                    startActivity(intent);
                }
                else {
                    getLinkAndRequestDownload(finalI);
                    updateUI(finalI, true);
                }
            });
            favBtns[i].setOnClickListener(v -> {
                if (downloaded(finalI)) {
                    Utils.deleteFile(this, prefix + finalI + ".json");
                    updateUI(finalI, false);
                }
                else {
                    getLinkAndRequestDownload(finalI);
                    updateUI(finalI, true);
                }
            });
        }
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
        if (downloaded)
            favBtns[id].setImageDrawable(AppCompatResources.getDrawable(
                    this, R.drawable.ic_downloaded));
        else
            favBtns[id].setImageDrawable(AppCompatResources.getDrawable(
                    this, R.drawable.ic_download));
    }

    private void getLinkAndRequestDownload(int id) {
        String[] links = new String[2];
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        remoteConfig.fetchAndActivate().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                links[0] = remoteConfig.getString("sahih_albokhari_url");
                links[1] = remoteConfig.getString("sahih_muslim_url");

                Log.d(Const.TAG, "Config params updated");
                Log.d(Const.TAG, "Sahih Albokhari URL: " + links[0]);
                Log.d(Const.TAG, "Sahih Muslim URL: " + links[1]);

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