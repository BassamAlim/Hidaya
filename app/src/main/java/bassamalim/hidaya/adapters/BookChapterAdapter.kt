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
import bassamalim.hidaya.R
import bassamalim.hidaya.models.BookChapter
import com.google.gson.Gson
import java.util.ArrayList

class BookChapterAdapter(
    private val context: Context,
    private val original: ArrayList<BookChapter>, private val bookId: Int)
    : RecyclerView.Adapter<BookChapterAdapter.ViewHolder?>() {

    private val pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val gson: Gson = Gson()
    private val items = ArrayList(original)
    private var favs: BooleanArray? = null

    init {
        getFavs()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: CardView
        val tv: TextView
        val favBtn: ImageButton

        init {
            card = view.findViewById(R.id.book_chapter_model_card)
            tv = view.findViewById(R.id.chapter_tv)
            favBtn = view.findViewById(R.id.fav_btn)
        }
    }

    private fun getFavs() {
        val favsStr: String = pref.getString("book" + bookId + "_favs", "")!!
        favs =
            if (favsStr.isEmpty()) BooleanArray(items.size)
            else gson.fromJson(favsStr, BooleanArray::class.java)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(viewGroup.context).inflate(
                R.layout.item_book_chapter, viewGroup, false
            )
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val card = items[position]

        viewHolder.tv.text = items[position].chapterTitle

        val fav = card.favorite
        if (fav)
            viewHolder.favBtn.setImageDrawable(
                AppCompatResources.getDrawable(context, R.drawable.ic_star))
        else
            viewHolder.favBtn.setImageDrawable(
                AppCompatResources.getDrawable(context, R.drawable.ic_star_outline))

        viewHolder.card.setOnClickListener(card.listener)

        viewHolder.favBtn.setOnClickListener {
            if (card.favorite) {
                favs!![items[position].chapterId] = false
                card.favorite = false
            }
            else {
                favs!![items[position].chapterId] = true
                card.favorite = true
            }
            notifyItemChanged(position)
            updateFavorites()
        }
    }

    fun filter(text: String) {
        items.clear()
        if (text.isEmpty()) items.addAll(original)
        else {
            for (chapterCard in original) {
                if (chapterCard.chapterTitle.contains(text)) items.add(chapterCard)
            }
        }
        notifyDataSetChanged()
    }

    private fun updateFavorites() {
        val favStr: String = gson.toJson(favs)
        val editor: SharedPreferences.Editor = pref.edit()
        editor.putString("book" + bookId + "_favs", favStr)
        editor.apply()
    }

    override fun getItemCount(): Int {
        return items.size
    }

}