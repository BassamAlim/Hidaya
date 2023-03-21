package bassamalim.hidaya.features.quranViewer

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInParent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.QViewType
import bassamalim.hidaya.core.models.Ayah
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.services.AyahPlayerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.Executors
import javax.inject.Inject

@HiltViewModel
class QuranViewerVM @Inject constructor(
    private val app: Application,
    private val repo: QuranViewerRepo,
    savedStateHandle: SavedStateHandle
): AndroidViewModel(app) {
    
    private val type = savedStateHandle.get<String>("type") ?: "by_surah"
    private var initialSuraId = savedStateHandle.get<Int>("sura_id") ?: 0
    private val page = savedStateHandle.get<Int>("page") ?: 0

    val numeralsLanguage = repo.numeralsLanguage
    val theme = repo.getTheme()
    var initialPage =
        if (type == "by_page") page
        else repo.getPage(initialSuraId)
        private set
    private val suraNames =
        if (repo.language == Language.ENGLISH) repo.getSuraNamesEn()
        else repo.getSuraNames()
    private val ayatDB = repo.getAyat()
    private val handler = Handler(Looper.getMainLooper())
    private var suraNum = 0
    private var bookmarkedPage = repo.getBookmarkedPage()
    val pref = repo.sp
    val reciterNames = repo.getReciterNames()
    private var lastRecordedPage = 0
    private var lastClickT = 0L
    private var lastClickedId = -1
    var scrollTo = -1F
        private set
    val selected = mutableStateOf<Ayah?>(null)
    var tracked = mutableStateOf<Ayah?>(null)
    private val playerState = mutableStateOf(PlaybackStateCompat.STATE_STOPPED)
    private var binder :AyahPlayerService.LocalBinder? = null
    private var serviceBound = false
    private var tc: MediaControllerCompat.TransportControls? = null
    private var uiListener: AyahPlayerService.Coordinator? = null

    private val _uiState = MutableStateFlow(QuranViewerState(
        pageNum = initialPage,
        viewType =
            if (repo.language == Language.ENGLISH) QViewType.List
            else repo.getViewType(),
        textSize = repo.getTextSize(),
        isBookmarked = bookmarkedPage == initialPage,
        ayas = buildPage(initialPage),
        tutorialDialogShown = repo.getShowTutorial()
    ))
    val uiState = _uiState.asStateFlow()

    fun onStart() {

    }

    fun onStop() {
        if (serviceBound) {
            app.unbindService(serviceConnection)
            serviceBound = false
        }

        handler.removeCallbacks(runnable)
    }

    fun onPageChange(currentPage: Int, page: Int) {
        if (currentPage == page) {
            updatePageState(page + 1)

            if (page != lastRecordedPage) {
                handler.removeCallbacks(runnable)
                checkPage()
            }
        }
    }

    fun onBookmarkClick() {
        bookmarkedPage = _uiState.value.pageNum

        _uiState.update { it.copy(
            isBookmarked = true
        )}

        repo.setBookmarkedPage(_uiState.value.pageNum, suraNum)
    }

    fun onPlayPauseClick() {
        val playerService = binder?.service
        if (playerService == null) {
            updateButton(PlaybackStateCompat.STATE_BUFFERING)
            setupPlayer()
        }
        else if (playerService.getState() == PlaybackStateCompat.STATE_PLAYING) {
            updateButton(PlaybackStateCompat.STATE_PAUSED)
            playerService.transportControls.pause()
        }
        else if (playerService.getState() == PlaybackStateCompat.STATE_PAUSED) {
            updateButton(PlaybackStateCompat.STATE_BUFFERING)
            if (selected.value == null) playerService.transportControls.play()
            else {
                playerService.setChosenSurah(selected.value!!.surahNum)
                requestPlay(selected.value!!.id)
            }
        }
    }

    fun onRewindClick() {
        binder?.service?.transportControls?.skipToPrevious()
    }

    fun onFastForwardClick() {
        binder?.service?.transportControls?.skipToNext()
    }

    fun onSettingsClick() {
        _uiState.update { it.copy(
            settingsDialogShown = true
        )}
    }

    fun onAyaClick(ayaId: Int, offset: Int) {
        val startIdx = _uiState.value.ayas.indexOfFirst { it.id == ayaId }

        val maxDuration = 1200
        for (idx in startIdx until _uiState.value.ayas.size) {
            val aya = _uiState.value.ayas[idx]
            if (offset < aya.end) {
                // double click
                if (aya.id == lastClickedId &&
                    System.currentTimeMillis() < lastClickT + maxDuration) {
                    selected.value = null

                    _uiState.update { it.copy(
                        infoDialogShown = true,
                        infoDialogText = aya.tafseer
                    )}
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

    fun onSuraHeaderGloballyPositioned(
        aya: Ayah,
        isCurrentPage: Boolean,
        layoutCoordinates: LayoutCoordinates
    ) {
        if (isCurrentPage && scrollTo == -1F && aya.surahNum == initialSuraId+1) {
            scrollTo = layoutCoordinates.positionInParent().y - 13
            initialSuraId = -1
        }
    }

    fun onScrolled() {
        scrollTo = 0F
    }

    fun onTutorialDialogDismiss(doNotShowAgain: Boolean) {
        _uiState.update { it.copy(
            tutorialDialogShown = false
        )}

        if (doNotShowAgain) repo.setDoNotShowTutorial()
    }

    fun onInfoDialogDismiss() {
        _uiState.update { it.copy(
            infoDialogShown = false
        )}
    }

    fun onSettingsDialogDismiss(viewType: QViewType) {
        _uiState.update { it.copy(
            settingsDialogShown = false,
            viewType = viewType,
            textSize = repo.getTextSize()
        )}

        repo.setViewType(viewType)
    }

    fun buildPage(pageNumber: Int): ArrayList<Ayah> {
        val ayas = ArrayList<Ayah>()

        // get page start
        var counter = ayatDB.indexOfFirst { aya -> aya.page == pageNumber }
        do {
            val aya = ayatDB[counter]
            val suraNum = aya.sura_num // starts from 1
            val ayaNum = aya.aya_num

            ayas.add(
                Ayah(
                    aya.id, aya.jozz, suraNum, ayaNum, suraNames[suraNum - 1],
                    "${aya.aya_text} ", aya.aya_translation_en, aya.aya_tafseer
                )
            )

            counter++
        } while (counter != Global.QURAN_AYAS && ayatDB[counter].page == pageNumber)

        return ayas
    }

    private fun updatePageState(pageNumber: Int) {
        suraNum = ayatDB.first { aya -> aya.page == pageNumber }.sura_num - 1
        _uiState.update { it.copy(
            pageNum = pageNumber,
            suraName = suraNames[suraNum],
            juzNum = ayatDB.first { aya -> aya.page == pageNumber }.jozz,
            ayas = buildPage(pageNumber),
            isBookmarked = bookmarkedPage == pageNumber
        )}
    }

    private fun checkPage() {
        lastRecordedPage = _uiState.value.pageNum
        handler.postDelayed(runnable, 40000)
    }

    private val runnable = Runnable {
        if (_uiState.value.pageNum == lastRecordedPage)
            updateRecords()
    }

    private fun updateRecords() {
        val old = repo.getPagesRecord()
        val new = old + 1

        repo.setPagesRecord(new)

        if (_uiState.value.pageNum == repo.getWerdPage())
            repo.setWerdDone()
    }

    private fun setupPlayer() {
        uiListener = object : AyahPlayerService.Coordinator {
            override fun onUiUpdate(state: Int) {
                updateButton(state)
            }

            override fun nextPage() {
                if (_uiState.value.pageNum < Global.QURAN_PAGES)
                    updatePageState(_uiState.value.pageNum + 1)
            }

            override fun track(ayaId: Int) {
                val idx = _uiState.value.ayas.indexOfFirst { aya -> aya.id == ayaId }

                if (idx == -1) return  // not the same page

                tracked.value = _uiState.value.ayas[idx]
            }
        }

        val playerIntent = Intent(app, AyahPlayerService::class.java)
        app.startService(playerIntent)
        app.bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    //Binding this Client to the AudioPlayer Service
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.i(Global.TAG, "In onServiceConnected")
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            binder = service as AyahPlayerService.LocalBinder
            val playerService = binder?.service
            tc = playerService!!.transportControls
            serviceBound = true

            if (selected.value == null) selected.value = _uiState.value.ayas[0]

            playerService.setChosenPage(_uiState.value.pageNum)
            playerService.setCoordinator(uiListener!!)
            playerService.setChosenSurah(selected.value!!.surahNum)

            requestPlay(selected.value!!.id)

            selected.value = null
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serviceBound = false
        }
    }

    private fun requestPlay(ayahId: Int) {
        Executors.newSingleThreadExecutor().execute {
            tc!!.playFromMediaId(ayahId.toString(), Bundle())
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

}