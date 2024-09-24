package bassamalim.hidaya.features.radio.domain

import android.app.Activity
import android.content.ComponentName
import android.media.AudioManager
import android.os.Build
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import androidx.annotation.RequiresApi
import bassamalim.hidaya.core.data.repositories.LiveContentRepository
import bassamalim.hidaya.features.radio.service.RadioService
import javax.inject.Inject

class RadioDomain @Inject constructor(
    private val liveContentRepository: LiveContentRepository
) {

    private lateinit var activity: Activity
    private var mediaBrowser: MediaBrowserCompat? = null
    private lateinit var controller: MediaControllerCompat
    private lateinit var tc: MediaControllerCompat.TransportControls

    fun setActivity(activity: Activity) {
        this.activity = activity
    }

    fun initializeController() {
        // Get the token for the MediaSession
        val token = mediaBrowser!!.sessionToken

        // Create a MediaControllerCompat
        val mediaController = MediaControllerCompat(activity, token)

        // Save the controller
        MediaControllerCompat.setMediaController(activity, mediaController)
        controller = MediaControllerCompat.getMediaController(activity)
        tc = controller.transportControls
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun connect(connectionCallbacks: MediaBrowserCompat.ConnectionCallback) {
        mediaBrowser = MediaBrowserCompat(
            activity,
            ComponentName(activity, RadioService::class.java),
            connectionCallbacks,
            null
        )
        mediaBrowser?.connect()

        activity.volumeControlStream = AudioManager.STREAM_MUSIC
    }

    fun disconnect(controllerCallback: MediaControllerCompat.Callback) {
        MediaControllerCompat.getMediaController(activity)
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