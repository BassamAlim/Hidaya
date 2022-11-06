package bassamalim.hidaya.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer
import android.net.Uri
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.WifiLock
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import bassamalim.hidaya.R
import bassamalim.hidaya.activities.RadioClient
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.utils.ActivityUtils
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

@RequiresApi(Build.VERSION_CODES.O)
class RadioService : MediaBrowserServiceCompat(), OnAudioFocusChangeListener {

    private lateinit var playPauseAction: NotificationCompat.Action
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var controller: MediaControllerCompat
    private var channelId = "channel ID"
    private val id = 444
    private var flags = 0
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notification: Notification
    private lateinit var player: MediaPlayer
    private lateinit var am: AudioManager
    private val intentFilter: IntentFilter = IntentFilter()
    private lateinit var audioFocusRequest: AudioFocusRequest
    private lateinit var wifiLock: WifiLock
    private var staticUrl: String? = null
    private lateinit var dynamicUrl: String

    companion object {
        private const val MY_MEDIA_ROOT_ID = "media_root_id"
        private const val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"
        private const val ACTION_PLAY_PAUSE = "bassamalim.hidaya.services.RadioService.playpause"
        private const val ACTION_STOP = "bassamalim.hidaya.services.RadioService.stop"
    }

    override fun onCreate() {
        super.onCreate()
        ActivityUtils.onActivityCreateSetLocale(this)

        initSession()
        initPlayer()
        setActions()
        initMediaSessionMetadata()
    }

    val callback: MediaSessionCompat.Callback = object : MediaSessionCompat.Callback() {
        override fun onPlayFromMediaId(mediaId: String, extras: Bundle) {
            Log.i(Global.TAG, "In onPlayFromMediaId of RadioClient")
            super.onPlayFromMediaId(mediaId, extras)
            if (staticUrl == null) {
                staticUrl = mediaId
                thread.start()    // get final URL
            }
        }

        override fun onPlay() {
            super.onPlay()

            buildNotification()
            // Request audio focus for playback, this registers the afChangeListener
            var result: Int = AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                result = am.requestAudioFocus(audioFocusRequest)

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // Start the service
                startService(Intent(applicationContext, RadioService::class.java))
                // Set the session active  (and update metadata and state)
                mediaSession.isActive = true

                // start the player
                startPlaying()

                updatePbState(PlaybackStateCompat.STATE_PLAYING, player.currentPosition)
                updateNotification(true)

                // Register Receiver
                registerReceiver(receiver, intentFilter)
                // Put the service in the foreground, post notification
                startForeground(id, notification)

                wifiLock.acquire()
            }
        }

        override fun onStop() {
            Log.i(Global.TAG, "In onStop of RadioClient")
            super.onStop()

            updatePbState(PlaybackStateCompat.STATE_STOPPED, player.currentPosition)

            try {
                unregisterReceiver(receiver)
            } catch (ignored: IllegalArgumentException) {}
            am.abandonAudioFocusRequest(audioFocusRequest)    // Abandon audio focus
            if (wifiLock.isHeld) wifiLock.release()
            player.release()

            stopSelf()    // Stop the service
            mediaSession.isActive = false    // Set the session inactive
            stopForeground()    // Take the service out of the foreground
        }

