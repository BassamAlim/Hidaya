package bassamalim.hidaya.features.radio.domain

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.media.AudioManager
import android.os.Build
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import androidx.annotation.RequiresApi
import bassamalim.hidaya.core.data.repositories.LiveContentRepository
import bassamalim.hidaya.features.radio.RadioService
import javax.inject.Inject

class RadioDomain @Inject constructor(
    private val app: Application,
    private val liveContentRepository: LiveContentRepository
) {

    private var mediaBrowser: MediaBrowserCompat? = null
    private lateinit var controller: MediaControllerCompat
    private lateinit var tc: MediaControllerCompat.TransportControls

    fun initializeController() {
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
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun connect(connectionCallbacks: MediaBrowserCompat.ConnectionCallback) {
        mediaBrowser = MediaBrowserCompat(
            app,
            ComponentName(app, RadioService::class.java),
            connectionCallbacks,
            null
        )
        mediaBrowser?.connect()

        (app.applicationContext as Activity).volumeControlStream = AudioManager.STREAM_MUSIC
    }

    fun disconnect(controllerCallback: MediaControllerCompat.Callback) {
        MediaControllerCompat.getMediaController(app.applicationContext as Activity)
            ?.unregisterCallback(controllerCallback)

        mediaBrowser?.disconnect()
    }

    fun play(url: String) {
        tc.playFromMediaId(url, null)
    }

    fun pause() {
        tc.pause()
    }

    fun resume() {
        tc.play()
    }

    fun registerCallback(controllerCallback: MediaControllerCompat.Callback) {
        controller.registerCallback(controllerCallback)
    }

    fun getState() = controller.playbackState.state

    fun getUrl() = liveContentRepository.getRadioUrl()

}