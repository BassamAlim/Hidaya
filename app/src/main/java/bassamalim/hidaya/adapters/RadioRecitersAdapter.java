package bassamalim.hidaya.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import bassamalim.hidaya.R;
import bassamalim.hidaya.models.ReciterCard;

import java.util.ArrayList;

public class RadioRecitersAdapter extends RecyclerView.Adapter<RadioRecitersAdapter.ViewHolder> {

    private final ArrayList<ReciterCard> recitersCards;
    private final ArrayList<ReciterCard> recitersCardsCopy;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView card;

        public ViewHolder(View view) {
            super(view);

            card = view.findViewById(R.id.reciter_model_card);
        }

        public CardView getCard() {
            return card;
        }
    }

    public RadioRecitersAdapter(ArrayList<ReciterCard> cards) {
        recitersCards = new ArrayList<>(cards);
        recitersCardsCopy = new ArrayList<>(cards);
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.radio_reciter_row_item, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        ReciterCard card = recitersCards.get(position);
        View cardView = viewHolder.getCard();

        ((TextView) cardView.findViewById(R.id.reciter_namescreen)).setText(card.getName());

        Button[] buttons = new Button[5];
        buttons[0] = cardView.findViewById(R.id.version1);
        buttons[1] = cardView.findViewById(R.id.version2);
        buttons[2] = cardView.findViewById(R.id.version3);
        buttons[3] = cardView.findViewById(R.id.version4);
        buttons[4] = cardView.findViewById(R.id.version5);

        int length = card.getVersions().length;
        for (int i = 0; i < 5; i++) {
            if (i < length) {
                buttons[i].setVisibility(View.VISIBLE);
                buttons[i].setText(card.getVersions()[i].getRewaya());
                buttons[i].setOnClickListener(card.getVersions()[i].getListener());
            }
            else
                buttons[i].setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return recitersCards.size();
    }

    public void filter(String text) {
        recitersCards.clear();
        if (text.isEmpty())
            recitersCards.addAll(recitersCardsCopy);
        else {
            for(ReciterCard reciterCard: recitersCardsCopy) {
                if (reciterCard.getName().contains(text))
                    recitersCards.add(reciterCard);
            }
        }
        notifyDataSetChanged();
    }

}
