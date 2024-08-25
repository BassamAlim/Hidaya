package bassamalim.hidaya.features.recitations.recitationsPlayer.domain

import android.app.Activity
import android.app.Application
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.IntentFilter
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.media3.common.util.UnstableApi
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.QuranRepository
import bassamalim.hidaya.core.data.repositories.RecitationsRepository
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.Recitation
import bassamalim.hidaya.core.utils.FileUtils
import bassamalim.hidaya.features.recitations.recitationsPlayer.RecitationsPlayerService
import kotlinx.coroutines.flow.first
import java.io.File
import java.util.Locale
import javax.inject.Inject

class RecitationsPlayerDomain @Inject constructor(
    private val app: Application,
    private val quranRepository: QuranRepository,
    private val recitationsRepository: RecitationsRepository,
    private val settingsRepository: AppSettingsRepository
) {

    private var mediaBrowser: MediaBrowserCompat? = null
    private lateinit var controller: MediaControllerCompat
    private lateinit var tc: MediaControllerCompat.TransportControls
    private var path = ""

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(UnstableApi::class)
    fun connect(
        connectionCallbacks: MediaBrowserCompat.ConnectionCallback,
        onComplete: BroadcastReceiver
    ) {
        mediaBrowser = MediaBrowserCompat(
            app,
            ComponentName(app, RecitationsPlayerService::class.java),
            connectionCallbacks,
            null
        )
        mediaBrowser?.connect()

        (app.applicationContext as Activity).volumeControlStream = AudioManager.STREAM_MUSIC

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            app.registerReceiver(
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_NOT_EXPORTED
            )
        else
            app.registerReceiver(
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            )
    }

    fun stopMediaBrowser(
        controllerCallback: MediaControllerCompat.Callback,
        onComplete: BroadcastReceiver
    ) {
        try {
            app.unregisterReceiver(onComplete)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }

        MediaControllerCompat.getMediaController(app.applicationContext as Activity)
            ?.unregisterCallback(controllerCallback)

        disconnectMediaBrowser()
    }

    fun disconnectMediaBrowser() {
        mediaBrowser?.disconnect()
    }

    fun isMediaBrowserInitialized() = mediaBrowser != null

    fun initializeController(controllerCallback: MediaControllerCompat.Callback) {
        // Get the token for the MediaSession
        val token = mediaBrowser!!.sessionToken

        // Create a MediaControllerCompat
        val mediaController = MediaControllerCompat(app, token)

        // Save the controller
        MediaControllerCompat.setMediaController(
            app.applicationContext as Activity,
            mediaController
        )

        controller = MediaControllerCompat.getMediaController(app.applicationContext as Activity)
        tc = controller.transportControls

        // Register a Callback to stay in sync
        controller.registerCallback(controllerCallback)
    }

    fun sendPlayRequest(
        mediaId: String,
        playType: String,
        reciterName: String,
        narration: Recitation.Narration
    ) {
        // Pass media data
        val bundle = Bundle()
        bundle.putString("play_type", playType)
        bundle.putString("reciter_name", reciterName)
        bundle.putSerializable("narration", narration)

        // Start Playback
        tc.playFromMediaId(mediaId, bundle)
    }

    fun pause() = tc.pause()

    fun resume() = tc.play()

    fun seekTo(pos: Long) = tc.seekTo(pos)

    fun skipToNext() = tc.skipToNext()

    fun skipToPrevious() = tc.skipToPrevious()

    fun getState() = controller.playbackState.state

    fun getMetadata() = controller.metadata

    fun getPlaybackState() = controller.playbackState

    fun downloadRecitation(
        narration: Recitation.Narration,
        suraIdx: Int,
        suraName: String
    ) {
        val server = narration.server
        val link = String.format(Locale.US, "%s/%03d.mp3", server, suraIdx + 1)
        val uri = Uri.parse(link)

        val request = DownloadManager.Request(uri)
        request.setTitle(suraName)
        FileUtils.createDir(app, path)
        request.setDestinationInExternalFilesDir(app, path, "${suraIdx}.mp3")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)

        (app.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
    }

    fun deleteRecitation() {
        FileUtils.deleteFile(app, path)
    }

    fun setPath(reciterId: Int, narrationId: Int, suraId: Int) {
        this.path = "${"${recitationsRepository.prefix}${reciterId}/${narrationId}/"}$suraId.mp3"
    }

    suspend fun getLanguage() = settingsRepository.getLanguage().first()

    fun getSuraNames(language: Language) = quranRepository.getDecoratedSuraNames(language)

    fun getReciterName(id: Int, language: Language) =
        recitationsRepository.getReciterName(id, language)

    fun getNarration(reciterId: Int, narrationId: Int) =
        recitationsRepository.getNarration(reciterId, narrationId)

    fun getRepeatMode() = recitationsRepository.getRepeatMode()

    suspend fun setRepeatMode(mode: Int) {
        tc.setRepeatMode(mode)
        recitationsRepository.setRepeatMode(mode)
    }

    fun getShuffleMode() = recitationsRepository.getShuffleMode()

    suspend fun setShuffleMode(mode: Int) {
        tc.setShuffleMode(mode)
        recitationsRepository.setShuffleMode(mode)
    }

    fun checkDownload(): DownloadState {
        return if (File("${app.getExternalFilesDir(null)}$path").exists())
            DownloadState.DOWNLOADED
        else
            DownloadState.NOT_DOWNLOADED
    }

}