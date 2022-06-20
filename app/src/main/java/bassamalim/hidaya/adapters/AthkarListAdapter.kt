package bassamalim.hidaya.adapters

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import bassamalim.hidaya.R
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.models.AthkarItem
import com.google.gson.Gson

class AthkarListAdapter(private val context: Context, cards: List<AthkarItem>) :
    RecyclerView.Adapter<AthkarListAdapter.ViewHolder?>() {

    private val db: AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "HidayaDB").createFromAsset(
            "databases/HidayaDB.db").allowMainThreadQueries().build()
    private val pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val items = cards as MutableList<AthkarItem>
    private val itemsCopy = cards

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: CardView
        val nameTv: TextView
        val favBtn: ImageButton

        init {
            card = view.findViewById(R.id.alathkar_model_card)
            nameTv = view.findViewById(R.id.namescreen)
            favBtn = view.findViewById(R.id.athkar_fav_btn)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(viewGroup.context).inflate(
                R.layout.item_athkar, viewGroup, false
            )
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val card: AthkarItem = items[position]

        viewHolder.nameTv.text = card.name

        val fav: Int = card.favorite
        if (fav == 0)
            viewHolder.favBtn.setImageDrawable(
                AppCompatResources.getDrawable(context, R.drawable.ic_star_outline)
        )
        else if (fav == 1)
            viewHolder.favBtn.setImageDrawable(
                AppCompatResources.getDrawable(context, R.drawable.ic_star)
        )

        viewHolder.card.setOnClickListener(card.listener)
        viewHolder.favBtn.setOnClickListener {
            if (card.favorite == 0) {
                db.athkarDao().setFav(card.id, 1)
                card.favorite = 1
            }
            else if (card.favorite == 1) {
                db.athkarDao().setFav(card.id, 0)
                card.favorite = 0
            }
            notifyItemChanged(position)

            updateFavorites()
        }
    }

    fun filter(text: String) {
        items.clear()
        if (text.isEmpty()) items.addAll(itemsCopy.toCollection(items))
        else {
            for (athkarItem in itemsCopy) {
                if (athkarItem.name.contains(text)) items.add(athkarItem)
            }
        }
        notifyDataSetChanged()
    }

    private fun updateFavorites() {
        val favAthkar: List<Int> = db.athkarDao().getFavs()

        val gson = Gson()
        val athkarJson: String = gson.toJson(favAthkar)

        val editor: SharedPreferences.Editor = pref.edit()
        editor.putString("favorite_athkar", athkarJson)
        editor.apply()
    }

    override fun getItemCount(): Int {
        return items.size
    }
}