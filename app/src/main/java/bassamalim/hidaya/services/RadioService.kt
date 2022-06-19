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
import bassamalim.hidaya.other.Utils
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class RadioService : MediaBrowserServiceCompat(), OnAudioFocusChangeListener {
    private var playPauseAction: NotificationCompat.Action? = null
    private var mediaSession: MediaSessionCompat? = null
    private var controller: MediaControllerCompat? = null
    private var channelId = "channel ID"
    private val id = 444
    private var flags = 0
    private var notificationManager: NotificationManager? = null
    private var notificationBuilder: NotificationCompat.Builder? = null
    private var notification: Notification? = null
    private var player: MediaPlayer? = null
    private var am: AudioManager? = null
    private val intentFilter: IntentFilter = IntentFilter()
    private var audioFocusRequest: AudioFocusRequest? = null
    private var wifiLock: WifiLock? = null
    private var link: String? = null
    override fun onCreate() {
        super.onCreate()
        Utils.onActivityCreateSetLocale(this)
        initSession()
        setActions()
        initMediaSessionMetadata()
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    val callback: MediaSessionCompat.Callback = object : MediaSessionCompat.Callback() {
        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            Log.d(Global.TAG, "In onPlayFromMediaId of RadioClient")
            super.onPlayFromMediaId(mediaId, extras)
            link = mediaId
            buildNotification()
            initPlayer()
            play()
        }

        override fun onStop() {
            Log.d(Global.TAG, "In onStop of RadioClient")
            super.onStop()
            stop()
        }

        override fun onPause() {
            super.onPause()
            Log.d(Global.TAG, "In onPause of RadioClient")
            pause()
        }
    }

    private fun play() {
        // Request audio focus for playback, this registers the afChangeListener
        var result: Int = AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) result =
            am!!.requestAudioFocus(audioFocusRequest!!)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // Start the service
            startService(Intent(applicationContext, RadioService::class.java))
            // Set the session active  (and update metadata and state)
            mediaSession!!.isActive = true

            // start the player (custom call)
            if (controller!!.playbackState.state == PlaybackStateCompat.STATE_PAUSED ||
                controller!!.playbackState.state == PlaybackStateCompat.STATE_STOPPED
            ) player!!.start() else startPlaying()
            updatePbState(PlaybackStateCompat.STATE_PLAYING)
            updateNotification(true)

            // Register Receiver
            registerReceiver(receiver, intentFilter)
            // Put the service in the foreground, post notification
            startForeground(id, notification)
        }
    }

    private fun pause() {
        Log.d(Global.TAG, "in pause of RadioService")
        // Update metadata and state
        // pause the player (custom call)
        mediaSession!!.isActive = false
        player!!.pause()
        updatePbState(PlaybackStateCompat.STATE_PAUSED)
        updateNotification(false)

        // Take the service out of the foreground, retain the notification
        stopForeground(false)
    }

    private fun stop() {
        Log.d(Global.TAG, "in stop of RadioService")
        // Abandon audio focus
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) am!!.abandonAudioFocusRequest(
            audioFocusRequest!!
        )
        cleanUp()
        unregisterReceiver(receiver)
        // stop the player (custom call)
        player!!.stop()
        // Stop the service
        stopSelf()
        // Set the session inactive  (and update metadata and state)
        mediaSession!!.isActive = false
        updatePbState(PlaybackStateCompat.STATE_STOPPED)

        // Take the service out of the foreground
        stopForeground(false)
        onDestroy()
    }

    override fun onAudioFocusChange(i: Int) {
        when (i) {
            AudioManager.AUDIOFOCUS_GAIN -> if (player != null) player!!.setVolume(1.0f, 1.0f)
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> if (player != null && player!!.isPlaying) player!!.setVolume(
                0.3f,
                0.3f
            )
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT, AudioManager.AUDIOFOCUS_LOSS -> if (player != null && player!!.isPlaying) pause()
        }
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                AudioManager.ACTION_AUDIO_BECOMING_NOISY -> {
                    Log.d(Global.TAG, "In ACTION_BECOMING_NOISY of RadioService")
                    pause()
                }
                ACTION_PLAY_PAUSE -> if (controller!!.playbackState.state
                    == PlaybackStateCompat.STATE_PLAYING
                ) {
                    Log.d(Global.TAG, "In ACTION_PAUSE of RadioService")
                    pause()
                } else if (controller!!.playbackState.state ==
                    PlaybackStateCompat.STATE_PAUSED
                ) {
                    Log.d(Global.TAG, "In ACTION_PLAY of RadioService")
                    play()
                }
                ACTION_STOP -> {
                    Log.d(Global.TAG, "In ACTION_STOP of RadioService")
                    stop()
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
        mediaSession!!.setPlaybackState(stateBuilder.build())

        // callback() has methods that handle callbacks from a media controller
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) mediaSession!!.setCallback(callback)

        // Set the session's token so that client activities can communicate with it.
        setSessionToken(mediaSession!!.sessionToken)
    }

    private fun buildNotification() {
        // Given a media session and its context (usually the component containing the session)

        // Get the session's metadata
        controller = mediaSession!!.controller
        val mediaMetadata: MediaMetadataCompat = controller!!.metadata
        val description: MediaDescriptionCompat = mediaMetadata.description
        mediaSession!!.setSessionActivity(getContentIntent())
        createNotificationChannel()

        // Create a NotificationCompat.Builder
        notificationBuilder = NotificationCompat.Builder(this, channelId)
        notificationBuilder!! // Add the metadata for the currently playing track
            .setContentTitle(description.title) // Stop the service when the notification is swiped away
            .setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this,
                    PlaybackStateCompat.ACTION_STOP
                )
            ) // Make the transport controls visible on the lockscreen
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Add an app icon and set its accent color
            // Be careful about the color
            .setSmallIcon(R.drawable.launcher_foreground)
            .setColorized(true)
            .setColor(resources.getColor(R.color.surface_M, theme)) // Add buttons
            // Enable launching the player by clicking the notification
            .setContentIntent(controller!!.sessionActivity)
            .addAction(playPauseAction) // So there will be no notification tone
            .setSilent(true) // So the user wouldn't swipe it off
            .setOngoing(true) // Take advantage of MediaStyle features
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession!!.sessionToken)
                    .setShowActionsInCompactView(0) // Add a cancel button
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            this, PlaybackStateCompat.ACTION_STOP
                        )
                    )
            )
        notification = notificationBuilder!!.build()

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
            MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE,
            getString(R.string.quran_radio)
        )
        metadataBuilder.putText(
            MediaMetadataCompat.METADATA_KEY_TITLE,
            getString(R.string.quran_radio)
        )
        mediaSession!!.setMetadata(metadataBuilder.build())
    }

    private fun updatePbState(state: Int) {
        val stateBuilder: PlaybackStateCompat.Builder = PlaybackStateCompat.Builder()
        stateBuilder.setState(state, player!!.currentPosition.toLong(), 1F)
            .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
        mediaSession!!.setPlaybackState(stateBuilder.build())
    }

    private fun updateNotification(playing: Boolean) {
        playPauseAction = if (playing) {
            NotificationCompat.Action(
                R.drawable.ic_baseline_pause,
                "play_pause", PendingIntent.getBroadcast(
                    this, id,
                    Intent(ACTION_PLAY_PAUSE).setPackage(packageName), flags
                )
            )
        } else {
            NotificationCompat.Action(
                R.drawable.ic_play_arrow,
                "play_pause", PendingIntent.getBroadcast(
                    this, id,
                    Intent(ACTION_PLAY_PAUSE).setPackage(packageName), flags
                )
            )
        }
        notificationBuilder!!.clearActions().addAction(playPauseAction)
        notification = notificationBuilder!!.build()
        notificationManager!!.notify(id, notification)
    }

    private fun initPlayer() {
        player = MediaPlayer()
        player!!.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        wifiLock = (applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .createWifiLock(WifiManager.WIFI_MODE_FULL, "myLock")
        wifiLock!!.acquire()
        player!!.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )
        am = getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        val attrs: AudioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(this)
                .setAudioAttributes(attrs)
                .build()
        }
        player!!.setOnPreparedListener {
            player!!.start()
            updatePbState(PlaybackStateCompat.STATE_PLAYING)
            updateNotification(true)
        }
        player!!.setOnCompletionListener { stop() }
        player!!.setOnErrorListener { _: MediaPlayer?, what: Int, _: Int ->
            Log.e(Global.TAG, "Error in RadioService player: $what")
            true
        }
    }

    private fun setActions() {
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        intentFilter.addAction(ACTION_PLAY_PAUSE)
        intentFilter.addAction(ACTION_STOP)
        val pkg: String = packageName
        flags = PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        playPauseAction = NotificationCompat.Action(
            R.drawable.ic_play_arrow, "play_pause",
            PendingIntent.getBroadcast(
                this, id,
                Intent(ACTION_PLAY_PAUSE).setPackage(pkg), flags
            )
        )
    }

    private fun startPlaying() {
        player!!.reset()
        thread.start()
    }

    // Other Links:
    // https://www.aloula.sa/83c0bda5-18e7-4c80-9c0a-21e764537d47
    // https://m.live.net.sa:1935/live/quransa/playlist.m3u8
    private var thread = Thread {
        try {    // A mechanism to handle redirects and get the final dynamic link
            Log.d(Global.TAG, "There")
            val url = URL(link)
            val connection = url.openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = false
            val secondURL = URL(connection.getHeaderField("Location"))
            link = secondURL.toString()
            link = link!!.replaceFirst("http:".toRegex(), "https:")
            Log.i(Global.TAG, "Dynamic Quran Radio URL: $link")
            player!!.setDataSource(applicationContext, Uri.parse(link))
            player!!.prepareAsync()
        } catch (e: IOException) {
            Log.e(Global.TAG, "Problem in RadioService player")
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence
            val description = "quran radio"
            channelId = "QuranRadio"
            name = getString(R.string.quran_radio)
            val importance: Int = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(
                channelId, name, importance
            )
            notificationChannel.description = description
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(notificationChannel)
        }
    }

    private fun getContentIntent(): PendingIntent {
        val intent: Intent = Intent(this, RadioClient::class.java).setAction("back")
        val flags: Int = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(this, 37, intent, flags)
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int, rootHints: Bundle?
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
        parentId: String,
        result: Result<List<MediaBrowserCompat.MediaItem?>?>
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

    private fun cleanUp() {
        if (wifiLock != null) wifiLock!!.release()
        if (player != null) {
            player!!.release()
            player = null
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(Global.TAG, "In onUnbind of RadioService")
        return super.onUnbind(intent)
    }

    companion object {
        private const val MY_MEDIA_ROOT_ID = "media_root_id"
        private const val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"
        private const val ACTION_PLAY_PAUSE = "bassamalim.hidaya.services.RadioService.playpause"
        private const val ACTION_STOP = "bassamalim.hidaya.services.RadioService.stop"
    }
}