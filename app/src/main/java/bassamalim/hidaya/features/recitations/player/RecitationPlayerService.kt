package bassamalim.hidaya.features.recitations.player

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
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.support.v4.media.MediaBrowserCompat
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
import androidx.media3.common.util.UnstableApi
import bassamalim.hidaya.R
import bassamalim.hidaya.core.Activity
import bassamalim.hidaya.core.data.dataSources.room.AppDatabase
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.QuranRepository
import bassamalim.hidaya.core.data.repositories.RecitationsRepository
import bassamalim.hidaya.core.data.repositories.UserRepository
import bassamalim.hidaya.core.helpers.ReceiverManager
import bassamalim.hidaya.core.models.Recitation
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.utils.ActivityUtils
import bassamalim.hidaya.features.recitations.recitersMenu.domain.LastPlayedMedia
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.io.IOException
import java.util.Locale
import java.util.Random
import javax.inject.Inject

@AndroidEntryPoint
@UnstableApi
@RequiresApi(api = Build.VERSION_CODES.O)
class RecitationPlayerService : MediaBrowserServiceCompat(), OnAudioFocusChangeListener {

    @Inject lateinit var quranRepository: QuranRepository
    @Inject lateinit var recitationsRepository: RecitationsRepository
    @Inject lateinit var appSettingsRepository: AppSettingsRepository
    @Inject lateinit var userRepository: UserRepository
    private val notificationId = 333
    private val handler: Handler = Handler(Looper.getMainLooper())
    private val intentFilter: IntentFilter = IntentFilter()
    private lateinit var db: AppDatabase
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notification: Notification
    private lateinit var player: MediaPlayer
    private lateinit var am: AudioManager
    private lateinit var audioFocusRequest: AudioFocusRequest
    private lateinit var wifiLock: WifiLock
    private lateinit var suraNames: List<String>
    private lateinit var playAction: NotificationCompat.Action
    private lateinit var pauseAction: NotificationCompat.Action
    private lateinit var nextAction: NotificationCompat.Action
    private lateinit var prevAction: NotificationCompat.Action
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private lateinit var controller: MediaControllerCompat
    private lateinit var mediaMetadata: MediaMetadataCompat
    private lateinit var playType: String
    private lateinit var narration: Recitation.Narration
    private var channelId = "channel ID"
    private var mediaId: String? = null
    private var reciterName: String? = null
    private var reciterId = 0
    private var versionId = 0
    private var suraIndex = 0
    private var shuffle = 0
    private var continueFrom = 0
    private var updateRecordCounter = 0

