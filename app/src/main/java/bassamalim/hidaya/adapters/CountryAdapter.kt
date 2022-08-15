package bassamalim.hidaya.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import bassamalim.hidaya.R
import bassamalim.hidaya.database.dbs.CountryDB

class CountryAdapter(
    private var original: List<CountryDB>,
    private val callback: Callback, private val language: String
): RecyclerView.Adapter<CountryAdapter.ViewHolder?>() {

    interface Callback {
        fun choice(id: Int)
    }

    private val items = original.toMutableList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val btn: Button

        init {
            btn = view.findViewById(R.id.btn)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_location, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        val name =
            if (language == "en") item.name_en
            else item.name_ar
        holder.btn.text = name

        holder.btn.setOnClickListener {
            callback.choice(item.id)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun filter(text: String) {
        items.clear()

        if (text.isEmpty()) items.addAll(original)
        else {
            if (language == "en") {
                for (item in original) {
                    if (item.name_en.contains(text)) items.add(item)
                }
            }
            else {
                for (item in original) {
                    if (item.name_ar.contains(text)) items.add(item)
                }
            }
        }

        notifyDataSetChanged()
    }

}