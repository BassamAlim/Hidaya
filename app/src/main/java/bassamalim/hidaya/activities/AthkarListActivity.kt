package bassamalim.hidaya.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import bassamalim.hidaya.R
import bassamalim.hidaya.adapters.AthkarListAdapter
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.database.dbs.AthkarDB
import bassamalim.hidaya.database.dbs.ThikrsDB
import bassamalim.hidaya.databinding.ActivityAthkarListBinding
import bassamalim.hidaya.models.AthkarItem
import bassamalim.hidaya.other.Utils

class AthkarListActivity : AppCompatActivity() {

    private var binding: ActivityAthkarListBinding? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: AthkarListAdapter? = null
    private var category = 0
    private var action: String? = null
    private var db: AppDatabase? = null
    private var language: String? = null
    private var names: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.onActivityCreateSetTheme(this)
        language = Utils.onActivityCreateSetLocale(this)
        binding = ActivityAthkarListBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        binding!!.home.setOnClickListener { onBackPressed() }

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "HidayaDB")
            .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build()

        action = intent.action
        when (action) {
            "all" -> binding!!.topBarTitle.text = getString(R.string.all_athkar)
            "favorite" -> binding!!.topBarTitle.text =
                getString(R.string.favorite_athkar)
            else -> {
                category = intent.getIntExtra("category", 0)

                val topBarTitle: String = if (language == "en") db!!.athkarCategoryDao()
                    .getNameEn(category) else db!!.athkarCategoryDao().getName(category)
                binding!!.topBarTitle.text = topBarTitle
            }
        }

        setupRecycler()

        setSearchListeners()
    }

    private val data: List<AthkarDB>
        get() {
            names =
                if (language == "en") db!!.athkarDao().getNamesEn()
                else db!!.athkarDao().getNames()

            return when (action) {
                "all" -> db!!.athkarDao().getAll()
                "favorite" -> db!!.athkarDao().getFavorites()
                else -> db!!.athkarDao().getList(category)
            }
        }

    private fun makeButtons(athkar: List<AthkarDB>): List<AthkarItem> {
        val buttons: MutableList<AthkarItem> = ArrayList()
        val favs: List<Int> = db!!.athkarDao().getFavs()

        for (i in athkar.indices) {
            val thikr: AthkarDB = athkar[i]

            if (language == "en" && !hasEn(thikr)) continue

            val clickListener = View.OnClickListener {
                val intent = Intent(this, AthkarViewer::class.java)
                intent.action = action
                intent.putExtra("thikr_id", thikr.athkar_id)
                startActivity(intent)
            }

            val fav = if (action == "favorite") 1 else favs[thikr.athkar_id]

            buttons.add(
                AthkarItem(
                    thikr.athkar_id, thikr.category_id,
                    names!![thikr.athkar_id], fav, clickListener
                )
            )
        }
        return buttons
    }

    private fun hasEn(thikr: AthkarDB): Boolean {
        val ts: List<ThikrsDB> = db!!.thikrsDao().getThikrs(thikr.athkar_id)
        for (i in ts.indices) {
            val t: ThikrsDB = ts[i]
            if (t.getText_en().length > 1) return true
        }
        return false
    }

    private fun setupRecycler() {
        recyclerView = findViewById(R.id.recycler)
        val layoutManager = LinearLayoutManager(this)
        recyclerView!!.layoutManager = layoutManager
        adapter = AthkarListAdapter(this, makeButtons(data))
        recyclerView!!.adapter = adapter
    }

    private fun setSearchListeners() {
        binding!!.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                adapter!!.filter(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter!!.filter(newText)
                return true
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
        recyclerView!!.adapter = null
        adapter = null
    }
}