package bassamalim.hidaya.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import bassamalim.hidaya.R
import bassamalim.hidaya.models.BookSearcherMatch
import bassamalim.hidaya.replacements.FilteredRecyclerAdapter

class BookSearcherAdapter(
    private val items: List<BookSearcherMatch>,
    private val searchView: SearchView) :
    FilteredRecyclerAdapter<BookSearcherAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bookTitleTv: TextView
        val chapterTitleTv: TextView
        val doorTitleTv: TextView
        val textTv: TextView

        init {
            bookTitleTv = view.findViewById(R.id.book_title_tv)
            chapterTitleTv = view.findViewById(R.id.chapter_title_tv)
            doorTitleTv = view.findViewById(R.id.door_title_tv)
            textTv = view.findViewById(R.id.text_tv)
        }
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup, viewType: Int
    ): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_book_searcher, viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val card = items[position]
        viewHolder.bookTitleTv.text = card.getBookTitle()
        viewHolder.chapterTitleTv.text = card.getChapterTitle()
        viewHolder.doorTitleTv.text = card.getDoorTitle()
        viewHolder.textTv.text = card.getText()
    }

    override fun filter(text: String?, selected: BooleanArray?) {
        searchView.setQuery(searchView.query, true)
    }

    override fun getItemCount(): Int {
        return items.size
    }
}