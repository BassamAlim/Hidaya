package bassamalim.hidaya.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer
import android.net.Uri
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.WifiLock
import android.os.*
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import androidx.preference.PreferenceManager
import androidx.room.Room
import bassamalim.hidaya.R
import bassamalim.hidaya.activities.TelawatClient
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.models.Reciter
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.other.Utils
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*

@RequiresApi(api = Build.VERSION_CODES.O)
class TelawatService : MediaBrowserServiceCompat(), OnAudioFocusChangeListener {

    private lateinit var playAction: NotificationCompat.Action
    private lateinit var pauseAction: NotificationCompat.Action
    private lateinit var nextAction: NotificationCompat.Action
    private lateinit var prevAction: NotificationCompat.Action
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private lateinit var controller: MediaControllerCompat
    private lateinit var mediaMetadata: MediaMetadataCompat
    private var channelId = "channel ID"
    private val id = 333
    private lateinit var pref: SharedPreferences
    private lateinit var db: AppDatabase
    private val handler: Handler = Handler(Looper.getMainLooper())
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notification: Notification
    private lateinit var player: MediaPlayer
    private lateinit var am: AudioManager
    private val intentFilter: IntentFilter = IntentFilter()
    private lateinit var audioFocusRequest: AudioFocusRequest
    private lateinit var wifiLock: WifiLock
    private lateinit var surahNames: List<String>
    private  var mediaId: String? = null
    private lateinit var reciterName: String
    private lateinit var playType: String
    private var reciterId = 0
    private var versionId = 0
    private var surahIndex = 0
    private lateinit var version: Reciter.RecitationVersion
    private var shuffle = 0
    private var continueFrom = 0

    companion object {
        private const val MY_MEDIA_ROOT_ID = "media_root_id"
        private const val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"
        private const val ACTION_PLAY = "bassamalim.hidaya.services.TelawatService.play"
        private const val ACTION_PAUSE = "bassamalim.hidaya.services.TelawatService.pause"
        private const val ACTION_NEXT = "bassamalim.hidaya.services.TelawatService.next"
        private const val ACTION_PREV = "bassamalim.hidaya.services.TelawatService.prev"
        private const val ACTION_STOP = "bassamalim.hidaya.services.TelawatService.stop"
    }

    override fun onCreate() {
        super.onCreate()

        Utils.onActivityCreateSetLocale(this)

        pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "HidayaDB")
            .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build()

        getSuraNames()

        initSession()

        setupActions()

        initMediaSessionMetadata()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    val callback: MediaSessionCompat.Callback = object : MediaSessionCompat.Callback() {
        override fun onPlayFromMediaId(givenMediaId: String, extras: Bundle) {
            Log.d(Global.TAG, "In onPlayFromMediaId of TelawatService")

            playType = extras.getString("play_type")!!
            if (givenMediaId != mediaId || playType == "continue") {
                Log.d(Global.TAG, "Old mediaID: $mediaId, New mediaId: $givenMediaId")

                mediaId = givenMediaId

                mediaSession.setSessionActivity(getContentIntent())

                reciterId = givenMediaId.substring(0, 3).toInt()
                versionId = givenMediaId.substring(3, 5).toInt()
                surahIndex = givenMediaId.substring(5).toInt()
                reciterName = extras.getString("reciter_name")!!
                version = extras.getSerializable("version") as Reciter.RecitationVersion

                if (playType == "continue") continueFrom = pref.getInt("last_telawa_progress", 0)

                buildNotification()
                initPlayer()

                if (controller.playbackState.state == PlaybackStateCompat.STATE_NONE) onPlay()
                else playOther()
            }
        }

        override fun onPlay() {
            Log.d(Global.TAG, "In onPlay of TelawatService")

            // Request audio focus for playback, this registers the afChangeListener
            val result: Int = am.requestAudioFocus(audioFocusRequest)

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // Start the service
                startService(Intent(applicationContext, TelawatService::class.java))
                // Set the session active  (and update metadata and state)
                mediaSession.isActive = true

                // start the player (custom call)
                if (controller.playbackState.state == PlaybackStateCompat.STATE_PAUSED
                    || controller.playbackState.state == PlaybackStateCompat.STATE_STOPPED)
                    player.start()
                else
                    startPlaying(surahIndex)

                updatePbState(PlaybackStateCompat.STATE_PLAYING)
                updateNotification(true)
                refresh()

                // Register Receiver
                registerReceiver(receiver, intentFilter)
                // Put the service in the foreground, post notification
                startForeground(id, notification)
            }
        }

