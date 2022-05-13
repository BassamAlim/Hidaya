package bassamalim.hidaya.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import bassamalim.hidaya.R;
import bassamalim.hidaya.models.Ayah;

public class RecyclerQuranViewerAdapter
        extends RecyclerView.Adapter<RecyclerQuranViewerAdapter.ViewHolder> {

    private final String LANGUAGE;
    private final String THEME;
    private final int MARGIN = 15;
    private final List<Ayah> items;
    private int textSize;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView suraNameTv;
        private final TextView basmalah;
        private final TextView textTv;
        private final TextView translationTv;

        public ViewHolder(View view) {
            super(view);

            suraNameTv = view.findViewById(R.id.sura_name_tv);
            basmalah = view.findViewById(R.id.basmalah_tv);
            textTv = view.findViewById(R.id.text_tv);
            translationTv = view.findViewById(R.id.translation_tv);
        }
    }

    public RecyclerQuranViewerAdapter(Context context, List<Ayah> items,
                                      String theme,  String language) {
        THEME = theme;
        LANGUAGE = language;
        this.items = items;

        textSize = PreferenceManager.getDefaultSharedPreferences(context).getInt(
                context.getString(R.string.alathkar_text_size_key), 15) + MARGIN;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        ViewHolder viewHolder = new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_recycler_quran_viewer, viewGroup, false));

        if (THEME.equals("ThemeM"))
            viewHolder.suraNameTv.setBackgroundResource(R.drawable.surah_header);
        else
            viewHolder.suraNameTv.setBackgroundResource(R.drawable.surah_header_light);

        viewHolder.suraNameTv.setTextSize(textSize+5);
        viewHolder.basmalah.setTextSize(textSize);
        viewHolder.textTv.setTextSize(textSize);
        viewHolder.translationTv.setTextSize(textSize);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        Ayah aya = items.get(position);

        header(viewHolder, aya);

        viewHolder.textTv.setText(aya.getText());
        viewHolder.translationTv.setText(aya.getTranslation());
    }

    private void header(ViewHolder vh, Ayah aya) {
        if (aya.getAyahNum() == 1) {
            vh.suraNameTv.setText(aya.getSurahName());
            vh.suraNameTv.setVisibility(View.VISIBLE);

            if (aya.getSurahNum() != 1 && aya.getAyahNum() != 9)   // surat al-fatiha and At-Taubah
                vh.basmalah.setVisibility(View.VISIBLE);
        }
        else {
            vh.suraNameTv.setVisibility(View.GONE);
            vh.basmalah.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
