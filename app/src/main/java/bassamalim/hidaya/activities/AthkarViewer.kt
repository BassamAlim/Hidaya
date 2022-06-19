package bassamalim.hidaya.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import bassamalim.hidaya.R
import bassamalim.hidaya.adapters.AthkarViewerAdapter
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.database.dbs.ThikrsDB
import bassamalim.hidaya.databinding.ActivityAthkarViewerBinding
import bassamalim.hidaya.dialogs.InfoDialog
import bassamalim.hidaya.models.Thikr
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.other.Utils

class AthkarViewer : AppCompatActivity() {

    private var binding: ActivityAthkarViewerBinding? = null
    private var pref: SharedPreferences? = null
    private var db: AppDatabase? = null
    private var recycler: RecyclerView? = null
    private var textSizeSb: SeekBar? = null
    private var adapter: AthkarViewerAdapter? = null
    private var language: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.onActivityCreateSetTheme(this)
        language = Utils.onActivityCreateSetLocale(this)
        binding = ActivityAthkarViewerBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        binding!!.home.setOnClickListener { onBackPressed() }
        pref = PreferenceManager.getDefaultSharedPreferences(this)
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "HidayaDB")
            .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build()
        val intent: Intent = intent
        val id: Int = intent.getIntExtra("thikr_id", 0)
        val topBarTitle: String =
            if (language == "en") db!!.athkarDao().getNameEn(id) else db!!.athkarDao().getName(id)
        binding!!.topBarTitle.text = topBarTitle
        setupRecycler(id)
        setupListeners()
    }

    private fun getThikrs(id: Int): List<ThikrsDB> {
        return db!!.thikrsDao().getThikrs(id)
    }

    private fun makeCards(thikrs: List<ThikrsDB>): ArrayList<Thikr> {
        val cards: ArrayList<Thikr> = ArrayList()
        for (i in thikrs.indices) {
            val t: ThikrsDB = thikrs[i]
            if (language == "en" && (t.getText_en().isEmpty())) continue
            if (language == "en") cards.add(
                Thikr(t.getThikr_id(), t.getTitle_en(), t.getText_en(),
                    t.getText_en_translation(), t.getFadl_en(), t.getReference_en(),
                    t.getRepetition_en()
                ) {
                    InfoDialog(getString(R.string.reference), t.getReference_en())
                        .show(supportFragmentManager, InfoDialog.TAG)
                }
            ) else cards.add(
                Thikr(t.getThikr_id(), t.getTitle(), t.getText(),
                    t.getText_en_translation(), t.getFadl(), t.getReference(),
                    t.getRepetition()
                ) {
                    InfoDialog(
                        getString(R.string.reference),
                        t.getReference()
                    ).show(supportFragmentManager, InfoDialog.TAG)
                }
            )
        }
        return cards
    }

    private fun setupRecycler(id: Int) {
        recycler = binding!!.recycler
        val layoutManager = LinearLayoutManager(this)
        recycler!!.layoutManager = layoutManager
        adapter = AthkarViewerAdapter(this, makeCards(getThikrs(id)), language!!)
        recycler!!.adapter = adapter
    }

    private fun setupListeners() {
        textSizeSb = binding!!.textSizeSb
        textSizeSb!!.progress = pref!!.getInt(getString(R.string.alathkar_text_size_key), 15)
        binding!!.textSizeIb.setOnClickListener {
            if (textSizeSb!!.visibility == View.GONE) textSizeSb!!.visibility = View.VISIBLE
            else textSizeSb!!.visibility = View.GONE
        }
        textSizeSb!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                Log.d(Global.TAG, seekBar.progress.toString())
                val editor: SharedPreferences.Editor = pref!!.edit()
                editor.putInt(getString(R.string.alathkar_text_size_key), seekBar.progress)
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