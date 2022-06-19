package bassamalim.hidaya.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bassamalim.hidaya.R
import bassamalim.hidaya.adapters.BookSearcherAdapter
import bassamalim.hidaya.databinding.ActivityBookSearcherBinding
import bassamalim.hidaya.dialogs.FilterDialog
import bassamalim.hidaya.models.Book
import bassamalim.hidaya.models.BookSearcherMatch
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.other.Utils
import com.google.gson.Gson
import java.io.File
import java.util.*
import java.util.regex.Pattern

class BookSearcher : AppCompatActivity() {

    private var binding: ActivityBookSearcherBinding? = null
    private var pref: SharedPreferences? = null
    private var gson: Gson? = null
    private var bookTitles: Array<String>? = null
    private var matches: MutableList<BookSearcherMatch>? = null
    private var recyclerView: RecyclerView? = null
    private var searchView: SearchView? = null
    private var adapter: BookSearcherAdapter? = null
    private var maxMatches = 0
    private var selectedBooks: BooleanArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.myOnActivityCreated(this)
        binding = ActivityBookSearcherBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        binding!!.home.setOnClickListener { onBackPressed() }
        init()
        setListeners()
        initFilterIb()
        setupSizeSpinner()
    }

    private fun init() {
        pref = PreferenceManager.getDefaultSharedPreferences(this)
        gson = Gson()
        bookTitles = resources.getStringArray(R.array.books_titles)
        searchView = binding!!.searchView
        matches = ArrayList<BookSearcherMatch>()
        maxMatches = pref!!.getInt("books_searcher_matches_last_position", 10)
    }

    private fun setListeners() {
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                perform(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return true
            }
        })
        binding!!.filterIb.setOnClickListener { v ->
            FilterDialog(
                this, v, "اختر الكتب", bookTitles!!, selectedBooks!!,
                adapter!!, binding!!.filterIb, "selected_search_books"
            )
        }
    }

    private fun setupSizeSpinner() {
        val spinner: Spinner = binding!!.sizeSpinner
        val last: Int = pref!!.getInt("books_searcher_matches_last_position", 0)
        spinner.setSelection(last)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                vId: Long
            ) {
                maxMatches = spinner.getItemAtPosition(position).toString().toInt()
                val editor: SharedPreferences.Editor = pref!!.edit()
                editor.putInt("books_searcher_matches_last_position", position)
                editor.apply()
                if (adapter != null && adapter!!.itemCount > 0) perform(searchView!!.query.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun perform(query: String) {
        selectedBooks = getSelectedBooks()
        search(query)
        if (matches!!.isEmpty()) {
            binding!!.notFoundTv.visibility = View.VISIBLE
            binding!!.recycler.visibility = View.INVISIBLE
        } else {
            binding!!.notFoundTv.visibility = View.INVISIBLE
            binding!!.recycler.visibility = View.VISIBLE
            setupRecycler(matches)
            if (adapter != null) adapter!!.notifyDataSetChanged()
        }
    }

    private fun search(text: String) {
        matches!!.clear()
        val prefix = "/Books/"
        val dir = File(getExternalFilesDir(null).toString() + prefix)
        if (!dir.exists()) return
        for (i in bookTitles!!.indices) {
            if (!selectedBooks!![i]) continue
            val jsonStr = Utils.getJsonFromDownloads(
                getExternalFilesDir(null).toString() +
                        prefix + i + ".json"
            )
            val book: Book = try {
                gson!!.fromJson(jsonStr, Book::class.java)
            } catch (e: Exception) {
                Log.e(Global.TAG, "Error in json read in BookSearcher")
                e.printStackTrace()
                continue
            }
            for (j in book.chapters.indices) {
                val chapter = book.chapters[j]
                for (k in chapter.doors.indices) {
                    val door = chapter.doors[k]
                    val doorText = door.text
                    val m = Pattern.compile(text).matcher(doorText)
                    val ss: Spannable = SpannableString(doorText)
                    while (m.find()) {
                        ss.setSpan(
                            ForegroundColorSpan(getColor(R.color.highlight_M)),
                            m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        matches!!.add(
                            BookSearcherMatch(
                                i, book.bookInfo.bookTitle,
                                j, chapter.chapterTitle,
                                k, door.doorTitle, ss
                            )
                        )
                        if (matches!!.size == maxMatches) return
                    }
                }
            }
        }
    }

    private fun setupRecycler(matches: List<BookSearcherMatch>?) {
        recyclerView = binding!!.recycler
        val layoutManager = LinearLayoutManager(this)
        recyclerView!!.layoutManager = layoutManager
        adapter = BookSearcherAdapter(matches!!, searchView!!)
        recyclerView!!.adapter = adapter
    }

    private fun initFilterIb() {
        selectedBooks = getSelectedBooks()
        for (bool in selectedBooks!!) {
            if (!bool) {
                binding!!.filterIb.setImageDrawable(
                    AppCompatResources.getDrawable(this, R.drawable.ic_filtered)
                )
                break
            }
        }
    }

    private fun getSelectedBooks(): BooleanArray {
        val defArr = BooleanArray(bookTitles!!.size)
        Arrays.fill(defArr, true)
        val defStr: String = gson!!.toJson(defArr)
        return gson!!.fromJson(
            pref!!.getString("selected_search_books", defStr),
            BooleanArray::class.java
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
        if (recyclerView != null) recyclerView!!.adapter = null
        adapter = null
    }
}