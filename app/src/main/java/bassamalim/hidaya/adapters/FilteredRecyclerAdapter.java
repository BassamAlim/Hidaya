package bassamalim.hidaya.adapters;

import androidx.recyclerview.widget.RecyclerView;

public abstract class FilteredRecyclerAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    public abstract void filter(String text, boolean[] selected);

}
