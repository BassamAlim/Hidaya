package bassamalim.hidaya.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bassamalim.hidaya.R
import bassamalim.hidaya.adapters.AthkarViewerAdapter
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.database.dbs.ThikrsDB
import bassamalim.hidaya.databinding.ActivityAthkarViewerBinding
import bassamalim.hidaya.dialogs.InfoDialog
import bassamalim.hidaya.models.Thikr
import bassamalim.hidaya.other.Utils

class AthkarViewer : AppCompatActivity() {

    private lateinit var binding: ActivityAthkarViewerBinding
    private lateinit var pref: SharedPreferences
    private lateinit var db: AppDatabase
    private lateinit var recycler: RecyclerView
    private lateinit var textSizeSb: SeekBar
    private lateinit var adapter: AthkarViewerAdapter
    private lateinit var language: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.onActivityCreateSetTheme(this)
        language = Utils.onActivityCreateSetLocale(this)
        binding = ActivityAthkarViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.home.setOnClickListener { onBackPressed() }

        pref = PreferenceManager.getDefaultSharedPreferences(this)

        db = Utils.getDB(this)

        val intent: Intent = intent
        val id: Int = intent.getIntExtra("thikr_id", 0)

        binding.topBarTitle.text =
            if (language == "en") db.athkarDao().getNameEn(id)
            else db.athkarDao().getName(id)

        setupRecycler(id)

        setupListeners()
    }

    private fun getThikrs(id: Int): List<ThikrsDB> {
        return db.thikrsDao().getThikrs(id)
    }

    private fun getItems(thikrs: List<ThikrsDB>): ArrayList<Thikr> {
        val items: ArrayList<Thikr> = ArrayList()
        for (i in thikrs.indices) {
            val t: ThikrsDB = thikrs[i]

            if (language == "en" && (t.getTextEn()!!.isEmpty())) continue

            if (language == "en")
                items.add(
                    Thikr(
                        t.getThikrId(), t.getTitleEn(), t.getTextEn()!!, t.getTextEnTranslation(),
                        t.getFadlEn(), t.getReferenceEn(), t.getRepetitionEn()
                    ) {
                        InfoDialog.newInstance(getString(R.string.reference), t.getReferenceEn()!!)
                            .show(supportFragmentManager, InfoDialog.TAG)
                    }
                )
            else
                items.add(
                    Thikr(t.getThikrId(), t.getTitle(), t.getText()!!,
                        t.getTextEnTranslation(), t.getFadl(), t.getReference(),
                        t.getRepetition()
                    ) {
                        InfoDialog.newInstance(getString(R.string.reference), t.getReference()!!)
                            .show(supportFragmentManager, InfoDialog.TAG)
                    }
                )
        }
        return items
    }

    private fun setupRecycler(id: Int) {
        recycler = binding.recycler
        val layoutManager = LinearLayoutManager(this)
        recycler.layoutManager = layoutManager
        adapter = AthkarViewerAdapter(this, getItems(getThikrs(id)), language)
        recycler.adapter = adapter
    }

    private fun setupListeners() {
        textSizeSb = binding.textSizeSb

        textSizeSb.progress = pref.getInt(getString(R.string.alathkar_text_size_key), 15)

        binding.textSizeIb.setOnClickListener {
            if (textSizeSb.visibility == View.GONE) textSizeSb.visibility = View.VISIBLE
            else textSizeSb.visibility = View.GONE
        }

        textSizeSb.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val editor: SharedPreferences.Editor = pref.edit()

                editor.putInt(getString(R.string.alathkar_text_size_key), seekBar.progress)
                editor.apply()

                adapter.setTextSize(seekBar.progress)
                recycler.adapter = null
                recycler.adapter = adapter
            }
        })
    }

}