package bassamalim.hidaya.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import bassamalim.hidaya.R
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.database.dbs.CityDB

class CityAdapter(
    private var items: MutableList<CityDB>, private val callback: Callback,
    private val db: AppDatabase, private val countryId: Int, private val language: String
    ): RecyclerView.Adapter<CityAdapter.ViewHolder?>() {

    interface Callback {
        fun choice(id: Int)
    }

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
            if (language == "en") item.nameEn
            else item.nameAr
        holder.btn.text = name

        holder.btn.setOnClickListener {
            callback.choice(item.id)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun filter(text: String) {
        items =
            if (language == "en") db.cityDao().getTopEn(countryId, text).toMutableList()
            else db.cityDao().getTopAr(countryId, text).toMutableList()

        notifyDataSetChanged()
    }

}