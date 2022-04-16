package bassamalim.hidaya.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import bassamalim.hidaya.R;
import bassamalim.hidaya.models.ThikrCard;

public class AthkarViewerAdapter extends RecyclerView.Adapter<AthkarViewerAdapter.ViewHolder> {

    private final ArrayList<ThikrCard> thikrCards;
    private final int textSize;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTv;
        private final TextView textTv;
        private final TextView fadlTv;
        private final TextView referenceTv;
        private final TextView repetitionTv;
        private final View repetitionDiv;
        private final View fadlDiv;
        private final View referenceDiv;

        public ViewHolder(View view) {
            super(view);
            titleTv = view.findViewById(R.id.title_tv);
            textTv = view.findViewById(R.id.text_tv);
            fadlTv = view.findViewById(R.id.fadl_tv);
            referenceTv = view.findViewById(R.id.reference_tv);
            repetitionTv = view.findViewById(R.id.repetition_tv);
            repetitionDiv = view.findViewById(R.id.repetition_div);
            fadlDiv = view.findViewById(R.id.fadl_div);
            referenceDiv = view.findViewById(R.id.reference_div);
        }
    }

    public AthkarViewerAdapter(Context context, ArrayList<ThikrCard> cards) {
        thikrCards = cards;

        textSize = PreferenceManager.getDefaultSharedPreferences(context).getInt(
                context.getString(R.string.alathkar_text_size_key), 25);
    }

    @NonNull @Override
    public AthkarViewerAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        AthkarViewerAdapter.ViewHolder viewHolder = new AthkarViewerAdapter.ViewHolder(
                LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_thikr,
                        viewGroup, false));

        viewHolder.titleTv.setTextSize(textSize);
        viewHolder.textTv.setTextSize(textSize);
        //viewHolder.textTv.font
        viewHolder.fadlTv.setTextSize(textSize-8);
        viewHolder.referenceTv.setTextSize(textSize-8);
        viewHolder.repetitionTv.setTextSize(textSize);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull AthkarViewerAdapter.ViewHolder viewHolder,
                                 final int position) {
        ThikrCard card = thikrCards.get(position);

        viewHolder.titleTv.setText(card.getTitle());
        viewHolder.textTv.setText(card.getText());
        viewHolder.fadlTv.setText(card.getFadl());
        viewHolder.referenceTv.setText(card.getReference());
        viewHolder.repetitionTv.setText(card.getRepetition());

        if (card.getRepetition().equals("1")) {
            viewHolder.repetitionTv.setVisibility(View.GONE);
            viewHolder.repetitionDiv.setVisibility(View.GONE);
        }
        if (card.getTitle() == null || card.getTitle().length() == 0)
            viewHolder.titleTv.setVisibility(View.GONE);
        if (card.getFadl() == null || card.getFadl().length() == 0) {
            viewHolder.fadlTv.setVisibility(View.GONE);
            viewHolder.fadlDiv.setVisibility(View.GONE);
        }
        if (card.getReference() == null || card.getReference().length() == 0) {
            viewHolder.referenceTv.setVisibility(View.GONE);
            viewHolder.referenceDiv.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return thikrCards.size();
    }
}
