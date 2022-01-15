package bassamalim.hidaya.adapters;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import bassamalim.hidaya.R;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.database.dbs.TelawatVersionsDB;
import bassamalim.hidaya.models.ReciterSurahCard;
import bassamalim.hidaya.other.Global;

public class TelawatSurahsAdapter extends RecyclerView.Adapter<TelawatSurahsAdapter.ViewHolder> {

    private final Context context;
    private final List<ReciterSurahCard> surahsCards;
    private final List<ReciterSurahCard> surahsCardsCopy;
    private final int versionId;
    private final TelawatVersionsDB ver;
    private final boolean[] downloaded = new boolean[114];
    private final String prefix;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView card;
        private final TextView namescreen;
        private final ImageButton imageButton;

        public ViewHolder(View view) {
            super(view);
            card = view.findViewById(R.id.surah_model_card);
            namescreen = view.findViewById(R.id.surah_namescreen);
            imageButton = view.findViewById(R.id.download_btn);
        }
    }

    public TelawatSurahsAdapter(Context context, ArrayList<ReciterSurahCard> cards,
                                int reciterId, int versionId) {
        this.context = context;
        AppDatabase db = Room.databaseBuilder(context, AppDatabase.class, "HidayaDB")
                .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build();

        surahsCards = new ArrayList<>(cards);
        surahsCardsCopy = new ArrayList<>(cards);

        this.versionId = versionId;

        ver = db.telawatVersionsDao().getVersion(reciterId, versionId);

        prefix = "/Telawat Downloads/" + ver.getReciter_id() + "/" + versionId;

        checkDownloaded();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_telawat_surah, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        int surahNum = surahsCards.get(position).getNum();

        viewHolder.namescreen.setText(surahsCards.get(position).getSurahName());
        viewHolder.card.setOnClickListener(surahsCards.get(position).getListener());

        if (downloaded[surahNum])
            viewHolder.imageButton.setImageDrawable(AppCompatResources.getDrawable(
                    context, R.drawable.ic_downloaded));
        else
            viewHolder.imageButton.setImageDrawable(AppCompatResources.getDrawable(
                    context, R.drawable.ic_download));

        viewHolder.imageButton.setOnClickListener(v -> {
            if (downloaded[surahsCards.get(position).getNum()]) {
                delete(surahsCards.get(position).getNum());

                viewHolder.imageButton.setImageDrawable(AppCompatResources.getDrawable(
                        context, R.drawable.ic_download));
            }
            else {
                download(surahsCards.get(position).getNum());

                viewHolder.imageButton.setImageDrawable(AppCompatResources.getDrawable(
                        context, R.drawable.ic_downloaded));
            }
        });
    }

    @Override
    public int getItemCount() {
        return surahsCards.size();
    }

    public void filter(String text) {
        surahsCards.clear();
        if (text.isEmpty())
            surahsCards.addAll(surahsCardsCopy);
        else {
            for(ReciterSurahCard reciterCard: surahsCardsCopy) {
                if (reciterCard.getSearchName().contains(text))
                    surahsCards.add(reciterCard);
            }
        }
        notifyDataSetChanged();
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

        Log.d(Global.TAG, "Exists");

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

        Log.d(Global.TAG, link);

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

        Log.d(Global.TAG, "Downloaded");
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

        Log.d(Global.TAG, "Deleted");
    }

}
