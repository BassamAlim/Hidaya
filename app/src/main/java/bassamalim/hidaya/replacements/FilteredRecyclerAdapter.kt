package bassamalim.hidaya.replacements

import androidx.recyclerview.widget.RecyclerView

abstract class FilteredRecyclerAdapter<VH : RecyclerView.ViewHolder?> : RecyclerView.Adapter<VH>() {

    abstract fun filter(text: String?, selected: BooleanArray?)

}