package bassamalim.hidaya.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import bassamalim.hidaya.R
import bassamalim.hidaya.adapters.BooksAdapter
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.database.dbs.BooksDB
import bassamalim.hidaya.databinding.ActivityBooksBinding
import bassamalim.hidaya.dialogs.TutorialDialog
import bassamalim.hidaya.other.Utils

class BooksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBooksBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BooksAdapter
    private lateinit var db: AppDatabase
    private lateinit var language: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.onActivityCreateSetTheme(this)
        language = Utils.onActivityCreateSetLocale(this)
        binding = ActivityBooksBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.home.setOnClickListener { onBackPressed() }

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "HidayaDB")
            .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build()

        checkFirstTime()

        setupRecycler()

        setListeners()
    }

    private fun checkFirstTime() {
        val key = "is_first_time_in_books_activity"
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(key, true))
            TutorialDialog(
            this, getString(R.string.books_activity_tips), key
            ).show(this.supportFragmentManager, TutorialDialog.TAG)
    }

    private fun setupRecycler() {
        recyclerView = findViewById(R.id.recycler)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        adapter = BooksAdapter(
            this, db.booksDao().getAll() as MutableList<BooksDB>, language,
        )
        recyclerView.adapter = adapter

        adapter.registerReceiver()
    }

    private fun setListeners() {
        binding.fab.setOnClickListener {
            val intent = Intent(this, BookSearcher::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.unregisterReceiver()
    }

}