package bassamalim.hidaya.viewmodel

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
import bassamalim.hidaya.enum.Language
import bassamalim.hidaya.enum.QViewType.*
import bassamalim.hidaya.models.Ayah
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.repository.QuranViewerRepo
import bassamalim.hidaya.services.AyahPlayerService
import bassamalim.hidaya.state.QuranViewerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.collections.List

@HiltViewModel
class QuranViewerVM @Inject constructor(
    private val app: Application,
    private val repository: QuranViewerRepo,
    savedStateHandle: SavedStateHandle
): AndroidViewModel(app) {
    
    private val type = savedStateHandle.get<String>("type") ?: "by_surah"
    private var initialSuraId = savedStateHandle.get<Int>("surah_id") ?: 0
    private val page = savedStateHandle.get<Int>("page") ?: 0

    val numeralsLanguage = repository.numeralsLanguage
    var initialPage =
        if (type == "by_page") page
        else repository.getPage(initialSuraId)
        private set
    private val ayatDB = repository.getAyat()
    private val suraNames =
        if (repository.language == Language.ENGLISH) repository.getSuraNamesEn()
        else repository.getSuraNames()
    private val handler = Handler(Looper.getMainLooper())
    private var suraNum = 0
    private var bookmarkedPage = repository.getBookmarkedPage()
    val pref = repository.pref
    val reciterNames = repository.getReciterNames()

    private lateinit var currentAyas: List<Ayah>
    private var lastRecordedPage = 0
    private var lastClickT = 0L
    private var lastClickedId = -1
    var scrollTo = -1F
        private set

    val selected = mutableStateOf<Ayah?>(null)
    var tracked = mutableStateOf<Ayah?>(null)

    private val playerState = mutableStateOf(PlaybackStateCompat.STATE_STOPPED)
    private var player: AyahPlayerService? = null
    private var serviceBound = false
    private var tc: MediaControllerCompat.TransportControls? = null
    private var uiListener: AyahPlayerService.Coordinator? = null

    private val _uiState = MutableStateFlow(QuranViewerState(
        pageNum = initialPage,
        viewType =
            if (repository.language == Language.ENGLISH) List
            else repository.getViewType(),
        textSize = repository.getTextSize(),
        isBookmarked = bookmarkedPage == initialPage,
    ))
    val uiState = _uiState.asStateFlow()
    
    fun onStart() {
        
    }

    fun onStop() {
        player?.finish()
        if (serviceBound) {
            app.applicationContext.unbindService(serviceConnection)
            player?.stopSelf()
        }
        player?.onDestroy()
        handler.removeCallbacks(runnable)
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
            ayas = buildPage(pageNumber + 1),
            isBookmarked = bookmarkedPage == pageNumber
        )}
    }

    private fun checkPage() {
        lastRecordedPage = _uiState.value.pageNum
        handler.postDelayed(runnable, 40000)
    }

    private val runnable = Runnable {
        if (_uiState.value.pageNum == lastRecordedPage) updateRecord()
    }

    private fun updateRecord() {
        val old = repository.getPagesRecord()
        val new = old + 1

        repository.setPagesRecord(new)

        if (_uiState.value.pageNum == repository.getWerdPage())
            repository.setWerdDone()
    }

    private fun setupPlayer() {
        uiListener = object : AyahPlayerService.Coordinator {
            override fun onUiUpdate(state: Int) {
                updateButton(state)
            }

            override fun nextPage() {
                if (_uiState.value.pageNum < Global.QURAN_PAGES)
                    onNextPage()
            }

            override fun track(ayaId: Int) {
                val idx = currentAyas.indexOfFirst { aya -> aya.id == ayaId }

                if (idx == -1) return  // not the same page

                tracked.value = currentAyas[idx]
            }
        }

        val ctx = app.applicationContext
        val playerIntent = Intent(ctx, AyahPlayerService::class.java)
        ctx.startService(playerIntent)
        ctx.bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE)
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

            player!!.setChosenPage(_uiState.value.pageNum)
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

    fun onPageChange(pagerStatePage: Int, page: Int) {
        if (pagerStatePage == page) {
            updatePageState(page + 1)

            if (page != lastRecordedPage) {
                handler.removeCallbacks(runnable)
                checkPage()
            }
        }
    }

    fun onNextPage() {
        updatePageState(_uiState.value.pageNum + 1)
    }

    fun onPrevPage() {
        if (_uiState.value.pageNum > 0)
            updatePageState(_uiState.value.pageNum - 1)
    }

    fun onBookmarkClick() {
        bookmarkedPage = _uiState.value.pageNum

        _uiState.update { it.copy(
            isBookmarked = true
        )}

        repository.setBookmarkedPage(_uiState.value.pageNum, suraNum)
    }

    fun onPlayPauseClick() {
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

    fun onRewindClick() {
        player?.transportControls?.skipToPrevious()
    }

    fun onFastForwardClick() {
        player?.transportControls?.skipToNext()
    }

    fun onSettingsClick() {
        _uiState.update { it.copy(
            settingsDialogShown = true
        )}
    }

    fun onAyaClick(ayaId: Int, offset: Int) {
        val startIdx = currentAyas.indexOfFirst { it.id == ayaId }

        val maxDuration = 1200
        for (idx in startIdx until currentAyas.size) {
            val aya = currentAyas[idx]

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

        if (doNotShowAgain) repository.setDoNotShowTutorial()
    }

    fun onSettingsDialogDone() {
        _uiState.update { it.copy(
            settingsDialogShown = false,
            viewType = repository.getViewType(),
            textSize = repository.getTextSize()
        )}
    }

}