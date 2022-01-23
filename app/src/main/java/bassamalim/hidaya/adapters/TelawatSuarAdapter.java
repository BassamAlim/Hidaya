package bassamalim.hidaya.adapters;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import bassamalim.hidaya.R;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.database.dbs.TelawatVersionsDB;
import bassamalim.hidaya.models.ReciterSuraCard;

public class TelawatSuarAdapter extends RecyclerView.Adapter<TelawatSuarAdapter.ViewHolder> {

    private final Context context;
    private final AppDatabase db;
    private final SharedPreferences pref;
    private final List<ReciterSuraCard> suarCards;
    private final List<ReciterSuraCard> suarCardsCopy;
    private final int versionId;
    private final TelawatVersionsDB ver;
    private final boolean[] downloaded = new boolean[114];
    private final String prefix;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView card;
        private final TextView namescreen;
        private final ImageButton favBtn;
        private final ImageButton downloadBtn;

        public ViewHolder(View view) {
            super(view);
            card = view.findViewById(R.id.sura_model_card);
            namescreen = view.findViewById(R.id.sura_namescreen);
            favBtn = view.findViewById(R.id.sura_fav_btn);
            downloadBtn = view.findViewById(R.id.download_btn);
        }
    }

    public TelawatSuarAdapter(Context context, ArrayList<ReciterSuraCard> cards,
                              int reciterId, int versionId) {
        this.context = context;

        db = Room.databaseBuilder(context, AppDatabase.class, "HidayaDB")
                .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build();

        pref = PreferenceManager.getDefaultSharedPreferences(context);

        suarCards = new ArrayList<>(cards);
        suarCardsCopy = new ArrayList<>(cards);

        this.versionId = versionId;

        ver = db.telawatVersionsDao().getVersion(reciterId, versionId);

        prefix = "/Telawat Downloads/" + ver.getReciter_id() + "/" + versionId;

        checkDownloaded();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_telawat_sura, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        viewHolder.namescreen.setText(suarCards.get(position).getSurahName());
        viewHolder.card.setOnClickListener(suarCards.get(position).getListener());

        doFavorite(viewHolder, position);

        doDownloaded(viewHolder, position);
    }

    @Override
    public int getItemCount() {
        return suarCards.size();
    }

    public void filter(String text) {
        suarCards.clear();
        if (text.isEmpty())
            suarCards.addAll(suarCardsCopy);
        else {
            for(ReciterSuraCard reciterCard: suarCardsCopy) {
                if (reciterCard.getSearchName().contains(text))
                    suarCards.add(reciterCard);
            }
        }
        notifyDataSetChanged();
    }

    private void doFavorite(ViewHolder viewHolder, int position) {
        ReciterSuraCard card = suarCards.get(position);

        int fav = card.getFavorite();
        if (fav == 0)
            viewHolder.favBtn.setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.ic_star_outline));
        else if (fav == 1)
            viewHolder.favBtn.setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.ic_star));

        viewHolder.favBtn.setOnClickListener(view -> {
            if (card.getFavorite() == 0) {
                db.suraDao().setFav(card.getNum(), 1);
                card.setFavorite(1);
            }
            else if (card.getFavorite() == 1) {
                db.suraDao().setFav(card.getNum(), 0);
                card.setFavorite(0);
            }
            notifyItemChanged(position);

            updateFavorites();
        });
    }

    private void doDownloaded(ViewHolder viewHolder, int position) {
        int suraNum = suarCards.get(position).getNum();

        if (downloaded[suraNum])
            viewHolder.downloadBtn.setImageDrawable(AppCompatResources.getDrawable(
                    context, R.drawable.ic_downloaded));
        else
            viewHolder.downloadBtn.setImageDrawable(AppCompatResources.getDrawable(
                    context, R.drawable.ic_download));

        viewHolder.downloadBtn.setOnClickListener(v -> {
            if (downloaded[suarCards.get(position).getNum()]) {
                delete(suarCards.get(position).getNum());

                viewHolder.downloadBtn.setImageDrawable(AppCompatResources.getDrawable(
                        context, R.drawable.ic_download));
            }
            else {
                download(suarCards.get(position).getNum());

                viewHolder.downloadBtn.setImageDrawable(AppCompatResources.getDrawable(
                        context, R.drawable.ic_downloaded));
            }
        });
    }

    private void checkDownloaded() {
        File dir;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            dir = new File(context.getExternalFilesDir(null) + prefix);
        else
            dir = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + prefix);

        if (!dir.exists())
            return;

        File[] files = dir.listFiles();

        for (int i = 0; i < Objects.requireNonNull(files).length; i++) {
            File file = files[i];

            String name = file.getName();
            String n = name.substring(0, name.length()-4);
            try {
                int num = Integer.parseInt(n);
                downloaded[num] = true;
            }
            catch (NumberFormatException ignored) {}
        }
    }

    private String createDir(int reciterID) {
        String text = "/Telawat Downloads/" + reciterID + "/" + versionId;

        File dir;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            dir = new File(context.getExternalFilesDir(null) + text);
        else
            dir = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + text);

        if (!dir.exists())
            dir.mkdirs();

        return text;
    }

    private void download(int num) {
        String server = ver.getUrl();
        String link = String.format(Locale.US, "%s/%03d.mp3", server, num+1);

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(
                Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(link);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle("تحميل التلاوة");
        request.setVisibleInDownloadsUi(true);
        request.setDestinationInExternalFilesDir(
                context, createDir(ver.getReciter_id()), num + ".mp3");
        request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        downloadManager.enqueue(request);

        downloaded[num] = true;
    }

    private void delete(int num) {
        String text = prefix + "/" + num + ".mp3";

        File file;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            file = new File(context.getExternalFilesDir(null) + text);
        else
            file = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + text);

        if (file.exists())
            file.delete();

        downloaded[num] = false;
    }

    private void updateFavorites() {
        Object[] favSuras = db.suraDao().getFav().toArray();

        Gson gson = new Gson();
        String surasJson = gson.toJson(favSuras);

        SharedPreferences.Editor editor = pref.edit();
        editor.putString("favorite_suras", surasJson);
        editor.apply();
    }
}