        override fun onStop() {
            Log.d(Global.TAG, "In onStop of TelawatService")

            // Abandon audio focus
            am.abandonAudioFocusRequest(audioFocusRequest)
            cleanUp()

            unregisterReceiver(receiver)
            handler.removeCallbacks(runnable)
            saveForLater(player.currentPosition)
            // stop the player (custom call)
            player.stop()
            // Stop the service
            stopSelf()
            // Set the session inactive  (and update metadata and state)
            mediaSession.isActive = false
            updatePbState(PlaybackStateCompat.STATE_STOPPED)

            // Take the service out of the foreground
            stopForeground(false)
        }

        override fun onPause() {
            Log.d(Global.TAG, "In onPause of TelawatService")

            // Update metadata and state
            updatePbState(PlaybackStateCompat.STATE_PAUSED)
            updateNotification(false)

            saveForLater(player.currentPosition)

            // pause the player
            player.pause()
            handler.removeCallbacks(runnable)

            // Take the service out of the foreground, retain the notification
            stopForeground(false)
        }

        override fun onFastForward() {
            super.onFastForward()
            player.seekTo(player.currentPosition + 10000)
            updatePbState(controller.playbackState.state)
        }

        override fun onRewind() {
            super.onRewind()
            player.seekTo(player.currentPosition - 10000)
            updatePbState(controller.playbackState.state)
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            skipToNext()
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            skipToPrevious()
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            player.seekTo(pos.toInt())
            updatePbState(controller.playbackState.state)
        }

        override fun onSetRepeatMode(repeatMode: Int) {
            super.onSetRepeatMode(repeatMode)
            player.isLooping = repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE
        }

