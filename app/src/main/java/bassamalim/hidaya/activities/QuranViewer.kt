package bassamalim.hidaya.activities

import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.activity.compose.setContent
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
import bassamalim.hidaya.dialogs.QuranSettingsDialog
import bassamalim.hidaya.models.Ayah
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.services.AyahPlayerService
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.ui.theme.nsp
import bassamalim.hidaya.ui.theme.uthmanic
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.DBUtils
import bassamalim.hidaya.utils.LangUtils
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import java.util.concurrent.Executors

class QuranViewer : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var pref: SharedPreferences
    private lateinit var action: String
    private lateinit var ayatDB: List<AyatDB?>
    private lateinit var names: List<String>
    private var textSize = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var currentAyas: List<Ayah>
    private var lastRecordedPage = 0
    private var initialPage = 0
    private var initialSura = -1
    private var currentSura = 0
    private var currentJuz = 0
    private var lastClickT = 0L
    private var lastClickedId = -1
    private var scrollTo = -1F
    private var infoDialogText = ""
    private val infoDialogShown = mutableStateOf(false)
    private val settingsDialogShown = mutableStateOf(false)

    private val viewType = mutableStateOf("page")
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

        viewType.value =
            if (language == "en") "list"
            else pref.getString("quran_view_type", "page")!!

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

    private fun buildPage(pageNumber: Int): ArrayList<Ayah> {
        val ayas = ArrayList<Ayah>()

        // get page start
        var counter = ayatDB.indexOfFirst { aya -> aya!!.page == pageNumber }
        do {
            val aya = ayatDB[counter]!!
            val suraNum = aya.sura_num // starts from 1
            val ayaNum = aya.aya_num

            ayas.add(
                Ayah(
                    aya.id, aya.jozz, suraNum, ayaNum, names[suraNum - 1],
                    "${aya.aya_text} ", aya.aya_translation_en, aya.aya_tafseer
                )
            )

            counter++
        } while (counter != Global.QURAN_AYAS && ayatDB[counter]!!.page == pageNumber)

        return ayas
    }

    private fun updateTopBar() {
        currentSura = ayatDB.first { aya -> aya!!.page == currentPage.value }!!.sura_num - 1
        currentJuz = ayatDB.first { aya -> aya!!.page == currentPage.value }!!.jozz
    }

    private fun onAyaClick(ayaId: Int, offset: Int) {
        val startIdx = currentAyas.indexOfFirst { it.id == ayaId }

        val maxDuration = 1200
        for (idx in startIdx until currentAyas.size) {
            val aya = currentAyas[idx]

            if (offset < aya.end) {
                // double click
                if (aya.id == lastClickedId &&
                    System.currentTimeMillis() < lastClickT + maxDuration) {
                    selected.value = null

                    infoDialogText = aya.tafseer
                    infoDialogShown.value = true
                }
                else {  // single click
                    if (selected.value == aya) selected.value = null
                    else selected.value = aya
                }

                lastClickedId = aya.id
                lastClickT = System.currentTimeMillis()
                break
            }
        }
    }

    private fun checkPage() {
        lastRecordedPage = currentPage.value
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

            override fun nextPage() {
                if (currentPage.value < Global.QURAN_PAGES) currentPage.value++
            }

            override fun track(ayaId: Int) {
                val idx = currentAyas.indexOfFirst { aya -> aya.id == ayaId }

                if (idx == -1) return  // not the same page

                tracked.value = currentAyas[idx]
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

            if (selected.value == null) selected.value = currentAyas[0]

            player!!.setChosenPage(currentPage.value)
            player!!.setCoordinator(uiListener!!)
            player!!.setChosenSurah(selected.value!!.surahNum)

            requestPlay(selected.value!!.id)

            selected.value = null
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serviceBound = false
        }
    }

    private fun requestPlay(ayahId: Int) {
        val bundle = Bundle()
        Executors.newSingleThreadExecutor().execute {
            tc!!.playFromMediaId(ayahId.toString(), bundle)
        }
    }

    private fun updateButton(state: Int) {
        when (state) {
            PlaybackStateCompat.STATE_NONE,
            PlaybackStateCompat.STATE_PAUSED,
            PlaybackStateCompat.STATE_STOPPED,
            PlaybackStateCompat.STATE_PLAYING,
            PlaybackStateCompat.STATE_BUFFERING -> playerState.value = state
            else -> {}
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
            backgroundColor = AppTheme.colors.quranBG,
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
                            fontSize = 18.nsp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Start,
                            textColor = AppTheme.colors.onPrimary
                        )

                        MyText(
                            text = "${getString(R.string.page)} " +
                                    LangUtils.translateNums(
                                        this@QuranViewer, currentPage.value.toString()
                                    ),
                            fontSize = 18.nsp,
                            fontWeight = FontWeight.Bold,
                            textColor = AppTheme.colors.onPrimary
                        )

                        MyText(
                            text = "${getString(R.string.juz)} " +
                                    LangUtils.translateNums(
                                        this@QuranViewer, currentJuz.toString()
                                    ),
                            fontSize = 18.nsp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            textColor = AppTheme.colors.onPrimary
                        )
                    }
                }
            },
            bottomBar = {
                BottomAppBar(
                    backgroundColor = AppTheme.colors.primary
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Bookmark btn
                        MyIconBtn(
                            iconId =
                                if (currentPage.value == bookmarkedPage.value)
                                    R.drawable.ic_bookmarked
                                else R.drawable.ic_bookmark,
                            description = stringResource(R.string.bookmark_page_button_description),
                            tint = AppTheme.colors.onPrimary,
                            size = 40.dp
                        ) {
                            bookmarkedPage.value = currentPage.value

                            pref.edit()
                                .putInt("bookmarked_page", currentPage.value)
                                .putInt("bookmarked_sura", currentSura)
                                .apply()
                        }

                        Row {
                            MyImageButton(
                                imageResId = R.drawable.ic_aya_backward,
                                description = stringResource(R.string.rewind_btn_description)
                            ) {
                                player?.transportControls?.skipToPrevious()
                            }

                            MyPlayerBtn(
                                playerState,
                                size = 50.dp,
                                padding = 5.dp,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            ) {
                                if (player == null) {
                                    updateButton(PlaybackStateCompat.STATE_BUFFERING)
                                    setupPlayer()
                                }
                                else if (player!!.getState() == PlaybackStateCompat.STATE_PLAYING) {
                                    updateButton(PlaybackStateCompat.STATE_PAUSED)
                                    player!!.transportControls.pause()
                                }
                                else if (player!!.getState() == PlaybackStateCompat.STATE_PAUSED) {
                                    updateButton(PlaybackStateCompat.STATE_BUFFERING)
                                    if (selected.value == null) player!!.transportControls.play()
                                    else {
                                        player!!.setChosenSurah(selected.value!!.surahNum)
                                        requestPlay(selected.value!!.id)
                                    }
                                }
                            }

                            MyImageButton(
                                imageResId = R.drawable.ic_aya_forward,
                                description = stringResource(R.string.fast_forward_btn_description)
                            ) {
                                player?.transportControls?.skipToNext()
                            }
                        }

                        // Preferences btn
                        MyIconBtn(
                            iconId = R.drawable.ic_preferences,
                            description = stringResource(R.string.settings),
                            tint = AppTheme.colors.onPrimary,
                            size = 44.dp
                        ) {
                            settingsDialogShown.value = true
                        }
                    }
                }
            }
        ) {
            val pagerState = rememberPagerState(initialPage-1)
            HorizontalPagerScreen(
                count = 604,
                pagerState = pagerState,
                modifier = Modifier.padding(it)
            ) { page ->
                val isCurrentPage = page == pagerState.currentPage

                val scrollState = rememberScrollState()

                if (pagerState.currentPage != this@QuranViewer.currentPage.value)
                    handler.removeCallbacks(runnable)

                this@QuranViewer.currentPage.value = pagerState.currentPage + 1

                updateTopBar()

                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val ayas = buildPage(page+1)

                    if (isCurrentPage) currentAyas = ayas

                    if (viewType.value == "list") ListItems(ayas, isCurrentPage)
                    else PageItems(ayas, isCurrentPage)

                    if (page == pagerState.currentPage)
                        LaunchedEffect(null) {
                            scrollState.animateScrollTo(scrollTo.toInt())
                            scrollTo = 0F
                        }
                }

                checkPage()
            }
        }

        TutorialDialog(
            textResId = R.string.quran_tips,
            prefKey = "is_first_time_in_quran"
        )

        if (infoDialogShown.value)
            InfoDialog(
                title = stringResource(R.string.tafseer),
                text = infoDialogText,
                infoDialogShown
            )

        if (settingsDialogShown.value)
            QuranSettingsDialog(pref, db, settingsDialogShown) {
                viewType.value = pref.getString("quran_view_type", "page")!!
                textSize = pref.getInt(getString(R.string.quran_text_size_key), 30)
            }.Dialog()
    }

    @Composable
    private fun PageItems(ayas: List<Ayah>, isCurrentPage: Boolean) {
        var text = StringBuilder()
        var sequence = ArrayList<Ayah>()
        var lastSura = ayas[0].surahNum

        NewSura(ayas[0], isCurrentPage)
        for (aya in ayas) {
            if (aya.surahNum != lastSura) {
                PageItem(text = text.toString(), sequence = sequence)

                NewSura(aya, isCurrentPage)

                text = StringBuilder()
                sequence = ArrayList()
            }

            aya.start = text.length
            text.append(aya.text)
            aya.end = text.length
            sequence.add(aya)

            lastSura = aya.surahNum
        }
        PageItem(text = text.toString(), sequence = sequence)
    }

    @Composable
    private fun PageItem(text: String, sequence: List<Ayah>) {
        val annotatedString = buildAnnotatedString {
            append(text)

            for (seqAya in sequence) {
                addStyle(
                    style = SpanStyle(
                        color =
                            if (selected.value == seqAya) AppTheme.colors.highlight
                            else if (tracked.value == seqAya) AppTheme.colors.track
                            else AppTheme.colors.strongText
                    ),
                    start = seqAya.start,
                    end = seqAya.end
                )
            }
        }

        Screen(annotatedString = annotatedString, ayaId = sequence[0].id)
    }

    @Composable
    private fun ListItems(ayas: List<Ayah>, isCurrentPage: Boolean) {
        for (aya in ayas) {
            NewSura(aya, isCurrentPage)

            val annotatedString = AnnotatedString(aya.text!!)
            Screen(annotatedString, aya.id)

            MyText(
                text = aya.translation!!,
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
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp),
            onClick = { offset ->
                onAyaClick(ayaId, offset)
            }
        )
    }

    @Composable
    private fun NewSura(aya: Ayah, isCurrentPage: Boolean) {
        if (aya.ayahNum == 1) {
            SuraHeader(aya, isCurrentPage)
            // surat al-fatiha and At-Taubah
            if (aya.surahNum != 1 && aya.surahNum != 9) Basmalah()
        }
    }

    @Composable
    private fun SuraHeader(aya: Ayah, isCurrentPage: Boolean) {
        Box(
            Modifier
                .fillMaxWidth()
                .height((textSize * 2.6).dp)
                .padding(top = 5.dp, bottom = 10.dp, start = 5.dp, end = 5.dp)
                .onGloballyPositioned { layoutCoordinates ->
                    if (isCurrentPage) {
                        if (aya.surahNum == initialSura+1) {
                            scrollTo = layoutCoordinates.positionInParent().y - 13
                            initialSura = -1
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.surah_header),
                contentDescription = aya.surahName,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )

            MyText(
                text = aya.surahName,
                fontSize = (textSize + 2).sp,
                fontWeight = FontWeight.Bold,
                textColor = AppTheme.colors.onPrimary
            )
        }
    }

    @Composable
    private fun Basmalah() {
        MyText(
            text = stringResource(R.string.basmalah),
            fontSize = (textSize - 3).sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 5.dp)
        )
    }

}