    companion object {
        private const val MY_MEDIA_ROOT_ID = "media_root_id"
        private const val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"
        private const val ACTION_PLAY =
            "bassamalim.hidaya.features.recitationsPlayer.RecitationsPlayerService.PLAY"
        private const val ACTION_PAUSE =
            "bassamalim.hidaya.features.recitationsPlayer.RecitationsPlayerService.PAUSE"
        private const val ACTION_NEXT =
            "bassamalim.hidaya.features.recitationsPlayer.RecitationsPlayerService.NEXT"
        private const val ACTION_PREV =
            "bassamalim.hidaya.features.recitationsPlayer.RecitationsPlayerService.PREVIOUS"
        private const val ACTION_STOP =
            "bassamalim.hidaya.features.recitationsPlayer.RecitationsPlayerService.STOP"
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                AudioManager.ACTION_AUDIO_BECOMING_NOISY -> {
                    Log.i(Global.TAG, "In ACTION_BECOMING_NOISY")
                    callback.onPause()
                }
                ACTION_PLAY -> {
                    Log.i(Global.TAG, "In ACTION_PLAY")
                    callback.onPlay()
                }
                ACTION_PAUSE -> {
                    Log.i(Global.TAG, "In ACTION_PAUSE")
                    callback.onPause()
                }
                ACTION_NEXT -> {
                    Log.i(Global.TAG, "In ACTION_NEXT")
                    skipToNext()
                }
                ACTION_PREV -> {
                    Log.i(Global.TAG, "In ACTION_PREV")
                    skipToPrevious()
                }
                ACTION_STOP -> {
                    Log.i(Global.TAG, "In ACTION_STOP")
                    callback.onStop()
                }
            }
        }
    }

    private val receiverManager = ReceiverManager(this, receiver, intentFilter)

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()

        GlobalScope.launch {
            ActivityUtils.onActivityCreateSetLocale(
                context = applicationContext,
                language = appSettingsRepository.getLanguage().first()
            )

            getSuraNames()

            initSession()
            initPlayer()
            setupActions()
            initMetadata()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    @OptIn(DelicateCoroutinesApi::class)
    val callback: MediaSessionCompat.Callback = object : MediaSessionCompat.Callback() {
        override fun onPlayFromMediaId(givenMediaId: String, extras: Bundle) {
            Log.i(Global.TAG, "In onPlayFromMediaId of RecitationsPlayerService")

            playType = extras.getString("play_type")!!
            if (givenMediaId != mediaId || playType == "continue") {
                mediaId = givenMediaId

                reciterId = givenMediaId.substring(0, 3).toInt()
                versionId = givenMediaId.substring(3, 5).toInt()
                suraIndex = givenMediaId.substring(5).toInt()
                reciterName = extras.getString("reciter_name")!!
                narration =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        extras.getSerializable("narration", Recitation.Narration::class.java)!!
                    else
                        extras.getSerializable("narration") as Recitation.Narration

                GlobalScope.launch {
                    if (playType == "continue")
                        continueFrom = recitationsRepository.getLastPlayedMedia()
                            .first().progress.toInt()

                    buildNotification()
                    updateMetadata(false)

                    wifiLock.acquire()

                    if (controller.playbackState.state == PlaybackStateCompat.STATE_NONE) onPlay()
                    else playOther()
                }
            }
        }

        override fun onPlay() {
            Log.i(Global.TAG, "In onPlay of RecitationsPlayerService")

            if (mediaId == null) return  // bandage for an error

            buildNotification()

            // Request audio focus for playback, this registers the afChangeListener
            val result = am.requestAudioFocus(audioFocusRequest)

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // Start the service
                startService(Intent(applicationContext, RecitationPlayerService::class.java))
                // Set the session active  (and update metadata and state)
                mediaSession.isActive = true

                receiverManager.register()
                // Put the service in the foreground, post notification
                startForeground(notificationId, notification)

                // start the player (custom call)
                if (controller.playbackState.state == PlaybackStateCompat.STATE_PAUSED
                    || controller.playbackState.state == PlaybackStateCompat.STATE_STOPPED) {
                    player.start()
                    refresh()
                }
                else startPlaying(suraIndex)

                updatePbState(
                    PlaybackStateCompat.STATE_PLAYING,
                    controller.playbackState.bufferedPosition
                )
                updateNotification(true)
            }
        }

        override fun onPause() {
            Log.i(Global.TAG, "In onPause of RecitationsPlayerService")

            // Update metadata and state
            updatePbState(
                PlaybackStateCompat.STATE_PAUSED,
                controller.playbackState.bufferedPosition
            )
            updateNotification(false)

            GlobalScope.launch {
                saveForLater(player.currentPosition)
                updateDurationRecord(updateRecordCounter)
            }

            handler.removeCallbacks(runnable)
            // pause the player
            player.pause()

            // Take the service out of the foreground, retain the notification
            stopForeground()
        }

        override fun onStop() {
            Log.i(Global.TAG, "In onStop of RecitationsPlayerService")

            updatePbState(
                PlaybackStateCompat.STATE_STOPPED,
                controller.playbackState.bufferedPosition
            )

            handler.removeCallbacks(runnable)
            am.abandonAudioFocusRequest(audioFocusRequest)    // Abandon audio focus
            receiverManager.unregister()
            if (wifiLock.isHeld) wifiLock.release()

            GlobalScope.launch {
                saveForLater(player.currentPosition)
                updateDurationRecord(updateRecordCounter)
            }

            player.release()

            stopSelf()    // Stop the service
            mediaSession.isActive = false    // Set the session inactive
            stopForeground()    // Take the service out of the foreground
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
            updatePbState(
                controller.playbackState.state,
                controller.playbackState.bufferedPosition
            )
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
        startPlaying(suraIndex)

        // Update state
        updatePbState(
            PlaybackStateCompat.STATE_PLAYING,
            controller.playbackState.bufferedPosition
        )
        updateNotification(true)
    }

    private fun skipToNext() {
        var temp = suraIndex
        if (shuffle == PlaybackStateCompat.SHUFFLE_MODE_NONE) {
            do {
                temp++
            } while (temp < Global.NUM_OF_QURAN_SURAS &&
                !narration.availableSuras.contains("," + (temp + 1) + ","))
        }
        else if (shuffle == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
            val random = Random()
            do {
                temp = random.nextInt(Global.NUM_OF_QURAN_SURAS)
            } while (!narration.availableSuras.contains("," + (temp + 1) + ","))
        }

        if (temp < Global.NUM_OF_QURAN_SURAS) {
            suraIndex = temp
            updateMetadata(false)
            updateNotification(true)
            startPlaying(suraIndex)
            updatePbState(
                PlaybackStateCompat.STATE_PLAYING,
                controller.playbackState.bufferedPosition
            )
        }
    }

    private fun skipToPrevious() {
        var temp = suraIndex
        if (shuffle == PlaybackStateCompat.SHUFFLE_MODE_NONE) {
            do {
                temp--
            } while (temp >= 0 && !narration.availableSuras.contains("," + (temp + 1) + ","))
        }
        else if (shuffle == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
            val random = Random()
            do {
                temp = random.nextInt(Global.NUM_OF_QURAN_SURAS)
            } while (!narration.availableSuras.contains("," + (temp + 1) + ","))
        }

        if (temp >= 0) {
            suraIndex = temp
            updateMetadata(false)
            updateNotification(true)
            startPlaying(suraIndex)
            updatePbState(
                PlaybackStateCompat.STATE_PLAYING,
                controller.playbackState.bufferedPosition
            )
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

    private fun initSession() {
        // Create a MediaSessionCompat
        mediaSession = MediaSessionCompat(this, "RecitationsPlayerService")

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

        val flags = PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE

        playAction = NotificationCompat.Action(
            R.drawable.ic_play,
            "Play",
            PendingIntent.getBroadcast(
                this,
                notificationId,
                Intent(ACTION_PLAY).setPackage(packageName),
                flags
            )
        )

        pauseAction = NotificationCompat.Action(
            R.drawable.ic_pause,
            "Pause",
            PendingIntent.getBroadcast(
                this,
                notificationId,
                Intent(ACTION_PAUSE).setPackage(packageName),
                flags
            )
        )

        nextAction = NotificationCompat.Action(
            R.drawable.ic_skip_next,
            "Next",
            PendingIntent.getBroadcast(
                this,
                notificationId,
                Intent(ACTION_NEXT).setPackage(packageName),
                flags
            )
        )

        prevAction = NotificationCompat.Action(
            R.drawable.ic_skip_previous,
            "Previous",
            PendingIntent.getBroadcast(
                this,
                notificationId,
                Intent(ACTION_PREV).setPackage(packageName),
                flags
            )
        )
    }

    private fun buildNotification() {
        // Get the session's metadata
        controller = mediaSession.controller
        mediaMetadata = controller.metadata
        val description = mediaMetadata.description

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
            .setContentIntent(getContentIntent())
            // Stop the service when the notification is swiped away
            .setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this, PlaybackStateCompat.ACTION_STOP
                )
            )
            // Make the transport controls visible on the lockscreen
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            // Add an app icon and set its accent color (Be careful about the color)
            .setSmallIcon(R.drawable.small_launcher_foreground)
            .setColor(ContextCompat.getColor(this, R.color.surface_M))
            // Add buttons
            .addAction(prevAction).addAction(pauseAction).addAction(nextAction)
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
        startForeground(notificationId, notification)
    }

    private fun initMetadata() {
        mediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putBitmap(    //Notification icon in card
                    MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON,
                    BitmapFactory.decodeResource(resources, R.color.surface_M)
                )
                .putBitmap(
                    MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                    BitmapFactory.decodeResource(resources, R.color.surface_M)
                )
                .putBitmap(    //lock screen icon for pre lollipop
                    MediaMetadataCompat.METADATA_KEY_ART,
                    BitmapFactory.decodeResource(resources, R.drawable.small_launcher_foreground)
                )
                .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, "")
                .putText(MediaMetadataCompat.METADATA_KEY_TITLE, "")
                .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, "")
                .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, "")
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, 0)
                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, 0)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
                .build()
        )
    }

    private fun updateMetadata(duration: Boolean) {
        val numOfAvailableSuras = narration.availableSuras.split(",").size

        mediaMetadata = MediaMetadataCompat.Builder()
            .putBitmap(    //Notification icon in card
                MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON,
                BitmapFactory.decodeResource(resources, R.color.surface_M)
            )
            .putBitmap(
                MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                BitmapFactory.decodeResource(resources, R.color.surface_M)
            )
            .putBitmap(    //lock screen icon for pre lollipop
                MediaMetadataCompat.METADATA_KEY_ART,
                BitmapFactory.decodeResource(resources, R.drawable.small_launcher_foreground)
            )
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, suraNames[suraIndex])
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, suraNames[suraIndex])
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, reciterName!!)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, reciterName!!)
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, narration.name)
            .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, suraIndex.toLong())
            .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, numOfAvailableSuras.toLong())
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
                (if (duration) player.duration else 0).toLong()
            )
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
            .build()

        mediaSession.setMetadata(mediaMetadata)
    }

    private fun updatePbState(state: Int, buffered: Long) {
        stateBuilder.setState(state, player.currentPosition.toLong(), 1F)
            .setActions(
                PlaybackStateCompat.ACTION_PLAY_PAUSE
                        or PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_PAUSE
                        or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        or PlaybackStateCompat.ACTION_STOP
                        or PlaybackStateCompat.ACTION_SEEK_TO
                        or PlaybackStateCompat.ACTION_SET_REPEAT_MODE
                        or PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE
            )
            .setBufferedPosition(buffered)

        mediaSession.setPlaybackState(stateBuilder.build())
    }

    private val runnable = Runnable {
        if (controller.playbackState.state == PlaybackStateCompat.STATE_PLAYING) refresh()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun refresh() {
        updatePbState(
            controller.playbackState.state,
            controller.playbackState.bufferedPosition
        )

        if (updateRecordCounter++ == 10) {
            GlobalScope.launch {
                updateDurationRecord(updateRecordCounter)
            }
        }

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
        notificationManager.notify(notificationId, notification)
    }

    @OptIn(DelicateCoroutinesApi::class)
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


        am = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val attrs = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()

        player.setAudioAttributes(attrs)

        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setOnAudioFocusChangeListener(this)
            .setAudioAttributes(attrs)
            .build()

        player.setOnPreparedListener {
            if (playType == "continue") player.seekTo(continueFrom)

            player.start()

            updateMetadata(true) // For the duration
            updatePbState(
                PlaybackStateCompat.STATE_PLAYING,
                controller.playbackState.bufferedPosition
            )
            updateNotification(true)

            refresh()
        }

        player.setOnCompletionListener {
            GlobalScope.launch {
                updateDurationRecord(updateRecordCounter)
            }
            skipToNext()
        }

        player.setOnInfoListener { _, what, _ ->
            when (what) {
                MediaPlayer.MEDIA_INFO_BUFFERING_START ->
                    updatePbState(
                        PlaybackStateCompat.STATE_BUFFERING,
                        controller.playbackState.bufferedPosition
                    )
                MediaPlayer.MEDIA_INFO_BUFFERING_END ->
                    updatePbState(
                        PlaybackStateCompat.STATE_PLAYING,
                        controller.playbackState.bufferedPosition
                    )
            }
            false
        }

        player.setOnBufferingUpdateListener { _, percent ->
            val ratio = percent / 100.0
            var bufferingLevel = 0L
            try {
                bufferingLevel = (player.duration * ratio).toLong()
            } catch (_: Exception) {}
            updatePbState(controller.playbackState.state, bufferingLevel)
        }

        player.setOnErrorListener { _, what, _ ->
            Log.e(Global.TAG, "Error in RecitationsPlayerService player: $what")
            true
        }
    }

    private fun startPlaying(sura: Int) {
        player.reset()

        if (tryOffline(sura)) return

        val text = String.format(Locale.US, "%s/%03d.mp3", narration.server, sura + 1)

        try {
            player.setDataSource(applicationContext, Uri.parse(text))
            player.prepareAsync()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(Global.TAG, "Problem in RecitationsPlayerService player")
        }
    }

    private fun tryOffline(sura: Int): Boolean {
        val path = (getExternalFilesDir(null).toString() + "/Telawat/" + reciterId
                + "/" + narration.id + "/" + sura + ".mp3")

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
            Log.e(Global.TAG, "Problem in RecitationsPlayerService player")
            false
        }
    }

    private fun createNotificationChannel() {
        val description = "Recitations Player Notification Channel"
        channelId = "Recitations"
        val name = getString(R.string.recitations)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val notificationChannel = NotificationChannel(channelId, name, importance)
        notificationChannel.description = description
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun getSuraNames() {
        suraNames = db.surasDao().getDecoratedNamesAr()
    }

    private fun getContentIntent(): PendingIntent {
        val intent = Intent(this, Activity::class.java).apply {
            action = Global.GO_TO_RECITATION
            putExtra("media_id", mediaId)
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

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
        List<MediaItem> mediaItems = mutableListOf<>();
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

    private suspend fun saveForLater(progress: Int) {
        if (reciterName == null) return

        recitationsRepository.setLastPlayedMedia(
            LastPlayedMedia(
                mediaId = mediaId!!,
                progress = progress.toLong()
            )
        )
    }

    private suspend fun updateDurationRecord(amount: Int) {
        val old = userRepository.getRecitationsRecord().first()
        val new = old + amount * 1000

        userRepository.setRecitationsRecord(new)

        updateRecordCounter = 0
    }

    private fun stopForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) stopForeground(STOP_FOREGROUND_DETACH)
        else stopForeground(false)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(Global.TAG, "In onUnbind of RecitationsPlayerService")
        GlobalScope.launch {
            saveForLater(player.currentPosition)
            updateDurationRecord(updateRecordCounter)
        }
        return super.onUnbind(intent)
    }

}