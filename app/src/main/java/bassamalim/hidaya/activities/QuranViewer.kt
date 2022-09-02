package bassamalim.hidaya.activities

import android.app.Activity
import android.content.*
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.adapters.ListQuranViewerAdapter
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.database.dbs.AyatDB
import bassamalim.hidaya.databinding.ActivityQuranViewerBinding
import bassamalim.hidaya.dialogs.InfoDialog
import bassamalim.hidaya.dialogs.QuranSettingsDialog
import bassamalim.hidaya.dialogs.TutorialDialog
import bassamalim.hidaya.models.Aya
import bassamalim.hidaya.models.Ayah
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.replacements.DoubleClickLMM
import bassamalim.hidaya.replacements.DoubleClickableSpan
import bassamalim.hidaya.replacements.SwipeActivity
import bassamalim.hidaya.services.AyahPlayerService
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.DBUtils
import bassamalim.hidaya.utils.LangUtils
import bassamalim.hidaya.utils.PrefUtils


class QuranViewer : SwipeActivity() {

    private lateinit var binding: ActivityQuranViewerBinding
    private lateinit var db: AppDatabase
    private lateinit var pref: SharedPreferences
    private lateinit var action: String
    private lateinit var flipper: ViewFlipper
    private lateinit var scrollViews: Array<ScrollView>
    private lateinit var lls: Array<LinearLayout>
    private lateinit var listVs: Array<ListView>
    private var adapter: ListQuranViewerAdapter? = null
    private lateinit var what: Any
    private var currentView = 0
    private var surahIndex = 0
    private var currentPage = 0
    private var currentSura = 0
    private lateinit var currentPageText: String
    private lateinit var currentSurah: String
    private var textSize = 0
    private val allAyahs: MutableList<Ayah> = ArrayList()
    private lateinit var names: List<String>
    private lateinit var arr: MutableList<Ayah>
    private lateinit var target: TextView
    private var scrolled = false
    private var selected: Ayah? = null
    private var lastTracked: Ayah? = null
    private var player: AyahPlayerService? = null
    private var serviceBound = false
    private lateinit var ayatDB: List<AyatDB?>
    private lateinit var theme: String
    private lateinit var language: String
    private lateinit var viewType: String
    private val handler: Handler = Handler(Looper.getMainLooper())
    private var lastRecordedPage = 0
    private var tc: MediaControllerCompat.TransportControls? = null
    private var uiListener: AyahPlayerService.Coordinator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeify()
        binding = ActivityQuranViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initiate()

        checkFirstTime()

        action = intent.action!!
        action(intent)

        if (viewType == "list") setupListVs()

        buildPage(currentPage)

