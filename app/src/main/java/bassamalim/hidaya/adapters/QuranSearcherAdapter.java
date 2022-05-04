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

    private Context context;
    private final List<Ayah> suraCards;

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

    public QuranSearcherAdapter(List<Ayah> results) {
        suraCards = results;
    }

    @NonNull @Override
    public QuranSearcherAdapter.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup viewGroup, int viewType) {

        context = viewGroup.getContext();

        return new QuranSearcherAdapter.ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_quran_searcher, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(QuranSearcherAdapter.ViewHolder viewHolder, final int position) {
        Ayah card = suraCards.get(position);

        String suraNumStr = "سورة رقم " + Utils.translateNumbers(
                context, String.valueOf(card.getSurah()));
        viewHolder.suraNumTv.setText(suraNumStr);

        String suraNameStr = "سورة " + card.getSurahName();
        viewHolder.suraNameTv.setText(suraNameStr);

        String pageNumStr = "صفحة رقم " + Utils.translateNumbers(
                context, String.valueOf(card.getPageNum()));
        viewHolder.pageNumTv.setText(pageNumStr);

        String ayaNumStr = "آية رقم " + Utils.translateNumbers(
                context, String.valueOf(card.getAyah()));
        viewHolder.ayaNumTv.setText(ayaNumStr);

        viewHolder.ayaTextTv.setText(card.getSS());

        String ayaTafseerStr = "التفسير: " + card.getTafseer();
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
