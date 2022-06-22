package bassamalim.hidaya.adapters

import android.content.Context
import android.content.SharedPreferences
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import bassamalim.hidaya.R
import bassamalim.hidaya.models.BookDoor
import com.google.gson.Gson

class BookViewerAdapter(
    private val context: Context, private val items: ArrayList<BookDoor>,
    private val bookId: Int, private val chapterId: Int
) : RecyclerView.Adapter<BookViewerAdapter.ViewHolder?>() {

    private val margin = 15
    private val pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val gson: Gson = Gson()
    private var favs: BooleanArray? = null
    private var textSize: Int

    init {
        textSize = pref.getInt(context.getString(R.string.books_text_size_key), 15) + margin
        getFavs()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val card: CardView
        val titleTv: TextView
        val textTv: TextView
        val favBtn: ImageButton

        init {
            card = view.findViewById(R.id.book_door_model_card)
            titleTv = view.findViewById(R.id.title_tv)
            textTv = view.findViewById(R.id.text_tv)
            favBtn = view.findViewById(R.id.fav_btn)
        }
    }

    private fun getFavs() {
        val favsStr: String = pref.getString(
            "book" + bookId + "_chapter" + chapterId + "_favs", ""
        )!!

        favs =
            if (favsStr.isEmpty()) BooleanArray(items.size)
            else gson.fromJson(favsStr, BooleanArray::class.java)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder = ViewHolder(
            LayoutInflater.from(viewGroup.context).inflate(
                R.layout.item_book_door, viewGroup, false
            )
        )

        viewHolder.titleTv.textSize = textSize.toFloat()
        viewHolder.textTv.textSize = textSize.toFloat()

        return viewHolder
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val card = items[position]

        viewHolder.titleTv.text = items[position].getDoorTitle()

        viewHolder.textTv.text = items[position].getText()

        val fav = card.isFav()
        if (fav)
            viewHolder.favBtn.setImageDrawable(
                AppCompatResources.getDrawable(context, R.drawable.ic_star))
        else
            viewHolder.favBtn.setImageDrawable(
            AppCompatResources.getDrawable(context, R.drawable.ic_star_outline))

        viewHolder.favBtn.setOnClickListener {
            if (card.isFav()) {
                favs!![items[position].getDoorId()] = false
                card.setFav(false)
            }
            else {
                favs!![items[position].getDoorId()] = true
                card.setFav(true)
            }
            notifyItemChanged(position)
            updateFavorites()
        }
    }

    private fun updateFavorites() {
        val favStr: String = gson.toJson(favs)
        val editor: SharedPreferences.Editor = pref.edit()
        editor.putString("book" + bookId + "_chapter" + chapterId + "_favs", favStr)
        editor.apply()
    }

    fun setTextSize(textSize: Int) {
        this.textSize = textSize + margin
    }

    override fun getItemCount(): Int {
        return items.size
    }

}