        setupListeners()
    }

    private fun themeify() {
        pref = PreferenceManager.getDefaultSharedPreferences(this)
        language = ActivityUtils.onActivityCreateSetLocale(this)
        textSize = pref.getInt(getString(R.string.quran_text_size_key), 30)
        theme = PrefUtils.getTheme(this, pref)
        viewType =
            if (language == "en") "list"
            else pref.getString("quran_view_type", "page")!!

        when (theme) {
            "ThemeL" -> setTheme(R.style.QuranL)
            "ThemeM" -> setTheme(R.style.QuranM)
        }
    }

    private fun initiate() {
        flipper = binding.flipper
        scrollViews = arrayOf(binding.scrollview1, binding.scrollview2)
        lls = arrayOf(binding.linear1, binding.linear2)

        db = DBUtils.getDB(this)

        ayatDB = db.ayahDao().getAll()
        names =
            if (language == "en") db.suarDao().getNamesEn()
            else db.suarDao().getNames()

        what =
            if (theme == "ThemeL")
                ForegroundColorSpan(resources.getColor(R.color.track_L, getTheme()))
            else ForegroundColorSpan(resources.getColor(R.color.track_M, getTheme()))
    }

    private fun checkFirstTime() {
        val prefKey = "is_first_time_in_quran"
        if (pref.getBoolean(prefKey, true))
            TutorialDialog.newInstance(
                getString(R.string.quran_tips), prefKey
            ).show(supportFragmentManager, TutorialDialog.TAG)
    }

    private fun action(intent: Intent) {
        when (action) {
            "by_surah" -> {
                surahIndex = intent.getIntExtra("surah_id", 0)
                currentPage = getPage(surahIndex)
            }
            "by_page" -> currentPage = intent.getIntExtra("page", 0)
        }
    }

    private fun setupListVs() {
        listVs = arrayOf(binding.listview1, binding.listview2)

        adapter = ListQuranViewerAdapter(
            this, R.layout.item_listview_quran_viewer, allAyahs, theme, language
        )

        listVs.map { listView -> listView.adapter = adapter }

        flipper.displayedChild = 2
    }

    private fun getPage(surahIndex: Int): Int {
        return db.suarDao().getPage(surahIndex)
    }

    private fun setCurrentPage(num: Int) {
        currentPage = num
        player?.setCurrentPage(num)
    }

    private fun buildPage(pageNumber: Int) {
        handler.removeCallbacks(runnable)

        if (viewType == "page") lls[currentView].removeAllViews()
        allAyahs.clear()
        arr = ArrayList()
        val pageAyahs = ArrayList<List<Ayah>?>()

        var counter = getPageStart(pageNumber)
        do {
            val aya = ayatDB[counter]!!
            val suraNum = aya.sura_no // starts from 1
            val ayaNum = aya.aya_no

            val ayahModel = Ayah(
                aya.id, aya.jozz, suraNum, ayaNum, names[suraNum - 1],
                "${aya.aya_text} ", aya.aya_translation_en, aya.aya_tafseer
            )

            if (ayaNum == 1) {
                if (arr.size > 0) {
                    pageAyahs.add(arr)
                    if (viewType == "page") publishPage(arr)
                }
                if (viewType == "page") addHeader(suraNum, ayahModel.getSurahName())
            }

            arr.add(ayahModel)

            counter++
        } while (ayatDB[counter]!!.page == pageNumber && counter != Global.QURAN_AYAS )

        val juz = arr[0].getJuz()

        pageAyahs.add(arr)

        if (viewType == "list") {
            publishList(arr)
            adapter?.notifyDataSetChanged()
        }
        else publishPage(arr)

        currentSura = pageAyahs.indexOf(pageAyahs.maxBy { list -> list!!.size })

        finalize(juz, pageAyahs[currentSura]!![0].getSurahName())

        checkPage(currentPage)
    }

    private fun publishPage(list: List<Ayah>?) {
        val screen = screen()
        val text = StringBuilder()

        for (i in list!!.indices) {
            list[i].setStart(text.length)
            text.append(list[i].getText())
            list[i].setEnd(text.length)
        }

        val ss = SpannableString(text)
        for (i in list.indices) {
            val clickableSpan = object : DoubleClickableSpan() {
                override fun onDoubleClick(view: View?) {
                    InfoDialog.newInstance(getString(R.string.tafseer), list[i].getTafseer())
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

        player?.setAllAyahsSize(allAyahs.size)

        screen.text = ss

        getContainer().addView(screen)

        arr = ArrayList()
    }

    private fun publishList(list: List<Ayah>?) {
        for (i in list!!.indices) {
            val ss = SpannableString(list[i].getText())
            val clickableSpan = object : DoubleClickableSpan() {
                override fun onDoubleClick(view: View?) {
                    InfoDialog.newInstance(getString(R.string.tafseer), list[i].getTafseer())
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
            ss.setSpan(
                clickableSpan, 0, list[i].getText()!!.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            list[i].setIndex(allAyahs.size)
            allAyahs.add(list[i])
        }

        player?.setAllAyahsSize(allAyahs.size)

        arr = ArrayList()
    }

    private fun finalize(juz: Int, name: String) {
        val juzText = "${getString(R.string.juz)} ${LangUtils.translateNums(this, juz.toString())}"

        currentSurah = "${getString(R.string.sura)} $name"
        currentPageText =
            "${getString(R.string.page)} ${LangUtils.translateNums(this, currentPage.toString())}"
        binding.juzNumber.text = juzText
        binding.suraName.text = currentSurah
        binding.pageNumber.text = currentPageText

        if (action == "by_surah" && !scrolled) {
            if (viewType == "page") scrollTo(target.top)
            scrolled = true
        }
    }

    override fun previous() {
        if (currentPage > 1) {
            flipper.setInAnimation(this, R.anim.slide_in_right)
            flipper.setOutAnimation(this, R.anim.slide_out_left)
            currentView = (currentView + 1) % 2
            setCurrentPage(--currentPage)
            buildPage(currentPage)
            flip()
        }
    }

    override operator fun next() {
        if (currentPage < Global.QURAN_PAGES) {
            flipper.setInAnimation(this, R.anim.slide_in_left)
            flipper.setOutAnimation(this, R.anim.slide_out_right)
            currentView = (currentView + 1) % 2
            setCurrentPage(++currentPage)
            buildPage(currentPage)
            flip()
        }
    }

    private fun flip() {
        if (viewType == "list") {
            if (flipper.displayedChild == 2) flipper.displayedChild = 3
            else flipper.displayedChild = 2
            listVs[currentView].scrollTo(0, 0)
        }
        else {
            if (flipper.displayedChild == 0) flipper.displayedChild = 1
            else flipper.displayedChild = 0
            scrollViews[currentView].scrollTo(0, 0)
        }
    }

    private fun checkPage(pageNumber: Int) {
        lastRecordedPage = pageNumber
        handler.postDelayed(runnable, 40000)
    }

    private val runnable = Runnable {
        if (currentPage == lastRecordedPage) updateRecord()
    }

    private fun updateRecord() {
        val old = pref.getInt("quran_pages_record", 0)
        val new = old + 1

        val editor = pref.edit()
        editor.putInt("quran_pages_record", new)
        if (currentPage == pref.getInt("today_werd_page", 25)) editor.putBoolean("werd_done", true)
        editor.apply()
    }

    private fun getContainer(): ViewGroup {
        return if (viewType == "list") listVs[currentView] else lls[currentView]
    }

    private fun updateButton(state: Int) {
        when(state) {
            PlaybackStateCompat.STATE_PLAYING -> {
                binding.bufferingCircle.visibility = View.GONE
                binding.playPause.visibility = View.VISIBLE
                binding.playPause.setImageDrawable(
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_pause, getTheme())
                )
            }
            PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.STATE_STOPPED-> {
                binding.bufferingCircle.visibility = View.GONE
                binding.playPause.visibility = View.VISIBLE
                binding.playPause.setImageDrawable(
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_play_aya, getTheme())
                )
            }
            PlaybackStateCompat.STATE_BUFFERING -> {
                binding.playPause.visibility = View.GONE
                binding.bufferingCircle.visibility = View.VISIBLE
            }
        }
    }

    private fun setupListeners() {
        binding.bookmarkButton.setOnClickListener {
            val editor = pref.edit()
            editor.putInt("bookmarked_page", currentPage)
            editor.putInt("bookmarked_sura", surahIndex)
            editor.apply()

            Toast.makeText(this, getString(R.string.page_bookmarked), Toast.LENGTH_SHORT).show()
        }

        binding.playPause.setOnClickListener {
            if (player == null) {
                updateButton(PlaybackStateCompat.STATE_BUFFERING)
                setupPlayer()
            }
            else if (player!!.getState() == PlaybackStateCompat.STATE_PLAYING) {
                player!!.transportControls.pause()
                updateButton(PlaybackStateCompat.STATE_PAUSED)
            }
            else if (player!!.getState() == PlaybackStateCompat.STATE_PAUSED) {
                if (selected == null) player!!.transportControls.play()
                else {
                    player!!.setChosenSurah(selected!!.getSurahNum())
                    requestPlay(selected!!)
                }
                updateButton(PlaybackStateCompat.STATE_PLAYING)
            }
        }

        binding.prevAyah.setOnClickListener { player!!.transportControls.skipToPrevious() }
        binding.nextAyah.setOnClickListener { player!!.transportControls.skipToNext() }

        binding.recitationSettings.setOnClickListener {
            settingsDialog.launch(Intent(this, QuranSettingsDialog::class.java))
        }
    }

    private fun getPageStart(pageNumber: Int): Int {
        var counter = 0
        while (ayatDB[counter]!!.page < pageNumber) counter++
        return counter
    }

    private val settingsDialog = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewType = pref.getString("quran_view_type", "page")!!
                textSize = pref.getInt(getString(R.string.quran_text_size_key), 30)

                flipper.inAnimation = null
                flipper.outAnimation = null
                if (viewType == "list") {
                    setupListVs()

                    adapter!!.setTextSize(textSize)
                    listVs[0].adapter = null
                    listVs[1].adapter = null
                    listVs[0].adapter = adapter
                    listVs[1].adapter = adapter
                }
                else {
                    currentView = 0
                    flipper.displayedChild = 0
                }

                buildPage(currentPage)
                player?.setViewType(viewType)
            }
        }

    private fun setupPlayer() {
        uiListener = object : AyahPlayerService.Coordinator {
            override fun onUiUpdate(state: Int) {
                updateButton(state)
            }

            override fun getAya(index: Int): Aya {
                val ayah = allAyahs[index]
                return Aya(ayah.getId(), ayah.getSurahNum(), ayah.getAyahNum(), ayah.getIndex())
            }

            override fun nextPage() {
                next()
            }

            override fun track(ayaId: Int, ayaIndex: Int) {
                val ayah = allAyahs[ayaIndex]

                if (ayah.getId() != ayaId) return

                scrollTo(ayah.getScreen()!!.top)

                if (lastTracked != null) {
                    lastTracked!!.getSS()!!.removeSpan(what)
                    lastTracked!!.getScreen()!!.text = lastTracked!!.getSS()
                }

                if (viewType == "list")
                    ayah.getSS()!!.setSpan(what, 0, ayah.getText()!!.length - 1,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                else
                    ayah.getSS()!!.setSpan(what, ayah.getStart(), ayah.getEnd(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                ayah.getScreen()!!.text = ayah.getSS()  // heavy, but the only working way
                lastTracked = ayah
            }
        }

        val playerIntent = Intent(this, AyahPlayerService::class.java)
        startService(playerIntent)
        bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    //Binding this Client to the AudioPlayer Service
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.i(Global.TAG, "In onServiceConnected")
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder: AyahPlayerService.LocalBinder = service as AyahPlayerService.LocalBinder
            player = binder.service
            tc = player!!.transportControls
            serviceBound = true

            if (selected == null) selected = allAyahs[0]
            
            player!!.setCurrentPage(currentPage)
            player!!.setViewType(viewType)
            player!!.setAllAyahsSize(allAyahs.size)
            player!!.setCoordinator(uiListener!!)

            player!!.setChosenSurah(selected!!.getSurahNum())
            requestPlay(selected!!)

            selected = null
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serviceBound = false
        }
    }

    private fun requestPlay(ayah: Ayah) {
        val bundle = Bundle()
        val aya = Aya(ayah.getId(), ayah.getSurahNum(), ayah.getAyahNum(), ayah.getIndex())
        bundle.putSerializable("aya", aya)
        tc!!.playFromMediaId(ayah.getAyahNum().toString(), bundle)
    }

    private fun scrollTo(position: Int) {
        val delay = 100L //delay to let finish with possible modifications to View
        if (viewType == "list") {
            listVs[currentView].smoothScrollToPosition(position)
        }
        else scrollViews[currentView].postDelayed(
            { scrollViews[currentView].smoothScrollTo(0, position) }, delay
        )
    }

    private fun addHeader(suraNum: Int, name: String) {
        val nameScreen = surahName(name)

        getContainer().addView(nameScreen)
        if (suraNum != 1 && suraNum != 9) // surat al-fatiha and At-Taubah
            getContainer().addView(basmalah())

        if (action == "by_surah" && suraNum == surahIndex + 1) target = nameScreen
    }

    private fun surahName(name: String): TextView {
        val nameTv = TextView(this)
        val screenParams = LinearLayout.LayoutParams(
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
        }
        else {
            nameScreen.setTextColor(Color.WHITE)
            nameScreen.setLinkTextColor(Color.WHITE)
        }
        return nameScreen
    }

    private fun screen(): TextView {
        val tv: TextView = layoutInflater.inflate(R.layout.tv_quran_viewer, null) as TextView
        tv.textSize = textSize.toFloat()
        if (theme == "ThemeL")
            tv.movementMethod = DoubleClickLMM.getInstance(
                resources.getColor(R.color.highlight_L, getTheme())
            )
        else
            tv.movementMethod = DoubleClickLMM.getInstance(
                resources.getColor(R.color.highlight_M, getTheme())
            )
        return tv
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.finish()
        if (serviceBound) {
            unbindService(serviceConnection)
            player?.stopSelf()
        }
        player?.onDestroy()
        handler.removeCallbacks(runnable)
    }

}