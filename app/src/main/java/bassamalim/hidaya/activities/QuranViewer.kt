package bassamalim.hidaya.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import bassamalim.hidaya.R
import bassamalim.hidaya.adapters.RecyclerQuranViewerAdapter
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.database.dbs.AyatDB
import bassamalim.hidaya.databinding.ActivityQuranViewerBinding
import bassamalim.hidaya.dialogs.InfoDialog
import bassamalim.hidaya.dialogs.QuranSettingsDialog
import bassamalim.hidaya.dialogs.TutorialDialog
import bassamalim.hidaya.enums.States
import bassamalim.hidaya.helpers.AyahPlayer
import bassamalim.hidaya.helpers.AyahPlayer.Coordinator
import bassamalim.hidaya.models.Ayah
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.other.Utils
import bassamalim.hidaya.replacements.DoubleClickLMM
import bassamalim.hidaya.replacements.DoubleClickableSpan
import bassamalim.hidaya.replacements.SwipeActivity
import java.util.*

class QuranViewer : SwipeActivity() {

    private var binding: ActivityQuranViewerBinding? = null
    private var db: AppDatabase? = null
    private var pref: SharedPreferences? = null
    private var action: String? = null
    private var flipper: ViewFlipper? = null
    private var scrollViews: Array<ScrollView>? = null
    private var lls: Array<LinearLayout>? = null
    private var recyclers: Array<RecyclerView>? = null
    private var adapter: RecyclerQuranViewerAdapter? = null
    private var currentView = 0
    private var surahIndex = 0
    private var currentPage = 0
    private var currentPageText: String? = null
    private var currentSurah: String? = null
    private var textSize = 0
    private val allAyahs: MutableList<Ayah> = ArrayList<Ayah>()
    private var names: List<String>? = null
    private var arr: MutableList<Ayah>? = null
    private var target: TextView? = null
    private var scrolled = false
    private var selected: Ayah? = null
    private var ayahPlayer: AyahPlayer? = null
    private var ayatDB: List<AyatDB?>? = null
    private var theme: String? = null
    private var language: String? = null
    private var viewType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeify()
        binding = ActivityQuranViewerBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        initiate()

        checkFirstTime()

        action = intent.action
        action(intent)

        if (viewType == "list") setupRecyclers()

        buildPage(currentPage)

        setListeners()

