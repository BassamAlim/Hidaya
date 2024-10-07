package bassamalim.hidaya.features.recitations.player.domain

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
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.media3.common.util.UnstableApi
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.QuranRepository
import bassamalim.hidaya.core.data.repositories.RecitationsRepository
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.utils.FileUtils
import bassamalim.hidaya.features.recitations.player.service.RecitationPlayerService
import bassamalim.hidaya.features.recitations.recitersMenu.domain.Recitation
import kotlinx.coroutines.flow.first
import java.io.File
import java.util.Locale
import javax.inject.Inject

class RecitationPlayerDomain @Inject constructor(
    private val app: Application,
    private val recitationsRepository: RecitationsRepository,
    private val quranRepository: QuranRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    private lateinit var activity: Activity
    private var mediaBrowser: MediaBrowserCompat? = null
    private lateinit var controller: MediaControllerCompat
    private lateinit var tc: MediaControllerCompat.TransportControls
    private var path = ""

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(UnstableApi::class)
    fun connect(
        activity: Activity,
        connectionCallbacks: MediaBrowserCompat.ConnectionCallback,
        onComplete: BroadcastReceiver
    ) {
        this.activity = activity

        mediaBrowser = MediaBrowserCompat(
            activity,
            ComponentName(activity, RecitationPlayerService::class.java),
            connectionCallbacks,
            null
        )
        mediaBrowser?.connect()

        activity.volumeControlStream = AudioManager.STREAM_MUSIC

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            activity.registerReceiver(
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_NOT_EXPORTED
            )
        else
            activity.registerReceiver(
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            )
    }

    fun stopMediaBrowser(
        controllerCallback: MediaControllerCompat.Callback,
        onComplete: BroadcastReceiver
    ) {
        try {
            activity.unregisterReceiver(onComplete)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }

        MediaControllerCompat.getMediaController(activity)
            ?.unregisterCallback(controllerCallback)

        disconnectMediaBrowser()
    }

    fun disconnectMediaBrowser() {
        mediaBrowser?.disconnect()
    }

    fun isMediaBrowserInitialized() = mediaBrowser != null

    fun initializeController(controllerCallback: MediaControllerCompat.Callback) {
        Log.d(Global.TAG, "in initializeController of RecitationPlayerDomain")

        // Get the token for the MediaSession
        val token = mediaBrowser!!.sessionToken

        // Create a MediaControllerCompat
        val mediaController = MediaControllerCompat(activity, token)

        // Save the controller
        MediaControllerCompat.setMediaController(activity, mediaController)

        controller = MediaControllerCompat.getMediaController(activity)
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

    fun getMetadata(): MediaMetadataCompat = controller.metadata

    fun getPlaybackState(): PlaybackStateCompat = controller.playbackState

    fun downloadRecitation(
        narration: Recitation.Narration,
        suraIdx: Int,
        suraName: String
    ) {
        val server = narration.server
        val link = String.format(Locale.US, "%s/%03d.mp3", server, suraIdx+1)
        val uri = Uri.parse(link)

        val request = DownloadManager.Request(uri)
        request.setTitle(suraName)
        FileUtils.createDir(app, path)
        request.setDestinationInExternalFilesDir(app, path, "${suraIdx}.mp3")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)

        (activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
    }

    fun deleteRecitation() {
        FileUtils.deleteFile(app, path)
    }

    fun setPath(reciterId: Int, narrationId: Int) {
        this.path = "${recitationsRepository.prefix}${reciterId}/$narrationId/"
    }

    suspend fun getLanguage() = appSettingsRepository.getLanguage().first()

    suspend fun getSuraNames(language: Language) = quranRepository.getDecoratedSuraNames(language)

    suspend fun getReciterName(id: Int, language: Language) =
        recitationsRepository.getSuraReciterName(id, language)

    suspend fun getNarration(reciterId: Int, narrationId: Int, language: Language) =
        recitationsRepository.getNarration(reciterId, narrationId, language).let {
            Recitation.Narration(
                id = it.id,
                name = it.name,
                server = it.server,
                availableSuras = it.availableSuras,
                downloadState = recitationsRepository.getNarrationDownloadState(
                    reciterId,
                    narrationId
                )
            )
        }

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