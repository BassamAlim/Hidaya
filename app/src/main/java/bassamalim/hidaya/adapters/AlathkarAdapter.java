package bassamalim.hidaya.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import bassamalim.hidaya.R;
import bassamalim.hidaya.models.AlathkarButton;

import java.util.ArrayList;

public class AlathkarAdapter extends RecyclerView.Adapter<AlathkarAdapter.ViewHolder> {

    private final ArrayList<AlathkarButton> alathkarButtons;
    private final ArrayList<AlathkarButton> alathkarButtonsCopy;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final Button button;

        public ViewHolder(View view) {
            super(view);
            button = view.findViewById(R.id.alathkar_model_button);
        }

        public Button getButton() {
            return button;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param buttons containing the data to populate views to be used
     */
    public AlathkarAdapter(ArrayList<AlathkarButton> buttons) {
        alathkarButtons = new ArrayList<>(buttons);
        alathkarButtonsCopy = new ArrayList<>(buttons);
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.alathkar_row_item,
                viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.getButton().setText(alathkarButtons.get(position).getName());
        viewHolder.getButton().setOnClickListener(alathkarButtons.get(position).getListener());
    }

    @Override
    public int getItemCount() {
        return alathkarButtons.size();
    }

    public void filter(String text) {
        alathkarButtons.clear();
        if (text.isEmpty())
            alathkarButtons.addAll(alathkarButtonsCopy);
        else {
            for(AlathkarButton alathkarButton: alathkarButtonsCopy) {
                if(alathkarButton.getName().contains(text))
                    alathkarButtons.add(alathkarButton);
            }
        }
        notifyDataSetChanged();
    }

}

