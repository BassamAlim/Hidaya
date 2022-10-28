package bassamalim.hidaya.activities

import android.app.Activity
import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.database.dbs.AyatDB
import bassamalim.hidaya.dialogs.InfoDialog
import bassamalim.hidaya.dialogs.QuranSettingsDialog
import bassamalim.hidaya.models.Aya
import bassamalim.hidaya.models.Ayah
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.services.AyahPlayerService
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.ui.theme.uthmanic
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.DBUtils
import bassamalim.hidaya.utils.LangUtils
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class QuranViewer : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var pref: SharedPreferences
    private lateinit var action: String
    private lateinit var ayatDB: List<AyatDB?>
    private lateinit var names: List<String>
    private var textSize = 0
    private val handler = Handler(Looper.getMainLooper())
    private val pageAyas = ArrayList<Ayah>()
    private var lastRecordedPage = 0
    private var initialPage = 0
    private var initialSura = -1
    private var currentSura = 0
    private var currentJuz = 0
    private var lastClickT = 0L
    private var lastClickedId = -1
    private var scrollTo = -1F

    private val viewType = mutableStateOf("page")
    @OptIn(ExperimentalPagerApi::class)
    private lateinit var pagerState: PagerState
    private val currentPage = mutableStateOf(0)
    private val bookmarkedPage = mutableStateOf(-1)
    private val selected = mutableStateOf<Ayah?>(null)
    private var tracked = mutableStateOf<Ayah?>(null)

    private val playerState = mutableStateOf(PlaybackStateCompat.STATE_STOPPED)
    private var player: AyahPlayerService? = null
    private var serviceBound = false
    private var tc: MediaControllerCompat.TransportControls? = null
    private var uiListener: AyahPlayerService.Coordinator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityUtils.myOnActivityCreated(this)

        init()

        ActivityUtils.checkFirstTime(
            this, supportFragmentManager,
            "is_first_time_in_quran", R.string.quran_tips, pref
        )

        action = intent.action!!
        action(intent)

        setContent {
            AppTheme {
                UI()
            }
        }
    }

    private fun init() {
        pref = PreferenceManager.getDefaultSharedPreferences(this)
        db = DBUtils.getDB(this)

        textSize = pref.getInt(getString(R.string.quran_text_size_key), 30)

        val language = ActivityUtils.onActivityCreateSetLocale(this)
        if (language == "en") viewType.value = "list"

        ayatDB = db.ayahDao().getAll()
        names =
            if (language == "en") db.suarDao().getNamesEn()
            else db.suarDao().getNames()

        bookmarkedPage.value = pref.getInt("bookmarked_page", -1)
    }

    private fun action(intent: Intent) {
        when (action) {
            "by_surah" -> {
                initialSura = intent.getIntExtra("surah_id", 0)
                currentSura = initialSura
                initialPage = db.suarDao().getPage(currentSura)
            }
            "by_page" -> initialPage = intent.getIntExtra("page", 0)
        }
    }

    private fun buildPage(pageNumber: Int) {
        handler.removeCallbacks(runnable)
        pageAyas.clear()

        var counter = getPageStart(pageNumber)
        do {
            val aya = ayatDB[counter]!!
            val suraNum = aya.sura_num // starts from 1
            val ayaNum = aya.aya_num

            val ayahModel = Ayah(
                aya.id, aya.jozz, suraNum, ayaNum, names[suraNum - 1],
                "${aya.aya_text} ", aya.aya_translation_en, aya.aya_tafseer
            )

            pageAyas.add(ayahModel)

            counter++
        } while (counter != Global.QURAN_AYAS && ayatDB[counter]!!.page == pageNumber)

        finalize(pageAyas[0])

        checkPage(currentPage.value)

        currentPage.value = pageNumber
    }

    private fun finalize(aya: Ayah) {
        currentSura = aya.getSurahNum()
        currentJuz = aya.getJuz()
    }

    private fun onAyaClick(ayaId: Int, offset: Int) {
        val startIdx = pageAyas.indexOfFirst { it.getId() == ayaId }

        val maxDuration = 1200
        for (idx in startIdx until pageAyas.size) {
            val aya = pageAyas[idx]

            if (offset < aya.getEnd()) {
                // double click
                if (aya.getId() == lastClickedId &&
                    System.currentTimeMillis() < lastClickT + maxDuration) {
                    selected.value = null

                    InfoDialog.newInstance(
                        getString(R.string.tafseer), aya.getTafseer()
                    ).show(supportFragmentManager, InfoDialog.TAG)
                }
                else {  // single click
                    if (selected.value == aya) selected.value = null
                    else selected.value = aya
                }

                lastClickedId = aya.getId()
                lastClickT = System.currentTimeMillis()
                break
            }
        }
    }

    private fun checkPage(pageNumber: Int) {
        lastRecordedPage = pageNumber
        handler.postDelayed(runnable, 40000)
    }

    private val runnable = Runnable {
        if (currentPage.value == lastRecordedPage) updateRecord()
    }

    private fun updateRecord() {
        val old = pref.getInt("quran_pages_record", 0)
        val new = old + 1

        val editor = pref.edit()
        editor.putInt("quran_pages_record", new)
        if (currentPage.value == pref.getInt("today_werd_page", 25))
            editor.putBoolean("werd_done", true)
        editor.apply()
    }

    private fun setupPlayer() {
        uiListener = object : AyahPlayerService.Coordinator {
            override fun onUiUpdate(state: Int) {
                updateButton(state)
            }

            override fun getAya(index: Int): Aya {
                val ayah = pageAyas[index]
                return Aya(ayah.getId(), ayah.getSurahNum(), ayah.getAyahNum(), ayah.getIndex())
            }

            override fun nextPage() {

            }

            override fun track(ayaId: Int, ayaIndex: Int) {
                val aya = pageAyas[ayaIndex]

                if (aya.getId() != ayaId) return  // not the same page

                //scrollTo(aya.getScreen()!!.top)

                tracked.value = aya
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
            val binder = service as AyahPlayerService.LocalBinder
            player = binder.service
            tc = player!!.transportControls
            serviceBound = true

            if (selected.value == null) selected.value = pageAyas[0]

            player!!.setCurrentPage(currentPage.value)
            player!!.setViewType(viewType.value)
            player!!.setPageAyasSize(pageAyas.size)
            player!!.setCoordinator(uiListener!!)

            player!!.setChosenSurah(selected.value!!.getSurahNum())
            requestPlay(selected.value!!)

            selected.value = null
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

    private fun updateButton(state: Int) {
        when (state) {
            PlaybackStateCompat.STATE_NONE,
            PlaybackStateCompat.STATE_STOPPED,
            PlaybackStateCompat.STATE_PLAYING,
            PlaybackStateCompat.STATE_BUFFERING -> playerState.value = state
            else -> {}
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
            viewType.value = pref.getString("quran_view_type", "page")!!
            textSize = pref.getInt(getString(R.string.quran_text_size_key), 30)
        }
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

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    private fun UI() {
        MyScaffold(
            title = "",
            topBar = {
                TopAppBar(
                    backgroundColor = AppTheme.colors.primary,
                    elevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MyText(
                            text = "${getString(R.string.sura)} ${names[currentSura]}",
                            fontSize = 18.sp
                        )

                        MyText(
                            text = "${getString(R.string.page)} " +
                                    LangUtils.translateNums(
                                        this@QuranViewer, currentPage.value.toString()
                                    ),
                            fontSize = 18.sp
                        )

                        MyText(
                            text = "${getString(R.string.juz)} " +
                                    LangUtils.translateNums(
                                        this@QuranViewer, currentJuz.toString()
                                    ),
                            fontSize = 18.sp
                        )
                    }
                }
            },
            bottomBar = {
                BottomAppBar(
                    backgroundColor = AppTheme.colors.primary
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Bookmark btn
                        MyIconBtn(
                            iconId =
                                if (currentPage.value == bookmarkedPage.value)
                                    R.drawable.ic_bookmarked
                                else R.drawable.ic_bookmark,
                            description = stringResource(R.string.bookmark_page_button_description),
                            tint =
                                if (currentPage.value == bookmarkedPage.value)
                                    AppTheme.colors.accent
                                else AppTheme.colors.onPrimary
                        ) {
                            bookmarkedPage.value = currentPage.value

                            pref.edit()
                                .putInt("bookmarked_page", currentPage.value)
                                .putInt("bookmarked_sura", currentSura)
                                .apply()
                        }

                        MyImageButton(
                            imageResId = R.drawable.ic_aya_backward,
                            description = stringResource(R.string.rewind_btn_description)
                        ) {
                            player?.transportControls?.skipToPrevious()
                        }

                        MyPlayerBtn(
                            state = playerState
                        ) {
                            if (player == null) {
                                updateButton(PlaybackStateCompat.STATE_BUFFERING)
                                setupPlayer()
                            }
                            else if (player!!.getState() == PlaybackStateCompat.STATE_PLAYING) {
                                player!!.transportControls.pause()
                                updateButton(PlaybackStateCompat.STATE_PAUSED)
                            }
                            else if (player!!.getState() == PlaybackStateCompat.STATE_PAUSED) {
                                if (selected.value == null) player!!.transportControls.play()
                                else {
                                    player!!.setChosenSurah(selected.value!!.getSurahNum())
                                    requestPlay(selected.value!!)
                                }
                                updateButton(PlaybackStateCompat.STATE_PLAYING)
                            }
                        }

                        MyImageButton(
                            imageResId = R.drawable.ic_aya_forward,
                            description = stringResource(R.string.fast_forward_btn_description)
                        ) {
                            player?.transportControls?.skipToNext()
                        }

                        // Preferences btn
                        MyIconBtn(
                            iconId = R.drawable.ic_preferences,
                            description = stringResource(R.string.settings),
                            tint = AppTheme.colors.accent
                        ) {
                            settingsDialog.launch(Intent(
                                this@QuranViewer, QuranSettingsDialog::class.java
                            ))
                        }
                    }
                }
            }
        ) {
            pagerState = rememberPagerState(initialPage-1)
            HorizontalPagerScreen(
                count = 604,
                pagerState = pagerState,
                modifier = Modifier.padding(it)
            ) {
                val coroutineScope = rememberCoroutineScope()
                val scrollState = rememberScrollState()

                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    buildPage(currentPage+1)

                    if (viewType.value == "list") ListItems()
                    else PageItems()

                    if (scrollTo != -1F) {
                        LaunchedEffect(null) {
                            coroutineScope.launch {
                                scrollState.animateScrollTo(scrollTo.roundToInt())
                                scrollTo = -1F
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun PageItems() {
        var text = StringBuilder()
        var sequence = ArrayList<Ayah>()
        var lastSura = pageAyas[0].getSurahNum()

        NewSura(pageAyas[0])

        for (aya in pageAyas) {
            if (aya.getSurahNum() == lastSura) {
                aya.setStart(text.length)
                text.append(aya.getText())
                aya.setEnd(text.length)
                sequence.add(aya)
            }
            else {
                PageItem(text = text.toString(), sequence = sequence)

                NewSura(aya)

                text = StringBuilder()
                sequence = ArrayList()
            }

            lastSura = aya.getSurahNum()
        }

        PageItem(text = text.toString(), sequence = sequence)
    }

    @Composable
    private fun PageItem(text: String, sequence: List<Ayah>) {
        val annotatedString = buildAnnotatedString {
            append(text)

            for (seqAya in sequence) {
                addStyle(
                    style = SpanStyle(color =
                        if (selected.value == seqAya) AppTheme.colors.accent
                        else AppTheme.colors.strongText
                    ),
                    start = seqAya.getStart(),
                    end = seqAya.getEnd()
                )
            }
        }

        Screen(annotatedString = annotatedString, ayaId = sequence[0].getId())
    }

    @Composable
    private fun ListItems() {
        for (aya in pageAyas) {
            NewSura(aya)

            val annotatedString = AnnotatedString(aya.getText()!!)
            Screen(annotatedString, aya.getId())

            MyText(
                text = aya.getTranslation()!!,
                fontSize = (textSize - 5).sp,
                modifier = Modifier.padding(6.dp)
            )

            MyHorizontalDivider()
        }
    }

    @Composable
    private fun Screen(annotatedString: AnnotatedString, ayaId: Int) {
        ClickableText(
            text = annotatedString,
            style = TextStyle(
                fontFamily = uthmanic,
                fontSize = textSize.sp,
                color = AppTheme.colors.strongText,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(vertical = 4.dp),
            onClick = { offset ->
                onAyaClick(ayaId, offset)
            }
        )
    }

    @Composable
    private fun NewSura(aya: Ayah) {
        if (aya.getAyahNum() == 1) {
            SuraHeader(aya)
            // surat al-fatiha and At-Taubah
            if (aya.getSurahNum() != 1 && aya.getSurahNum() != 9) Basmalah()
        }
    }

    @Composable
    private fun SuraHeader(aya: Ayah) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(vertical = 10.dp)
                .onGloballyPositioned { layoutCoordinates ->
                    if (aya.getSurahNum() == initialSura) {
                        scrollTo = layoutCoordinates.positionInRoot().y
                        initialSura = -1
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.surah_header),
                contentDescription = aya.getSurahName(),
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )

            MyText(
                text = aya.getSurahName(),
                fontSize = (textSize + 5).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    @Composable
    private fun Basmalah() {
        MyText(
            text = stringResource(R.string.basmalah),
            fontSize = textSize.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 5.dp)
        )
    }

}