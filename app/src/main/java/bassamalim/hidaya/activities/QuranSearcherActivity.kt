package bassamalim.hidaya.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.AdapterView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import bassamalim.hidaya.R
import bassamalim.hidaya.adapters.QuranSearcherAdapter
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.database.dbs.AyatDB
import bassamalim.hidaya.databinding.ActivityQuranSearcherBinding
import bassamalim.hidaya.models.Ayah
import bassamalim.hidaya.other.Utils
import java.util.*
import java.util.regex.Pattern

class QuranSearcherActivity : AppCompatActivity() {

    private var binding: ActivityQuranSearcherBinding? = null
    private var pref: SharedPreferences? = null
    private var recyclerView: RecyclerView? = null
    private var searchView: SearchView? = null
    private var adapter: QuranSearcherAdapter? = null
    private var allAyat: List<AyatDB?>? = null
    private var matches: MutableList<Ayah>? = null
    private var names: List<String>? = null
    private var maxMatches = 0
    private var language: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.onActivityCreateSetTheme(this)
        language = Utils.onActivityCreateSetLocale(this)
        binding = ActivityQuranSearcherBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        binding!!.home.setOnClickListener { onBackPressed() }

        init()

        setListeners()

        setupSizeSpinner()

        initRecycler()
    }

    private fun init() {
        val db: AppDatabase = Room.databaseBuilder(
            this, AppDatabase::class.java, "HidayaDB")
            .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build()
        pref = PreferenceManager.getDefaultSharedPreferences(this)

        searchView = binding!!.searchView

        allAyat = db.ayahDao().getAll()

        names =
            if (language == "en") db.suarDao().getNamesEn()
            else db.suarDao().getNames()

        matches = ArrayList<Ayah>()

        maxMatches = pref!!.getInt("quran_searcher_matches_last_position", 1)
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
    }

    private fun setupSizeSpinner() {
        val spinner: Spinner = binding!!.sizeSpinner

        val last: Int = pref!!.getInt("quran_searcher_matches_last_position", 0)
        spinner.setSelection(last)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View, position: Int, vId: Long
            ) {
                maxMatches = spinner.getItemAtPosition(position).toString().toInt()

                val editor: SharedPreferences.Editor = pref!!.edit()
                editor.putInt("quran_searcher_matches_last_position", position)
                editor.apply()

                if (adapter != null && adapter!!.itemCount > 0)
                    perform(searchView!!.query.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun perform(query: String) {
        search(query)

        if (matches!!.isEmpty()) {
            binding!!.notFoundTv.visibility = View.VISIBLE
            binding!!.recycler.visibility = View.INVISIBLE
        }
        else {
            binding!!.notFoundTv.visibility = View.INVISIBLE
            binding!!.recycler.visibility = View.VISIBLE

            if (adapter != null) adapter!!.notifyDataSetChanged()
        }
    }

    private fun search(text: String) {
        matches!!.clear()

        for (i in allAyat!!.indices) {
            val a: AyatDB = allAyat!![i]!!

            val m = Pattern.compile(text).matcher(a.aya_text_emlaey!!)
            val ss = SpannableString(a.aya_text_emlaey)
            while (m.find()) {
                ss.setSpan(
                    ForegroundColorSpan(getColor(R.color.highlight_M)),
                    m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                matches!!.add(
                    Ayah(a.sura_no, names!![a.sura_no], a.page, a.aya_no, a.aya_tafseer!!, ss)
                )
            }

            if (matches!!.size == maxMatches) break
        }
    }

    private fun initRecycler() {
        recyclerView = binding!!.recycler
        val layoutManager = LinearLayoutManager(this)
        recyclerView!!.layoutManager = layoutManager
        adapter = QuranSearcherAdapter(this, matches!!)
        recyclerView!!.adapter = adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
        if (recyclerView != null) recyclerView!!.adapter = null
        adapter = null
    }
}