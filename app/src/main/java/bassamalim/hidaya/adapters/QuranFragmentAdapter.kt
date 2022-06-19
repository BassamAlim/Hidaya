package bassamalim.hidaya.adapters

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import bassamalim.hidaya.R
import bassamalim.hidaya.activities.QuranViewer
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.models.Sura
import com.google.gson.Gson

class QuranFragmentAdapter(private val context: Context, buttons: ArrayList<Sura>) :
    RecyclerView.Adapter<QuranFragmentAdapter.ViewHolder?>() {

    private val db: AppDatabase = Room.databaseBuilder(
        context, AppDatabase::class.java, "HidayaDB")
        .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build()
    private val pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val gson: Gson = Gson()
    private val items: ArrayList<Sura>
    private val itemsCopy: ArrayList<Sura>

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: CardView
        val namescreen: TextView
        val tanzeelView: ImageView
        val favBtn: ImageButton

        init {
            card = view.findViewById(R.id.surah_button_model)
            namescreen = view.findViewById(R.id.namescreen)
            tanzeelView = view.findViewById(R.id.tanzeel_view)
            favBtn = view.findViewById(R.id.sura_fav_btn)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(
                context
            ).inflate(
                R.layout.item_quran_fragment,
                viewGroup, false
            )
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val card: Sura = items[position]
        viewHolder.namescreen.text = card.getSuraName()
        setTanzeel(viewHolder, card)
        doFavorites(viewHolder, card, position)
        viewHolder.card.setOnClickListener(card.getCardListener())
    }

    private fun setTanzeel(vh: ViewHolder, card: Sura) {
        val tanzeel: Int = card.getTanzeel()
        if (tanzeel == 0) // Makkah
            vh.tanzeelView.setImageDrawable(
                AppCompatResources.getDrawable(context, R.drawable.ic_kaaba_black)
            ) else if (tanzeel == 1) // Madina
            vh.tanzeelView.setImageDrawable(
                AppCompatResources.getDrawable(context, R.drawable.ic_madinah)
            )
    }

    private fun doFavorites(vh: ViewHolder, card: Sura, position: Int) {
        val fav: Int = card.getFavorite()
        if (fav == 0) vh.favBtn.setImageDrawable(
            AppCompatResources.getDrawable(context, R.drawable.ic_star_outline)
        ) else if (fav == 1) vh.favBtn.setImageDrawable(
            AppCompatResources.getDrawable(context, R.drawable.ic_star)
        )
        vh.favBtn.setOnClickListener {
            if (card.getFavorite() == 0) {
                db.suarDao().setFav(card.getNumber(), 1)
                card.setFavorite(1)
            } else if (card.getFavorite() == 1) {
                db.suarDao().setFav(card.getNumber(), 0)
                card.setFavorite(0)
            }
            notifyItemChanged(position)
            updateFavorites()
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun filterName(text: String) {
        items.clear()
        if (text.isEmpty()) items.addAll(itemsCopy) else {
            for (suraCard in itemsCopy) {
                if (suraCard.getSearchName().contains(text)) items.add(suraCard)
            }
        }
        notifyDataSetChanged()
    }

    fun filterNumber(text: String) {
        items.clear()
        if (text.isEmpty()) items.addAll(itemsCopy) else {
            try {
                val num = text.toInt()
                if (num in 1..604) {
                    val openPage = Intent(context, QuranViewer::class.java)
                    openPage.action = "by_page"
                    openPage.putExtra("page", num)
                    context.startActivity(openPage)
                } else Toast.makeText(
                    context, context.getString(R.string.page_does_not_exist),
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: NumberFormatException) {
                for (suraCard in itemsCopy) {
                    if (suraCard.getSearchName().contains(text)) items.add(suraCard)
                }
            }
        }
        notifyDataSetChanged()
    }

    private fun updateFavorites() {
        val favSuras: List<Any> = db.suarDao().getFav()
        val surasJson: String = gson.toJson(favSuras)
        val editor: SharedPreferences.Editor = pref.edit()
        editor.putString("favorite_suras", surasJson)
        editor.apply()
    }

    init {
        items = ArrayList(buttons)
        itemsCopy = ArrayList(buttons)
    }
}