        override fun onSetShuffleMode(shuffleMode: Int) {
            super.onSetShuffleMode(shuffleMode)
            shuffle = shuffleMode
        }
    }

    private fun playOther() {
        mediaSession.isActive = true
        // start the player
        startPlaying(surahIndex)

        // Update state
        updatePbState(PlaybackStateCompat.STATE_PLAYING)
        updateNotification(true)
    }

    private fun skipToNext() {
        var temp = surahIndex
        if (shuffle == PlaybackStateCompat.SHUFFLE_MODE_NONE) {
            do {
                temp++
            } while (temp < 114 && !version.getSuras().contains("," + (temp + 1) + ","))
        }
        else if (shuffle == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
            val random = Random()
            do {
                temp = random.nextInt(114)
            } while (!version.getSuras().contains("," + (temp + 1) + ","))
        }

        if (temp < 114) {
            surahIndex = temp
            updateMetadata(false)
            updateNotification(true)
            startPlaying(surahIndex)
            updatePbState(PlaybackStateCompat.STATE_PLAYING)
        }
    }

    private fun skipToPrevious() {
        var temp = surahIndex
        if (shuffle == PlaybackStateCompat.SHUFFLE_MODE_NONE) {
            do {
                temp--
            } while (temp >= 0 && !version.getSuras().contains("," + (temp + 1) + ","))
        } else if (shuffle == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
            val random = Random()
            do {
                temp = random.nextInt(114)
            } while (!version.getSuras().contains("," + (temp + 1) + ","))
        }

        if (temp >= 0) {
            surahIndex = temp
            updateMetadata(false)
            updateNotification(true)
            startPlaying(surahIndex)
            updatePbState(PlaybackStateCompat.STATE_PLAYING)
        }
    }

    override fun onAudioFocusChange(i: Int) {
        when (i) {
            AudioManager.AUDIOFOCUS_GAIN ->
                player.setVolume(1.0f, 1.0f)
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
                    Log.d(Global.TAG, "In ACTION_BECOMING_NOISY")
                    callback.onPause()
                }
                ACTION_PLAY -> {
                    Log.d(Global.TAG, "In ACTION_PLAY")
                    callback.onPlay()
                }
                ACTION_PAUSE -> {
                    Log.d(Global.TAG, "In ACTION_PAUSE")
                    callback.onPause()
                }
                ACTION_NEXT -> {
                    Log.d(Global.TAG, "In ACTION_NEXT")
                    skipToNext()
                }
                ACTION_PREV -> {
                    Log.d(Global.TAG, "In ACTION_PREV")
                    skipToPrevious()
                }
                ACTION_STOP -> {
                    Log.d(Global.TAG, "In ACTION_STOP")
                    callback.onStop()
                }
            }
        }
    }

    private fun initSession() {
        // Create a MediaSessionCompat
        mediaSession = MediaSessionCompat(this, "TelawatService")

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
        stateBuilder = PlaybackStateCompat.Builder().setActions(
            PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE
        )
        mediaSession.setPlaybackState(stateBuilder.build())

        // callback() has methods that handle callbacks from a media controller
        mediaSession.setCallback(callback)

        // Set the session's token so that client activities can communicate with it.
        sessionToken = mediaSession.sessionToken
    }

    private fun setupActions() {
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        intentFilter.addAction(ACTION_PLAY)
        intentFilter.addAction(ACTION_PAUSE)
        intentFilter.addAction(ACTION_NEXT)
        intentFilter.addAction(ACTION_PREV)
        intentFilter.addAction(ACTION_STOP)

        val pkg: String = packageName
        val flags: Int = PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE

        playAction = NotificationCompat.Action(
            R.drawable.ic_play_arrow, "Play", PendingIntent.getBroadcast(
                this, id, Intent(ACTION_PLAY).setPackage(pkg), flags)
        )

        pauseAction = NotificationCompat.Action(
            R.drawable.ic_baseline_pause, "Pause", PendingIntent.getBroadcast(
                this, id, Intent(ACTION_PAUSE).setPackage(pkg), flags
            )
        )

        nextAction = NotificationCompat.Action(
            R.drawable.ic_skip_next, "next", PendingIntent.getBroadcast(
                this, id, Intent(ACTION_NEXT).setPackage(pkg), flags
            )
        )

        prevAction = NotificationCompat.Action(
            R.drawable.ic_skip_previous, "previous", PendingIntent.getBroadcast(
                this, id, Intent(ACTION_PREV).setPackage(pkg), flags
            )
        )
    }

    private fun buildNotification() {
        // Get the session's metadata
        controller = mediaSession.controller
        mediaMetadata = controller.metadata
        val description: MediaDescriptionCompat = mediaMetadata.description

        mediaSession.setSessionActivity(getContentIntent())

        createNotificationChannel()

        // Create a NotificationCompat.Builder
        notificationBuilder = NotificationCompat.Builder(this, channelId)

        notificationBuilder
            // Add the metadata for the currently playing track
            .setContentTitle(description.title)
            .setContentText(description.subtitle)
            .setSubText(description.description)
            .setLargeIcon(description.iconBitmap)
            // Enable launching the player by clicking the notification
            .setContentIntent(controller.sessionActivity)
            // Stop the service when the notification is swiped away
            .setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this, PlaybackStateCompat.ACTION_STOP
                )
            )
            // Make the transport controls visible on the lockscreen
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            // Add an app icon and set its accent color (Be careful about the color)
            .setSmallIcon(R.drawable.launcher_foreground)
            .setColor(ContextCompat.getColor(this, R.color.surface_M))
            // Add buttons
            .addAction(prevAction).addAction(pauseAction)
            .addAction(nextAction)
            // So there will be no notification tone
            .setSilent(true)
            // So the user wouldn't swipe it off
            .setOngoing(true)
            // Take advantage of MediaStyle features
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
                    // Add a cancel button
                    .setShowCancelButton(true)
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
        metadataBuilder.putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, "")
        metadataBuilder.putText(MediaMetadataCompat.METADATA_KEY_TITLE, "")
        metadataBuilder.putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, "")
        metadataBuilder.putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, "")

        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, 0)
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, 0)

        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)

        mediaSession.setMetadata(metadataBuilder.build())
    }

    private fun updateMetadata(duration: Boolean) {
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

        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)

        metadataBuilder.putString(
            MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, surahNames[surahIndex]
        )
        metadataBuilder.putString(
            MediaMetadataCompat.METADATA_KEY_TITLE, surahNames[surahIndex]
        )
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, reciterName)
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, reciterName)
        metadataBuilder.putString(
            MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, version.getRewaya()
        )

        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, surahIndex.toLong())
        metadataBuilder.putLong(
            MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, version.getCount().toLong()
        )
        metadataBuilder.putLong(
            MediaMetadataCompat.METADATA_KEY_DURATION,
            (if (duration) player.duration else 0).toLong()
        )

        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)

        mediaMetadata = metadataBuilder.build()
        mediaSession.setMetadata(mediaMetadata)
    }

    private fun updatePbState(state: Int) {
        stateBuilder.setState(state, player.currentPosition.toLong(), 1F)
            .setActions(PlaybackStateCompat.ACTION_SEEK_TO)

        mediaSession.setPlaybackState(stateBuilder.build())
    }

    private val runnable = Runnable {
        if (controller.playbackState.state == PlaybackStateCompat.STATE_PLAYING) refresh()
    }

    private fun refresh() {
        updatePbState(controller.playbackState.state)
        handler.postDelayed(runnable, 1000)
    }

    private fun updateNotification(playing: Boolean) {
        if (playing)
            notificationBuilder.clearActions()
            .addAction(prevAction).addAction(pauseAction).addAction(nextAction)
        else
            notificationBuilder.clearActions()
            .addAction(prevAction).addAction(playAction).addAction(nextAction)

        notification = notificationBuilder.build()
        notificationManager.notify(id, notification)
    }

    private fun initPlayer() {
        player = MediaPlayer()
        player.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)

        wifiLock = (applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .createWifiLock(WifiManager.WIFI_MODE_FULL, "myLock")
        wifiLock.acquire()

        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )
        am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val attrs: AudioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setOnAudioFocusChangeListener(this)
            .setAudioAttributes(attrs)
            .build()

        player.setOnPreparedListener {
            if (playType == "continue") player.seekTo(continueFrom)

            player.start()

            updateMetadata(true) // For the duration
            updatePbState(PlaybackStateCompat.STATE_PLAYING)
            updateNotification(true)
        }
        player.setOnCompletionListener { skipToNext() }
        player.setOnErrorListener { _, what, _ ->
            Log.e(Global.TAG, "Error in TelawatService player: $what")
            true
        }
    }

    private fun startPlaying(surah: Int) {
        player.reset()

        if (tryOffline(surah)) return

        val text = String.format(Locale.US, "%s/%03d.mp3", version.getServer(), surah + 1)

        try {
            player.setDataSource(applicationContext, Uri.parse(text))
            player.prepareAsync()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(Global.TAG, "Problem in TelawatService player")
        }
    }

    private fun tryOffline(surah: Int): Boolean {
        val path: String = (getExternalFilesDir(null).toString() + "/Telawat/" + reciterId
                + "/" + version.getVersionId() + "/" + surah + ".mp3")

        return try {
            player.setDataSource(path)
            player.prepare()
            Log.i(Global.TAG, "Playing Offline")
            true
        } catch (f: FileNotFoundException) {
            Log.i(Global.TAG, "Not available offline")
            false
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(Global.TAG, "Problem in TelawatService player")
            false
        }
    }

    private fun createNotificationChannel() {
        val name: CharSequence
        val description = "quran listening"
        channelId = "Telawat"
        name = getString(R.string.recitations)
        val importance: Int = NotificationManager.IMPORTANCE_DEFAULT
        val notificationChannel = NotificationChannel(channelId, name, importance)
        notificationChannel.description = description
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun getSuraNames() {
        surahNames = db.suarDao().getNames()
    }

    private fun getContentIntent(): PendingIntent {
        val intent: Intent = Intent(this, TelawatClient::class.java).setAction("back")
            .putExtra("media_id", mediaId)

        val flags: Int = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        return PendingIntent.getActivity(this, 36, intent, flags)
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

    private fun saveForLater(progress: Int) {
        val text: String =
            getString(R.string.sura) + " " + surahNames[surahIndex] + " " +
                    getString(R.string.for_reciter) + " " + reciterName + " " +
                    getString(R.string.in_rewaya_of) + " " + version.getRewaya()

        val editor: SharedPreferences.Editor = pref.edit()
        editor.putString("last_played_media_id", mediaId)
        editor.putString("last_played_text", text)
        editor.putInt("last_telawa_progress", progress)
        editor.apply()
    }

    private fun cleanUp() {
        wifiLock.release()
        player.release()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(Global.TAG, "In onUnbind of TelawatService")
        saveForLater(player.currentPosition)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanUp()
    }

}