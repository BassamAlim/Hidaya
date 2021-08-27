package com.bassamalim.athkar.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bassamalim.athkar.Constants;
import com.bassamalim.athkar.R;
import com.bassamalim.athkar.models.SurahButton;
import java.util.ArrayList;

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> {

    private final ArrayList<SurahButton> surahButtons;
    private final ArrayList<SurahButton> surahButtonsCopy;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final Button button;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            view.setOnClickListener(v -> Log.i(Constants.TAG, "clicked"));

            button = view.findViewById(R.id.model_button);
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param buttons containing the data to populate views to be used
     */
    public MyRecyclerAdapter(ArrayList<SurahButton> buttons) {
        surahButtons = new ArrayList<>(buttons);
        surahButtonsCopy = new ArrayList<>(buttons);
    }

    // Create new views (invoked by the layout manager)
    @NonNull @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.button_row_item, viewGroup, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.button.setText(surahButtons.get(position).getSurahName());
        viewHolder.button.setOnClickListener(surahButtons.get(position).getListener());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return surahButtons.size();
    }

    public void filter(String text) {
        surahButtons.clear();
        if(text.isEmpty())
            surahButtons.addAll(surahButtonsCopy);
        else {
            //text = text.toLowerCase();
            for(SurahButton surahButton: surahButtonsCopy) {
                if(surahButton.getSearchName().contains(text))
                    surahButtons.add(surahButton);
            }
        }
        //not so efficient
        notifyDataSetChanged();
        //notifyItemChanged();
    }

}