        override fun onPause() {
            super.onPause()
            Log.i(Global.TAG, "In onPause of RadioClient")

            // pause the player (custom call)
            mediaSession.isActive = false
            player.stop()    // since its radio, it can not be paused and resumed normally

            // Update metadata and state
            updatePbState(PlaybackStateCompat.STATE_PAUSED, player.currentPosition)
            updateNotification(false)

            // Take the service out of the foreground, retain the notification
            stopForeground()
        }
    }

    override fun onAudioFocusChange(i: Int) {
        when (i) {
            AudioManager.AUDIOFOCUS_GAIN -> player.setVolume(1.0f, 1.0f)
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->
                if (player.isPlaying) player.setVolume(0.3f, 0.3f)
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT, AudioManager.AUDIOFOCUS_LOSS ->
                if (player.isPlaying) callback.onPause()
        }
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                AudioManager.ACTION_AUDIO_BECOMING_NOISY -> {
                    Log.i(Global.TAG, "In ACTION_BECOMING_NOISY of RadioService")
                    callback.onPause()
                }
                ACTION_PLAY_PAUSE ->
                    if (controller.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
                        Log.i(Global.TAG, "In ACTION_PAUSE of RadioService")
                        callback.onPause()
                    }
                    else if (controller.playbackState.state == PlaybackStateCompat.STATE_PAUSED) {
                        Log.i(Global.TAG, "In ACTION_PLAY of RadioService")
                        callback.onPlay()
                    }
                ACTION_STOP -> {
                    Log.i(Global.TAG, "In ACTION_STOP of RadioService")
                    callback.onStop()
                }
            }
        }
    }

    private fun initSession() {
        // Create a MediaSessionCompat
        mediaSession = MediaSessionCompat(this, "RadioService")

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
        val stateBuilder: PlaybackStateCompat.Builder = PlaybackStateCompat.Builder().setActions(
            PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE
        )
        mediaSession.setPlaybackState(stateBuilder.build())

        // callback() has methods that handle callbacks from a media controller
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) mediaSession.setCallback(callback)

        // Set the session's token so that client activities can communicate with it.
        sessionToken = mediaSession.sessionToken
    }

    private fun buildNotification() {
        // Given a media session and its context (usually the component containing the session)

        // Get the session's metadata
        controller = mediaSession.controller
        val mediaMetadata: MediaMetadataCompat = controller.metadata
        val description: MediaDescriptionCompat = mediaMetadata.description
        mediaSession.setSessionActivity(getContentIntent())
        createNotificationChannel()

        // Create a NotificationCompat.Builder
        notificationBuilder = NotificationCompat.Builder(this, channelId)

        notificationBuilder
            // Add the metadata for the currently playing track
            .setContentTitle(description.title)
            // Stop the service when the notification is swiped away
            .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                PlaybackStateCompat.ACTION_STOP))
            // Make the transport controls visible on the lockscreen
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            // Add an app icon and set its accent color
            // Be careful about the color
            .setSmallIcon(R.drawable.launcher_foreground)
            .setColorized(true)
            .setColor(resources.getColor(R.color.surface_M, theme))
            // Add buttons
            // Enable launching the player by clicking the notification
            .setContentIntent(controller.sessionActivity)
            .addAction(playPauseAction)
            // So there will be no notification tone
            .setSilent(true)
            // So the user wouldn't swipe it off
            .setOngoing(true)
            // Take advantage of MediaStyle features
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0)
                    .setShowCancelButton(true) // Add a cancel button
                    .setCancelButtonIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            this, PlaybackStateCompat.ACTION_STOP
                        )
                    )
            )
        notification = notificationBuilder.build()

        // Display the notification and place the service in the foreground
        startForeground(id, notification)
    }

    private fun initMediaSessionMetadata() {
        val metadataBuilder: MediaMetadataCompat.Builder = MediaMetadataCompat.Builder()

        //Notification icon in card
        metadataBuilder.putBitmap(
            MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON,
            BitmapFactory.decodeResource(resources, R.color.surface_M)
        )
        metadataBuilder.putBitmap(
            MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
            BitmapFactory.decodeResource(resources, R.color.surface_M)
        )
        //lock screen icon for pre lollipop
        metadataBuilder.putBitmap(
            MediaMetadataCompat.METADATA_KEY_ART,
            BitmapFactory.decodeResource(resources, R.drawable.launcher_foreground)
        )

        metadataBuilder.putText(
            MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, getString(R.string.quran_radio)
        )
        metadataBuilder.putText(MediaMetadataCompat.METADATA_KEY_TITLE, getString(R.string.quran_radio))

        mediaSession.setMetadata(metadataBuilder.build())
    }

    private fun updatePbState(state: Int, position: Int) {
        val stateBuilder: PlaybackStateCompat.Builder = PlaybackStateCompat.Builder()

        stateBuilder.setState(state, position.toLong(), 1F)
            .setActions(PlaybackStateCompat.ACTION_SEEK_TO)

        mediaSession.setPlaybackState(stateBuilder.build())
    }

    private fun updateNotification(playing: Boolean) {
        playPauseAction =
            if (playing) {
                NotificationCompat.Action(
                    R.drawable.ic_baseline_pause, "play_pause", PendingIntent.getBroadcast(
                        this, id, Intent(ACTION_PLAY_PAUSE).setPackage(packageName), flags
                    )
                )
            }
            else {
                NotificationCompat.Action(
                    R.drawable.ic_play_arrow, "play_pause", PendingIntent.getBroadcast(
                        this, id, Intent(ACTION_PLAY_PAUSE).setPackage(packageName), flags
                    )
                )
            }
        notificationBuilder.clearActions().addAction(playPauseAction)

        notification = notificationBuilder.build()
        notificationManager.notify(id, notification)
    }

    private fun initPlayer() {
        player = MediaPlayer()
        player.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)

        wifiLock =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                (applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
                    .createWifiLock(WifiManager.WIFI_MODE_FULL_LOW_LATENCY, "myLock")
            else
                (applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
                    .createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "myLock")

        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )

        am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val attrs = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(this)
                .setAudioAttributes(attrs)
                .build()
        }

        player.setOnPreparedListener {
            player.start()
            updatePbState(PlaybackStateCompat.STATE_PLAYING, player.currentPosition)
            updateNotification(true)
        }

        player.setOnInfoListener { _, what, _ ->
            when (what) {
                MediaPlayer.MEDIA_INFO_BUFFERING_START ->
                    updatePbState(PlaybackStateCompat.STATE_BUFFERING, player.currentPosition)
                MediaPlayer.MEDIA_INFO_BUFFERING_END ->
                    updatePbState(PlaybackStateCompat.STATE_PLAYING, player.currentPosition)
            }
            false
        }

        player.setOnErrorListener { _: MediaPlayer?, what: Int, _: Int ->
            Log.e(Global.TAG, "Error in RadioService player: $what")
            true
        }
    }

    private fun setActions() {
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        intentFilter.addAction(ACTION_PLAY_PAUSE)
        intentFilter.addAction(ACTION_STOP)

        val pkg = packageName
        flags = PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE

        playPauseAction = NotificationCompat.Action(
            R.drawable.ic_play_arrow, "play_pause", PendingIntent.getBroadcast(
                this, id, Intent(ACTION_PLAY_PAUSE).setPackage(pkg), flags
            )
        )
    }

    private fun startPlaying() {
        player.reset()
        player.setDataSource(applicationContext, Uri.parse(dynamicUrl))
        player.prepareAsync()
    }

    // Other Links:
    // https://www.aloula.sa/83c0bda5-18e7-4c80-9c0a-21e764537d47
    // https://m.live.net.sa:1935/live/quransa/playlist.m3u8

    private val thread = Thread {
        updatePbState(PlaybackStateCompat.STATE_BUFFERING, 0)

        try {    // A mechanism to handle redirects and get the final dynamic link
            val url = URL(staticUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = false
            val secondURL = URL(connection.getHeaderField("Location"))
            dynamicUrl = secondURL.toString().replaceFirst("http:".toRegex(), "https:")
            Log.i(Global.TAG, "Dynamic Quran Radio URL: ${this.dynamicUrl}")

            updatePbState(PlaybackStateCompat.STATE_PAUSED, 0)
        } catch (e: IOException) {
            Log.e(Global.TAG, "Problem in RadioService player")
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val description = "quran radio"
            channelId = "QuranRadio"
            val name = getString(R.string.quran_radio)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(channelId, name, importance)
            notificationChannel.description = description
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun stopForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) stopForeground(STOP_FOREGROUND_DETACH)
        else stopForeground(false)
    }

    private fun getContentIntent(): PendingIntent {
        val intent = Intent(this, RadioClient::class.java).setAction("back")

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        return PendingIntent.getActivity(this, 37, intent, flags)
    }

    override fun onGetRoot(
        clientPackageName: String, clientUid: Int, rootHints: Bundle?
    ): BrowserRoot {
        // (Optional) Control the level of access for the specified package name.
        return if (allowBrowsing(clientPackageName, clientUid)) {
            // Returns a root ID that clients can use with onLoadChildren() to retrieve
            // the content hierarchy.
            BrowserRoot(MY_MEDIA_ROOT_ID, null)
        } else {
            // Clients can connect, but this BrowserRoot is an empty hierarchy
            // so onLoadChildren returns nothing. This disables the ability to browse for content.
            BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, null)
        }
    }

    override fun onLoadChildren(
        parentId: String, result: Result<List<MediaBrowserCompat.MediaItem?>?>
    ) {
        /*//  Browsing not allowed
        if (TextUtils.equals(MY_EMPTY_MEDIA_ROOT_ID, parentMediaId)) {
            result.sendResult(null);
            return;
        }
        // Assume for example that the music catalog is already loaded/cached.
        List<MediaItem> mediaItems = new ArrayList<>();
        // Check if this is the root menu:
        if (MY_MEDIA_ROOT_ID.equals(parentMediaId)) {
            // Build the MediaItem objects for the top level,
            // and put them in the mediaItems list...
        } else {
            // Examine the passed parentMediaId to see which submenu we're at,
            // and put the children of that menu in the mediaItems list...
        }
        result.sendResult(mediaItems);*/
    }

    private fun allowBrowsing(clientPackageName: String, clientUid: Int): Boolean {
        return true
    }

}