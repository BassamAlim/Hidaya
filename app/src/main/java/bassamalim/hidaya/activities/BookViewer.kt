package bassamalim.hidaya.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bassamalim.hidaya.R
import bassamalim.hidaya.adapters.BookViewerAdapter
import bassamalim.hidaya.databinding.ActivityBookViewerBinding
import bassamalim.hidaya.models.Book
import bassamalim.hidaya.models.BookDoor
import bassamalim.hidaya.other.Utils
import com.google.gson.Gson
import java.util.*

class BookViewer : AppCompatActivity() {

    private var binding: ActivityBookViewerBinding? = null
    private var pref: SharedPreferences? = null
    private var bookId = 0
    private var chapterId = 0
    private var doors: Array<Book.BookChapter.BookDoor>? = null
    private var favs: BooleanArray? = null
    private var recycler: RecyclerView? = null
    private var textSizeSb: SeekBar? = null
    private var adapter: BookViewerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.myOnActivityCreated(this)
        binding = ActivityBookViewerBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        binding!!.home.setOnClickListener { onBackPressed() }

        pref = PreferenceManager.getDefaultSharedPreferences(this)

        val intent: Intent = intent
        bookId = intent.getIntExtra("book_id", 0)
        chapterId = intent.getIntExtra("chapter_id", 0)
        binding!!.topBarTitle.text = intent.getStringExtra("book_title")

        getData()

        setupRecycler()

        setupListeners()
    }

    private fun getData() {
        val path: String = getExternalFilesDir(null).toString() + "/Books/" + bookId + ".json"
        val jsonStr = Utils.getJsonFromDownloads(path)
        val gson = Gson()
        val book: Book = gson.fromJson(jsonStr, Book::class.java)
        doors = book.chapters[chapterId].doors

        val favsStr: String = PreferenceManager.getDefaultSharedPreferences(this)
            .getString("book" + bookId + "_chapter" + chapterId + "_favs", "")!!
        favs =
            if (favsStr.isEmpty()) BooleanArray(doors!!.size)
            else gson.fromJson(favsStr, BooleanArray::class.java)
    }

    private fun makeCards(): ArrayList<BookDoor> {
        val cards = ArrayList<BookDoor>()
        for (i in doors!!.indices) {
            val door = doors!![i]
            cards.add(BookDoor(door.doorId, door.doorTitle, door.text, favs!![i]))
        }
        return cards
    }

    private fun setupRecycler() {
        recycler = binding!!.recycler
        val layoutManager = LinearLayoutManager(this)
        recycler!!.layoutManager = layoutManager
        adapter = BookViewerAdapter(this, makeCards(), bookId, chapterId)
        recycler!!.adapter = adapter
    }

    private fun setupListeners() {
        textSizeSb = binding!!.textSizeSb

        textSizeSb!!.progress = pref!!.getInt(getString(R.string.books_text_size_key), 15)

        binding!!.textSizeIb.setOnClickListener {
            if (textSizeSb!!.visibility == View.GONE) textSizeSb!!.visibility = View.VISIBLE
            else textSizeSb!!.visibility = View.GONE
        }

        textSizeSb!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val editor: SharedPreferences.Editor = pref!!.edit()
                editor.putInt(getString(R.string.books_text_size_key), seekBar.progress)
                editor.apply()

                adapter!!.setTextSize(seekBar.progress)
                recycler!!.adapter = null
                recycler!!.adapter = adapter
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
        recycler!!.adapter = null
        adapter = null
    }
}