        setupPlayer()
    }

    private fun themeify() {
        pref = PreferenceManager.getDefaultSharedPreferences(this)
        language = Utils.onActivityCreateSetLocale(this)
        textSize = pref!!.getInt(getString(R.string.quran_text_size_key), 30)
        theme = pref!!.getString(getString(R.string.theme_key), getString(R.string.default_theme))

        viewType = if (language == "en") "list" else pref!!.getString("quran_view_type", "page")
        when (theme) {
            "ThemeL" -> setTheme(R.style.QuranL)
            "ThemeM" -> setTheme(R.style.QuranM)
        }
    }

    private fun initiate() {
        flipper = binding!!.flipper
        scrollViews = arrayOf(binding!!.scrollview1, binding!!.scrollview2)
        lls = arrayOf(binding!!.linear1, binding!!.linear2)

        db = Room.databaseBuilder(this, AppDatabase::class.java, "HidayaDB")
            .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build()

        ayatDB = db!!.ayahDao().getAll()
        names =
            if (language == "en") db!!.suarDao().getNamesEn()
            else db!!.suarDao().getNames()
    }

    private fun checkFirstTime() {
        if (pref!!.getBoolean("is_first_time_in_quran", true)) TutorialDialog(
            this, getString(R.string.quran_tips),
            "is_first_time_in_quran"
        ).show(supportFragmentManager, TutorialDialog.TAG)
    }

    private fun action(intent: Intent) {
        when (action) {
            "by_surah" -> {
                surahIndex = intent.getIntExtra("surah_id", 0)
                setCurrentPage(getPage(surahIndex))
            }
            "by_page" -> setCurrentPage(intent.getIntExtra("page", 0))
            "random" -> setCurrentPage(Random().nextInt(Global.QURAN_PAGES - 1))
        }
    }

    private fun setupRecyclers() {
        recyclers = arrayOf(binding!!.recycler1, binding!!.recycler2)

        val layoutManagers =
            arrayOf(LinearLayoutManager(this), LinearLayoutManager(this))
        recyclers!![0].layoutManager = layoutManagers[0]
        recyclers!![1].layoutManager = layoutManagers[1]

        adapter =
            if (action == "by_surah")
                RecyclerQuranViewerAdapter(this, allAyahs, theme!!, language!!, surahIndex)
            else
                RecyclerQuranViewerAdapter(this, allAyahs, theme!!, language!!, -1)

        recyclers!![0].adapter = adapter
        recyclers!![1].adapter = adapter

        flipper!!.displayedChild = 2
    }

    private fun getPage(surahIndex: Int): Int {
        return db!!.suarDao().getPage(surahIndex)
    }

    private fun setCurrentPage(num: Int) {
        currentPage = num
        if (ayahPlayer != null) ayahPlayer!!.setCurrentPage(num)
    }

    private fun buildPage(pageNumber: Int) {
        if (viewType == "page") lls!![currentView].removeAllViews()
        allAyahs.clear()
        arr = ArrayList<Ayah>()
        val pageAyahs: MutableList<List<Ayah>?> = ArrayList<List<Ayah>?>()

        var counter = getPageStart(pageNumber)
        do {
            val aya: AyatDB = ayatDB!![counter]!!
            val suraNum: Int = aya.sura_no // starts from 1
            val ayaNum: Int = aya.aya_no

            val ayahModel = Ayah(aya.jozz, suraNum, ayaNum, names!![suraNum - 1],
                aya.aya_text + " ", aya.aya_translation_en, aya.aya_tafseer!!)

            if (ayaNum == 1) {
                if (arr!!.size > 0) {
                    pageAyahs.add(arr)
                    if (viewType == "page") publishPage(arr)
                }
                if (viewType == "page") addHeader(suraNum, ayahModel.getSurahName())
            }

            arr!!.add(ayahModel)
        } while (++counter != Global.QURAN_AYAS && ayatDB!![counter]!!.page == pageNumber)

        val juz: Int = arr!![0].getJuz()

        pageAyahs.add(arr)

        if (viewType == "list") {
            publishList(arr)
            if (adapter != null) adapter!!.notifyDataSetChanged()
        }
        else publishPage(arr)

        finalize(juz, pageAyahs[findMainSurah(pageAyahs)]!![0].getSurahName())
    }

    private fun publishPage(list: List<Ayah>?) {
        val screen: TextView = screen()
        val text = StringBuilder()

        for (i in list!!.indices) {
            list[i].setStart(text.length)
            text.append(list[i].getText())
            list[i].setEnd(text.length - 3)
        }

        val ss = SpannableString(text)
        for (i in list.indices) {
            val clickableSpan: DoubleClickableSpan = object : DoubleClickableSpan() {
                override fun onDoubleClick(view: View?) {
                    InfoDialog(getString(R.string.tafseer), list[i].getTafseer())
                        .show(supportFragmentManager, InfoDialog.TAG)
                }

                override fun onClick(widget: View) {
                    selected = allAyahs[list[i].getIndex()]
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                }
            }
            list[i].setSS(ss)
            ss.setSpan(clickableSpan, list[i].getStart(), list[i].getEnd(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            list[i].setIndex(allAyahs.size)
            list[i].setScreen(screen)
            allAyahs.add(list[i])
        }

        if (ayahPlayer != null) ayahPlayer!!.setAllAyahsSize(allAyahs.size)

        screen.text = ss

        getContainer().addView(screen)

        arr = ArrayList<Ayah>()
    }

    private fun publishList(list: List<Ayah>?) {
        for (i in list!!.indices) {
            val ss = SpannableString(list[i].getText())
            val clickableSpan: DoubleClickableSpan = object : DoubleClickableSpan() {
                override fun onDoubleClick(view: View?) {
                    InfoDialog(getString(R.string.tafseer), list[i].getTafseer())
                        .show(supportFragmentManager, InfoDialog.TAG)
                }

                override fun onClick(widget: View) {
                    selected = allAyahs[list[i].getIndex()]
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                }
            }
            list[i].setSS(ss)
            ss.setSpan(clickableSpan, 0, list[i].getText()!!.length - 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            list[i].setIndex(allAyahs.size)
            allAyahs.add(list[i])
        }

        if (ayahPlayer != null) ayahPlayer!!.setAllAyahsSize(allAyahs.size)

        arr = ArrayList<Ayah>()
    }

    private fun finalize(juz: Int, name: String) {
        val juzText: String = getString(R.string.juz) + " " +
                Utils.translateNumbers(this, juz.toString())
        currentSurah = getString(R.string.sura) + " " + name
        currentPageText = getString(R.string.page) + " " +
                Utils.translateNumbers(this, currentPage.toString())
        binding!!.juzNumber.text = juzText
        binding!!.suraName.text = currentSurah
        binding!!.pageNumber.text = currentPageText

        if (action == "by_surah" && !scrolled) {
            val delay: Long = 100 //delay to let finish with possible modifications to ScrollView
            if (viewType == "list") recyclers!![currentView].smoothScrollToPosition(0)
            else scrollViews!![currentView].postDelayed(
                { scrollViews!![currentView].smoothScrollTo(0, target!!.top) }, delay
            )
            scrolled = true
        }
    }

    override fun previous() {
        if (currentPage > 1) {
            flipper!!.setInAnimation(this, R.anim.slide_in_right)
            flipper!!.setOutAnimation(this, R.anim.slide_out_left)
            currentView = (currentView + 1) % 2
            setCurrentPage(--currentPage)
            buildPage(currentPage)
            flip()
        }
    }

    override operator fun next() {
        if (currentPage < Global.QURAN_PAGES) {
            flipper!!.setInAnimation(this, R.anim.slide_in_left)
            flipper!!.setOutAnimation(this, R.anim.slide_out_right)
            currentView = (currentView + 1) % 2
            setCurrentPage(++currentPage)
            buildPage(currentPage)
            flip()
        }
    }

    private fun flip() {
        if (viewType == "list") {
            if (flipper!!.displayedChild == 2) flipper!!.displayedChild = 3
            else flipper!!.displayedChild = 2
            recyclers!![currentView].scrollTo(0, 0)
        }
        else {
            if (flipper!!.displayedChild == 0) flipper!!.displayedChild = 1
            else flipper!!.displayedChild = 0
            scrollViews!![currentView].scrollTo(0, 0)
        }
    }

    private fun getContainer(): ViewGroup {
        return if (viewType == "list") recyclers!![currentView] else lls!![currentView]
    }

    private fun updateUi(state: States) {
        when (state) {
            States.Playing -> binding!!.play.setImageDrawable(
                ResourcesCompat.getDrawable(resources, R.drawable.ic_pause, getTheme())
            )
            States.Paused -> binding!!.play.setImageDrawable(
                ResourcesCompat.getDrawable(resources, R.drawable.ic_play_aya, getTheme())
            )
            States.Stopped -> binding!!.play.setImageDrawable(
                ResourcesCompat.getDrawable(resources, R.drawable.ic_play_aya, getTheme())
            )
        }
    }

    private fun setListeners() {
        binding!!.bookmarkButton.setOnClickListener {
            val editor: SharedPreferences.Editor = pref!!.edit()
            editor.putInt("bookmarked_page", currentPage)
            editor.putString("bookmarked_text", "$currentPageText, $currentSurah")
            editor.apply()

            Toast.makeText(this, getString(R.string.page_bookmarked), Toast.LENGTH_SHORT).show()
        }

        binding!!.play.setOnClickListener {
            if (ayahPlayer!!.getState() == States.Playing) {
                if (ayahPlayer!!.getLastPlayed() != null) selected = ayahPlayer!!.getLastPlayed()

                ayahPlayer!!.pause()
                updateUi(States.Paused)
            }
            else if (ayahPlayer!!.getState() == States.Paused) {
                if (selected == null) ayahPlayer!!.resume()
                else {
                    ayahPlayer!!.setChosenSurah(selected!!.getSurahNum())
                    ayahPlayer!!.requestPlay(selected)
                }
                updateUi(States.Playing)
            }
            else {
                if (selected == null) selected = allAyahs[0]

                ayahPlayer!!.setChosenSurah(selected!!.getSurahNum())
                ayahPlayer!!.requestPlay(selected)

                updateUi(States.Playing)
            }
            selected = null
        }
        binding!!.prevAyah.setOnClickListener {ayahPlayer!!.prevAyah()}
        binding!!.nextAyah.setOnClickListener {ayahPlayer!!.nextAyah()}

        binding!!.recitationSettings.setOnClickListener {
            val intent = Intent(this, QuranSettingsDialog::class.java)
            settingsDialog.launch(intent)
        }
    }

    private fun getPageStart(pageNumber: Int): Int {
        val start: Int
        var counter = 0
        while (ayatDB!![counter]!!.page < pageNumber) counter++
        start = counter
        return start
    }

    private val settingsDialog: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent = result.data!!
                viewType = data.getStringExtra("view_type")
                textSize = data.getIntExtra("text_size", 30)

                flipper!!.inAnimation = null
                flipper!!.outAnimation = null
                if (viewType == "list") {
                    setupRecyclers()

                    adapter!!.setTextSize(textSize)
                    recyclers!![0].adapter = null
                    recyclers!![1].adapter = null
                    recyclers!![0].adapter = adapter
                    recyclers!![1].adapter = adapter
                }
                else flipper!!.displayedChild = 0

                buildPage(currentPage)
                if (ayahPlayer != null) ayahPlayer!!.setViewType(viewType)
            }
        }

    private fun setupPlayer() {
        ayahPlayer = AyahPlayer(this)
        ayahPlayer!!.setCurrentPage(currentPage)
        ayahPlayer!!.setViewType(viewType)

        val uiListener: Coordinator = object : Coordinator {
            override fun onUiUpdate(state: States) {
                updateUi(state)
            }

            override fun getAyah(index: Int): Ayah {
                return allAyahs[index]
            }

            override fun nextPage() {
                next()
            }
        }
        ayahPlayer!!.setCoordinator(uiListener)

        ayahPlayer!!.setAllAyahsSize(allAyahs.size)
    }

    private fun findMainSurah(surahs: List<List<Ayah>?>): Int {
        var largest = 0
        for (i in 1 until surahs.size) {
            if (surahs[i]!!.size > surahs[largest]!!.size) largest = i
        }
        return largest
    }

    private fun addHeader(suraNum: Int, name: String) {
        val nameScreen: TextView = surahName(name)

        getContainer().addView(nameScreen)
        if (suraNum != 1 && suraNum != 9) // surat al-fatiha and At-Taubah
            getContainer().addView(basmalah())

        if (action == "by_surah" && suraNum == surahIndex + 1) target = nameScreen
    }

    private fun surahName(name: String): TextView {
        val nameTv = TextView(this)
        val screenParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 160
        )
        screenParams.bottomMargin = 20
        nameTv.layoutParams = screenParams
        nameTv.setPadding(0, 10, 0, 10)
        nameTv.gravity = Gravity.CENTER
        nameTv.setBackgroundColor(Color.TRANSPARENT)
        nameTv.textSize = (textSize + 5).toFloat()
        if (theme == "ThemeL") nameTv.setBackgroundResource(R.drawable.surah_header_light)
        else nameTv.setBackgroundResource(R.drawable.surah_header)
        nameTv.typeface = Typeface.DEFAULT_BOLD
        nameTv.text = name
        return nameTv
    }

    private fun basmalah(): TextView {
        val nameScreen = TextView(this)
        val screenParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        nameScreen.layoutParams = screenParams
        nameScreen.setPadding(0, 0, 0, 10)
        nameScreen.gravity = Gravity.CENTER
        nameScreen.textSize = textSize.toFloat()
        nameScreen.typeface = Typeface.DEFAULT_BOLD
        nameScreen.text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"
        if (theme == "ThemeL") {
            nameScreen.setTextColor(Color.BLACK)
            nameScreen.setLinkTextColor(Color.BLACK)
        } else {
            nameScreen.setTextColor(Color.WHITE)
            nameScreen.setLinkTextColor(Color.WHITE)
        }
        return nameScreen
    }

    private fun screen(): TextView {
        val tv: TextView = layoutInflater.inflate(R.layout.tv_quran_viewer, null) as TextView
        tv.textSize = textSize.toFloat()
        if (theme == "ThemeL") tv.movementMethod = DoubleClickLMM.getInstance(
            resources.getColor(R.color.highlight_L, getTheme())
        ) else tv.movementMethod = DoubleClickLMM.getInstance(
            resources.getColor(R.color.highlight_M, getTheme())
        )
        return tv
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
        ayahPlayer!!.finish()
        ayahPlayer = null
    }
}