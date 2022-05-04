package bassamalim.hidaya.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import bassamalim.hidaya.R;
import bassamalim.hidaya.activities.QuranActivity;
import bassamalim.hidaya.models.Ayah;
import bassamalim.hidaya.other.Utils;

public class QuranSearcherAdapter extends RecyclerView.Adapter<QuranSearcherAdapter.ViewHolder> {

    private final Context context;
    private final List<Ayah> suraCards;
    private final String suraStr;
    private final String tafseerString;
    private final String suraNumString;
    private final String pageNumString;
    private final String ayaNumString;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView suraNumTv;
        private final TextView suraNameTv;
        private final TextView pageNumTv;
        private final TextView ayaNumTv;
        private final TextView ayaTextTv;
        private final TextView ayaTafseerTv;
        private final Button gotoPageBtn;

        public ViewHolder(View view) {
            super(view);
            suraNumTv = view.findViewById(R.id.sura_num_tv);
            suraNameTv = view.findViewById(R.id.sura_name_tv);
            pageNumTv = view.findViewById(R.id.page_num_tv);
            ayaNumTv = view.findViewById(R.id.aya_num_tv);
            ayaTextTv = view.findViewById(R.id.aya_text_tv);
            ayaTafseerTv = view.findViewById(R.id.aya_tafseer_tv);
            gotoPageBtn = view.findViewById(R.id.goto_page);
        }
    }

    public QuranSearcherAdapter(Context context, List<Ayah> results) {
        this.context = context;

        suraCards = results;
        suraStr = context.getString(R.string.sura);
        suraNumString = context.getString(R.string.sura_number);
        pageNumString = context.getString(R.string.page_number);
        ayaNumString = context.getString(R.string.aya_number);
        tafseerString = context.getString(R.string.tafseer);
    }

    @NonNull @Override
    public QuranSearcherAdapter.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup viewGroup, int viewType) {
        return new QuranSearcherAdapter.ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_quran_searcher, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(QuranSearcherAdapter.ViewHolder viewHolder, final int position) {
        Ayah card = suraCards.get(position);

        String suraNumStr = suraNumString + " " + Utils.translateNumbers(
                context, String.valueOf(card.getSurah()));
        viewHolder.suraNumTv.setText(suraNumStr);

        String suraNameStr = suraStr +  " " + card.getSurahName();
        viewHolder.suraNameTv.setText(suraNameStr);

        String pageNumStr = pageNumString + " " + Utils.translateNumbers(
                context, String.valueOf(card.getPageNum()));
        viewHolder.pageNumTv.setText(pageNumStr);

        String ayaNumStr = ayaNumString + " " + Utils.translateNumbers(
                context, String.valueOf(card.getAyah()));
        viewHolder.ayaNumTv.setText(ayaNumStr);

        viewHolder.ayaTextTv.setText(card.getSS());

        String ayaTafseerStr = tafseerString + ": " + card.getTafseer();
        viewHolder.ayaTafseerTv.setText(ayaTafseerStr);

        viewHolder.gotoPageBtn.setOnClickListener(v -> {
            Intent intent = new Intent(context, QuranActivity.class);
            intent.setAction("by_page");
            intent.putExtra("page", card.getPageNum());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return suraCards.size();
    }
}
