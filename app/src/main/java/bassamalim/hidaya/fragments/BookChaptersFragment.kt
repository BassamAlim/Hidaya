package bassamalim.hidaya.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bassamalim.hidaya.activities.BookViewer
import bassamalim.hidaya.adapters.BookChapterAdapter
import bassamalim.hidaya.databinding.FragmentBookChaptersBinding
import bassamalim.hidaya.enums.ListType
import bassamalim.hidaya.models.Book
import bassamalim.hidaya.models.BookChapter
import bassamalim.hidaya.other.Utils
import com.google.gson.Gson

class BookChaptersFragment(private val type: ListType, private val bookId: Int) : Fragment() {

    private var binding: FragmentBookChaptersBinding? = null
    private var recycler: RecyclerView? = null
    private var adapter: BookChapterAdapter? = null
    private lateinit var book: Book
    private lateinit var favs: BooleanArray

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookChaptersBinding.inflate(inflater, container, false)

        setupRecycler()

        setSearchListeners()

        return binding!!.root
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)

        if (menuVisible) {
            adapter = BookChapterAdapter(requireContext(), makeCards(), bookId)
            recycler!!.adapter = adapter
        }
    }

    private fun getData() {
        val path = requireContext().getExternalFilesDir(null).toString() + "/Books/" +
                bookId + ".json"
        val jsonStr = Utils.getJsonFromDownloads(path)
        val gson = Gson()
        book = gson.fromJson(jsonStr, Book::class.java)

        val pref: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        val favsStr: String = pref.getString("book" + bookId + "_favs", "")!!
        favs =
            if (favsStr.isEmpty()) BooleanArray(book.chapters.size)
            else gson.fromJson(favsStr, BooleanArray::class.java)
    }

    private fun makeCards(): ArrayList<BookChapter> {
        getData()

        val cards = ArrayList<BookChapter>()
        for (i in book.chapters.indices) {
            if (type == ListType.All || type == ListType.Favorite && favs[i]) {
                val chapterTitle = book.chapters[i].chapterTitle

                val listener = View.OnClickListener {
                    val intent = Intent(context, BookViewer::class.java)
                    intent.putExtra("book_id", bookId)
                    intent.putExtra("book_title", chapterTitle)
                    intent.putExtra("chapter_id", i)
                    startActivity(intent)
                }

                cards.add(BookChapter(i, chapterTitle, favs[i], listener))
            }
        }
        return cards
    }

    private fun setupRecycler() {
        recycler = binding!!.recycler
        val layoutManager = LinearLayoutManager(context)
        recycler!!.layoutManager = layoutManager
        adapter = BookChapterAdapter(requireContext(), makeCards(), bookId)
        recycler!!.adapter = adapter
    }

    private fun setSearchListeners() {
        binding!!.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                adapter?.filter(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter?.filter(newText)
                return true
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        recycler!!.adapter = null
        adapter = null
    }

}