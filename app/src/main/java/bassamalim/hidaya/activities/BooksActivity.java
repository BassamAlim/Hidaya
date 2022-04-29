package bassamalim.hidaya.activities;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;

import java.io.File;
import java.util.Objects;

import bassamalim.hidaya.R;
import bassamalim.hidaya.databinding.ActivityBooksBinding;
import bassamalim.hidaya.dialogs.TutorialDialog;
import bassamalim.hidaya.models.Book;
import bassamalim.hidaya.other.Global;
import bassamalim.hidaya.other.Utils;

public class BooksActivity extends AppCompatActivity {

    private ActivityBooksBinding binding;
    private int numOfBooks;
    private final String prefix = "/Books/";
    private boolean[] downloaded;
    private CardView[] cards;
    private ImageButton[] favBtns;
    private Book.BookInfo[] infoArr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        binding = ActivityBooksBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.home.setOnClickListener(v -> onBackPressed());

        numOfBooks = getResources().getStringArray(R.array.books_titles).length;
        downloaded = new boolean[numOfBooks];

        checkFirstTime();

        checkDownloaded();

        getInfoArr();

        initViews();

        setListeners();
    }

    private void checkFirstTime() {
        String key = "is_first_time_in_books_activity";
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(key, true))
            new TutorialDialog(this, getString(R.string.books_activity_tips), key).show(
                    this.getSupportFragmentManager(), TutorialDialog.TAG);
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

    private void getInfoArr() {
        infoArr = new Book.BookInfo[numOfBooks];
        for (int i = 0; i < numOfBooks; i++) {
            if (downloaded[i]) {
                String path = getExternalFilesDir(null) + "/Books/" + i + ".json";
                String jsonStr = Utils.getJsonFromDownloads(path);
                Gson gson = new Gson();
                Book book = gson.fromJson(jsonStr, Book.class);
                infoArr[i] = book.getBookInfo();
            }
        }
    }

    private void initViews() {
        cards = new CardView[]{binding.bokhariCard, binding.muslimCard};
        TextView[] titleTvs = new TextView[]{binding.bokhariTitleTv, binding.muslimTitleTv};
        favBtns = new ImageButton[]{binding.bokhariDownloadBtn, binding.muslimDownloadBtn};

        for (int i = 0; i < numOfBooks; i++) {
            if (downloaded[i]) {
                titleTvs[i].setText(infoArr[i].getBookTitle());
                // ... other info
            }
        }

        for (int i = 0; i < numOfBooks; i++)
            updateUI(i, downloaded[i]);
    }

    private void setListeners() {
        for (int i = 0; i < numOfBooks; i++) {
            int finalI = i;
            cards[i].setOnClickListener(v -> {
                if (downloaded(finalI)) {
                    Intent intent = new Intent(this,
                            BooksChaptersCollectionActivity.class);
                    intent.putExtra("book_id", finalI);
                    intent.putExtra("book_title",
                            getResources().getStringArray(R.array.books_titles)[finalI]);
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

        binding.fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, BookSearcher.class);
            startActivity(intent);
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

                Log.d(Global.TAG, "Config params updated");
                Log.d(Global.TAG, "Sahih Albokhari URL: " + links[0]);
                Log.d(Global.TAG, "Sahih Muslim URL: " + links[1]);

                download(id, links[id]);
            }
            else
                Log.d(Global.TAG, "Fetch failed");
        });
    }

    private void download(int id, String link) {
        Log.d(Global.TAG, link);

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