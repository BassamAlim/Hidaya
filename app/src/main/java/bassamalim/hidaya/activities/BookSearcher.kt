package bassamalim.hidaya.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bassamalim.hidaya.R
import bassamalim.hidaya.adapters.BookSearcherAdapter
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.database.dbs.BooksDB
import bassamalim.hidaya.databinding.ActivityBookSearcherBinding
import bassamalim.hidaya.dialogs.FilterDialog
import bassamalim.hidaya.models.Book
import bassamalim.hidaya.models.BookSearcherMatch
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.DBUtils
import bassamalim.hidaya.utils.FileUtils
import bassamalim.hidaya.utils.PrefUtils
import com.google.gson.Gson
import java.io.File
import java.util.*
import java.util.regex.Pattern

class BookSearcher : AppCompatActivity() {

    private lateinit var binding: ActivityBookSearcherBinding
    private lateinit var pref: SharedPreferences
    private lateinit var db: AppDatabase
    private lateinit var gson: Gson
    private lateinit var books: List<BooksDB>
    private lateinit var matches: MutableList<BookSearcherMatch>
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private var adapter: BookSearcherAdapter? = null
    private var maxMatches = 0
    private lateinit var selectedBooks: BooleanArray
    private lateinit var language: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityUtils.onActivityCreateSetTheme(this)
        language = ActivityUtils.onActivityCreateSetLocale(this)
        binding = ActivityBookSearcherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.home.setOnClickListener { onBackPressed() }

        db = DBUtils.getDB(this)

        init()

        initFilterIb()

        setListeners()

        setupSizeSpinner()
    }

    private fun init() {
        pref = PreferenceManager.getDefaultSharedPreferences(this)

        gson = Gson()

        books = db.booksDao().getAll()

        searchView = binding.searchView

        matches = ArrayList<BookSearcherMatch>()

        maxMatches = pref.getInt("books_searcher_matches_last_position", 10)
    }

    private fun setListeners() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                perform(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return true
            }
        })

        val bookTitles = Array(books.size) { "" }    // how to create array in kotlin
        for (i in books.indices) {
            bookTitles[i] =
                if (language == "en") books[i].titleEn
                else books[i].title
        }
        binding.filterIb.setOnClickListener { v ->
            FilterDialog(
                this, v, getString(R.string.choose_books), bookTitles, selectedBooks,
                adapter, binding.filterIb, "selected_search_books"
            )
        }
    }

    private fun setupSizeSpinner() {
        val spinner: Spinner = binding.sizeSpinner

        val arrRes =
            if (PrefUtils.getNumeralsLanguage(this, pref) == "en") R.array.searcher_matches_en
            else R.array.searcher_matches


        val spinnerAdapter = ArrayAdapter.createFromResource(
            this, arrRes, android.R.layout.simple_spinner_item
        )
        spinner.adapter = spinnerAdapter
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val last = pref.getInt("books_searcher_matches_last_position", 0)
        spinner.setSelection(last)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View, position: Int, vId: Long
            ) {
                maxMatches = spinner.getItemAtPosition(position).toString().toInt()

                val editor = pref.edit()
                editor.putInt("books_searcher_matches_last_position", position)
                editor.apply()

                if (adapter != null && adapter!!.itemCount > 0) perform(searchView.query.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun perform(query: String) {
        selectedBooks = getSelectedBooks()
        search(query)

        if (matches.isEmpty()) {
            binding.notFoundTv.visibility = View.VISIBLE
            binding.recycler.visibility = View.INVISIBLE
        }
        else {
            binding.notFoundTv.visibility = View.INVISIBLE
            binding.recycler.visibility = View.VISIBLE

            setupRecycler(matches)

            adapter?.notifyDataSetChanged()
        }
    }

    private fun search(text: String) {
        matches.clear()

        val prefix = "/Books/"
        val dir = File(getExternalFilesDir(null).toString() + prefix)

        if (!dir.exists()) return

        for (i in books.indices) {
            if (!selectedBooks[i] || !downloaded(i)) continue

            val jsonStr = FileUtils.getJsonFromDownloads(
                getExternalFilesDir(null).toString() + prefix + i + ".json"
            )

            val book: Book = try {
                gson.fromJson(jsonStr, Book::class.java)
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

                        matches.add(
                            BookSearcherMatch(
                                i, book.bookInfo.bookTitle,
                                j, chapter.chapterTitle,
                                k, door.doorTitle, ss
                            )
                        )

                        if (matches.size == maxMatches) return
                    }
                }
            }
        }
    }

    private fun setupRecycler(matches: List<BookSearcherMatch>?) {
        recyclerView = binding.recycler
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        adapter = BookSearcherAdapter(matches!!, searchView)
        recyclerView.adapter = adapter
    }

    private fun initFilterIb() {
        selectedBooks = getSelectedBooks()
        for (bool in selectedBooks) {
            if (!bool) {
                binding.filterIb.setImageDrawable(
                    AppCompatResources.getDrawable(this, R.drawable.ic_filtered)
                )
                break
            }
        }
    }

    private fun getSelectedBooks(): BooleanArray {
        val defArr = BooleanArray(books.size)
        Arrays.fill(defArr, true)
        val defStr = gson.toJson(defArr)
        return gson.fromJson(
            pref.getString("selected_search_books", defStr),
            BooleanArray::class.java
        )
    }

    private fun downloaded(id: Int): Boolean {
        val dir = File(getExternalFilesDir(null).toString() + "/Books/")

        if (!dir.exists()) return false

        val files = dir.listFiles()
        for (element in files!!) {
            val name = element.name
            val n = name.substring(0, name.length - 5)
            try {
                val num = n.toInt()
                if (num == id) return true
            } catch (ignored: NumberFormatException) {}
        }
        return false
    }

}