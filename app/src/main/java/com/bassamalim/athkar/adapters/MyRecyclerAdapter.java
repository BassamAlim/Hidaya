package com.bassamalim.athkar.adapters;

import android.content.Context;
import android.content.Intent;
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
import com.bassamalim.athkar.views.QuranView;
import java.util.ArrayList;

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> surahNames;
    private ArrayList<String> surahNamesCopy;

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

        public Button getButton() {
            return button;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param gContext
     * @param names String[] containing the data to populate views to be used
     */
    public MyRecyclerAdapter(Context gContext, ArrayList<String> names) {
        context = gContext;
        surahNames = new ArrayList<>(names);
        surahNamesCopy = new ArrayList<>(names);
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

        viewHolder.getButton().setText(surahNames.get(position));
        viewHolder.button.setOnClickListener(v -> {
            Intent intent = new Intent(context, QuranView.class);
            intent.putExtra("surah index", position);
            context.startActivity(intent);
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return surahNames.size();
    }

    public void filter(String text) {
        surahNames.clear();
        if(text.isEmpty())
            surahNames.addAll(surahNamesCopy);
        else {
            //text = text.toLowerCase();
            for(String surahName: surahNamesCopy) {
                if(surahName.contains(text))
                    surahNames.add(surahName);
            }
        }
        //not so efficient
        notifyDataSetChanged();
        //notifyItemChanged();
    }

}
