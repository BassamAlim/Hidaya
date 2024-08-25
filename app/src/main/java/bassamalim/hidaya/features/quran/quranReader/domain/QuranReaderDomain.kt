package bassamalim.hidaya.features.quranReader.domain

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.media3.common.util.UnstableApi
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.QuranRepository
import bassamalim.hidaya.core.data.repositories.UserRepository
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.QuranPageBookmark
import bassamalim.hidaya.core.models.Verse
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.features.quranReader.ayaPlayer.VersePlayerService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors
import javax.inject.Inject

class QuranReaderDomain @Inject constructor(
    private val app: Application,
    private val quranRepository: QuranRepository,
    private val appSettingsRepository: AppSettingsRepository,
    private val userRepository: UserRepository
) {

    private val handler = Handler(Looper.getMainLooper())
    private var mediaBrowser: MediaBrowserCompat? = null
    private var controller: MediaControllerCompat? = null
    private var tc: MediaControllerCompat.TransportControls? = null
    private lateinit var controllerCallback: MediaControllerCompat.Callback
    private var lastRecordedPage = 0
    private lateinit var getPageNumCallback: () -> Int
    private lateinit var getPageVersesCallback: () -> List<Verse>
    private lateinit var getSelectedVerseCallback: () -> Verse?
    private lateinit var setSelectedVerseCallback: (Verse?) -> Unit

    private val runnable = Runnable {
        if (getPageNumCallback() == lastRecordedPage) {
            runBlocking {
                updateRecords()
            }
        }
    }

    private suspend fun updateRecords() {
        val newRecord = getPagesRecord().first() + 1
        setPagesRecord(newRecord)

        if (getPageNumCallback() == getWerdPage().first())
            setWerdDone()
    }

    fun handlePageChange(pageIdx: Int) {
        if (pageIdx != lastRecordedPage) {
            handler.removeCallbacks(runnable)
            checkPage()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(UnstableApi::class)
    fun setupPlayer(
        controllerCallback:MediaControllerCompat.Callback,
        getPageCallback: () -> Int,
        getPageVersesCallback: () -> List<Verse>,
        getSelectedVerseCallback: () -> Verse?,
        setSelectedVerseCallback: (Verse?) -> Unit
    ) {
        this.controllerCallback = controllerCallback
        this.getPageNumCallback = getPageCallback
        this.getSelectedVerseCallback = getSelectedVerseCallback
        this.setSelectedVerseCallback = setSelectedVerseCallback
        this.getPageVersesCallback = getPageVersesCallback

        mediaBrowser = MediaBrowserCompat(
            app,
            ComponentName(app, VersePlayerService::class.java),
            connectionCallbacks,
            null
        )
        mediaBrowser?.connect()

        (app.applicationContext as Activity).volumeControlStream = AudioManager.STREAM_MUSIC
    }

    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            Log.i(Global.TAG, "In onServiceConnected")

            if (mediaBrowser == null) return

            val mediaController: MediaControllerCompat?
            try {
                // Create a MediaControllerCompat
                mediaController = MediaControllerCompat(app, mediaBrowser!!.sessionToken)
            } catch (e: IllegalStateException) {
                Log.e(Global.TAG, "Error in QuranReader: ${e.message}")
                return
            }

            // Save the controller
            MediaControllerCompat.setMediaController(
                (app.applicationContext as Activity),
                mediaController
            )

            if (getSelectedVerseCallback() == null) {
                setSelectedVerseCallback(getPageVersesCallback()[0])
            }

            // Finish building the UI
            buildTransportControls()

            requestPlay(getSelectedVerseCallback()!!.id)
        }

        override fun onConnectionSuspended() {
            Log.e(Global.TAG, "Connection suspended in QuranReader")
            // The Service has crashed.
        }

        override fun onConnectionFailed() {
            Log.e(Global.TAG, "Connection failed in QuranReader")
            // The Service has refused our connection
        }
    }

    private fun buildTransportControls() {
        controller = MediaControllerCompat.getMediaController((app.applicationContext as Activity))
        tc = controller!!.transportControls

        // Register a Callback to stay in sync
        controller?.registerCallback(controllerCallback)
    }

    fun requestPlay(ayaId: Int) {
        Executors.newSingleThreadExecutor().execute {
            tc?.playFromMediaId(ayaId.toString(), Bundle())

            setSelectedVerseCallback(null)
        }
    }

    fun isMediaPlayerInitialized() = mediaBrowser != null

    fun isControllerInitialized() = controller != null

    fun getPlaybackState() = controller?.playbackState?.state ?: PlaybackStateCompat.STATE_NONE

    fun pausePlayer() {
        tc?.pause()
    }

    fun playPlayer() {
        tc?.play()
    }

    fun stopPlayer() {
        MediaControllerCompat.getMediaController(app.applicationContext as Activity)
            ?.unregisterCallback(controllerCallback)

        mediaBrowser?.disconnect()
        mediaBrowser = null

        handler.removeCallbacks(runnable)
    }

    private fun checkPage() {
        lastRecordedPage = getPageNumCallback()
        handler.postDelayed(runnable, 40000)
    }

    fun skipToPreviousTrack() {
        tc?.skipToPrevious()
    }

    fun skipToNextTrack() {
        tc?.skipToNext()
    }

    fun disconnectMediaBrowser() {
        mediaBrowser?.disconnect()
    }

    suspend fun getLanguage() = appSettingsRepository.getLanguage().first()

    suspend fun getNumeralsLanguage() = appSettingsRepository.getNumeralsLanguage().first()

    suspend fun getTheme() = appSettingsRepository.getTheme().first()

    fun getSuraPageNum(suraId: Int) = quranRepository.getSuraPageNum(suraId)

    fun getVersePageNum(ayaId: Int) = quranRepository.getVersePageNum(ayaId)

    fun getAllVerses() = quranRepository.getAllVerses()

    fun getSuraNames(language: Language) = quranRepository.getDecoratedSuraNames(language)

    fun getViewType() = quranRepository.getViewType()

    suspend fun getShouldShowTutorial() = quranRepository.getShouldShowReaderTutorial().first()

    fun getTextSize() = quranRepository.getTextSize()

    fun getPageBookmark() = quranRepository.getPageBookmark()

    suspend fun setBookmarkedPage(pageBookmark: QuranPageBookmark?) {
        quranRepository.setPageBookmark(pageBookmark)
    }

    fun getPagesRecord() = userRepository.getLocalRecord().map { 
        it.quranPages
    }

    suspend fun setPagesRecord(record: Int) {
        userRepository.setLocalRecord(
            userRepository.getLocalRecord().first().copy(
                quranPages = record
            )
        )
    }

    fun getWerdPage() = quranRepository.getWerdPage()

    suspend fun setWerdDone() {
        quranRepository.setIsWerdDone(true)
    }

    suspend fun setDoNotShowTutorial() {
        quranRepository.setShouldShowReaderTutorial(false)
    }

    fun getScreenHeight() = app.resources.displayMetrics.heightPixels

}