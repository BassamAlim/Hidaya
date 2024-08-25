package bassamalim.hidaya.features.quranReader.ayaPlayer

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
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.database.models.Verse
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.helpers.ReceiverManager
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.utils.ActivityUtils
import bassamalim.hidaya.core.utils.DBUtils
import bassamalim.hidaya.features.quran.quranReader.versePlayer.AlternatingPlayersManager

@UnstableApi
@RequiresApi(api = Build.VERSION_CODES.O)
class VersePlayerService : MediaBrowserServiceCompat(), OnAudioFocusChangeListener, PlayerCallback {

    private val intentFilter = IntentFilter()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var apm: AlternatingPlayersManager
    private lateinit var wifiLock: WifiManager.WifiLock
    private lateinit var db: AppDatabase
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var audioFocusRequest: AudioFocusRequest
    private lateinit var controller: MediaControllerCompat
    private lateinit var audioManager: AudioManager
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private lateinit var mediaMetadata: MediaMetadataCompat
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notification: Notification
    private lateinit var playAction: NotificationCompat.Action
    private lateinit var pauseAction: NotificationCompat.Action
    private lateinit var nextAction: NotificationCompat.Action
    private lateinit var prevAction: NotificationCompat.Action
    private lateinit var verses: List<Verse>
    private lateinit var reciterNames: List<String>
    private lateinit var suarNames: List<String>
    private val notificationId = 101
    private var reciterId = -1
    private var updateRecordCounter = 0
    private var channelId = "channel ID"

    companion object {
        private const val MY_MEDIA_ROOT_ID = "media_root_id"
        private const val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"
        private const val ACTION_PLAY = "bassamalim.hidaya.features.quranViewer.AyaPlayerService.PLAY"
        private const val ACTION_PAUSE = "bassamalim.hidaya.features.quranViewer.AyaPlayerService.PAUSE"
        private const val ACTION_NEXT = "bassamalim.hidaya.features.quranViewer.AyaPlayerService.NEXT"
        private const val ACTION_PREV = "bassamalim.hidaya.features.quranViewer.AyaPlayerService.PREVIOUS"
        private const val ACTION_STOP = "bassamalim.hidaya.features.quranViewer.AyaPlayerService.STOP"
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
                    apm.nextVerse()
                }
                ACTION_PREV -> {
                    Log.i(Global.TAG, "In ACTION_PREV")
                    apm.previousVerse()
                }
                ACTION_STOP -> {
                    Log.i(Global.TAG, "In ACTION_STOP")
                    callback.onStop()
                }
            }
        }
    }

    private val receiverManager = ReceiverManager(this, receiver, intentFilter)

    override fun onCreate() {
        super.onCreate()

        ActivityUtils.onActivityCreateSetLocale(applicationContext)

        preferencesDS = PreferencesDataSource(
            PreferenceManager.getDefaultSharedPreferences(this)
        )
        db = DBUtils.getDB(this)

        verses = db.versesDao().getAll()
        reciterNames = db.verseRecitersDao().getNames()
        suarNames =
            if (preferencesDS.getLanguage() == Language.ENGLISH) db.surasDao().getDecoratedNamesEn()
            else db.surasDao().getDecoratedNamesAr()

        initSession()
        initAPM()
        setupActions()
        initMetadata()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    val callback: MediaSessionCompat.Callback = object : MediaSessionCompat.Callback() {
        override fun onPlayFromMediaId(mediaId: String, extras: Bundle) {
            Log.i(Global.TAG, "In onPlayFromMediaId of AyaPlayerService")

            val ayaId = mediaId.toInt()

            reciterId = preferencesDS.getString(Preference.VerseReciter).toInt()

            if (apm.isNotInitialized()) {
                // Start the service
                startService(Intent(applicationContext, VersePlayerService::class.java))
            }

            mediaSession.isActive = true

            buildNotification()

            // Put the service in the foreground, post notification
            startForeground(notificationId, notification)

            wifiLock.acquire()

            updatePbState(PlaybackStateCompat.STATE_PLAYING)
            updateNotification(true)
            updateMetadata(ayaId, false)

            // Request audio focus for playback, this registers the afChangeListener
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && audioManager.requestAudioFocus(audioFocusRequest)
                != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                return

            receiverManager.register()

            apm.playFromMediaId(verseIdx = ayaId-1)
        }

        // used as onResume
        override fun onPlay() {
            Log.i(Global.TAG, "In onPlay of AyaPlayerService")

            if (apm.isNotInitialized()) {
                // Start the service
                startService(Intent(applicationContext, VersePlayerService::class.java))
            }

            mediaSession.isActive = true

            buildNotification()

            // Put the service in the foreground, post notification
            startForeground(notificationId, notification)

            wifiLock.acquire()

            updatePbState(PlaybackStateCompat.STATE_PLAYING)
            updateNotification(true)

            // Request audio focus for playback, this registers the afChangeListener
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && audioManager.requestAudioFocus(audioFocusRequest)
                != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                return

            receiverManager.register()

            apm.resume()
        }

        override fun onPause() {
            Log.i(Global.TAG, "In onPause of AyaPlayerService")

            // Update metadata and state
            updatePbState(PlaybackStateCompat.STATE_PAUSED)
            updateNotification(false)

            // pause the player
            apm.pause()

            // Update metadata and state

            updateDurationRecord(updateRecordCounter)

            handler.removeCallbacks(runnable)
            abandonAudioFocus()

            // Take the service out of the foreground, retain the notification
            stopForeground()
        }

        override fun onStop() {
            Log.i(Global.TAG, "In onStop of AyaPlayerService")

            updatePbState(PlaybackStateCompat.STATE_STOPPED)

            handler.removeCallbacks(runnable)
            abandonAudioFocus()
            receiverManager.unregister()
            if (wifiLock.isHeld) wifiLock.release()

            updateDurationRecord(updateRecordCounter)

            apm.release()

            stopSelf()    // Stop the service
            mediaSession.isActive = false    // Set the session inactive
            stopForeground()
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            apm.nextVerse()
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            apm.previousVerse()
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            apm.seekTo(pos)
            updatePbState(getPbState())
        }
    }

    private fun initSession() {
        // Create a MediaSessionCompat
        mediaSession = MediaSessionCompat(this, "AyaPlayerService")

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

    /**
     * Initialize the two media players and the wifi lock
     */
    private fun initAPM() {
        apm = AlternatingPlayersManager(
            ctx = this,
            preferencesDS = preferencesDS,
            db = db,
            callback = this
        )

        wifiLock =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                (applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
                    .createWifiLock(WifiManager.WIFI_MODE_FULL_LOW_LATENCY, "myLock")
            else
                (applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
                    .createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "myLock")

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val attrs = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()

        apm.setAudioAttributes(attrs)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(this)
                .setAudioAttributes(attrs)
                .build()
        }
    }

    override fun updatePbState(state: Int) {
        var currentPosition = 0L
        try {
            currentPosition = apm.getCurrentPosition().toLong()
        } catch (_: Exception) {}

        stateBuilder.setState(state, currentPosition, 1F)
            .setActions(
                PlaybackStateCompat.ACTION_PLAY_PAUSE
                        or PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_PAUSE
                        or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        or PlaybackStateCompat.ACTION_SEEK_TO
                        or PlaybackStateCompat.ACTION_STOP
            )
        mediaSession.setPlaybackState(stateBuilder.build())
    }

    override fun getPbState(): Int {
        return controller.playbackState.state
    }

    override fun track(verseId: Int) {
        updateMetadata(verseId, true)
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

        createNotificationChannel()

        // Create a NotificationCompat.Builder
        notificationBuilder = NotificationCompat.Builder(this, channelId)
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
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val description = "quran listening"
            channelId = "AyaPlayer"
            val name = getString(R.string.recitations)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(channelId, name, importance)
            notificationChannel.description = description
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
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

    private val runnable = Runnable {
        if (getPbState() == PlaybackStateCompat.STATE_PLAYING)
            refresh()
    }

    private fun refresh() {
        updatePbState(getPbState())

        if (updateRecordCounter++ == 10) updateDurationRecord(updateRecordCounter)

        handler.postDelayed(runnable, 1000)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                apm.setVolume(1.0f)
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (getPbState() == PlaybackStateCompat.STATE_PLAYING)
                    apm.setVolume(0.3f)
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT, AudioManager.AUDIOFOCUS_LOSS -> {
                if (getPbState() == PlaybackStateCompat.STATE_PLAYING)
                    callback.onPause()
            }
        }
    }

    private fun abandonAudioFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                    audioManager.abandonAudioFocusRequest(audioFocusRequest)
        }
        else true
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
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, 0)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
                .build()
        )
    }

    private fun updateMetadata(ayaId: Int = apm.verseIdx, duration: Boolean) {
        val aya = getAya(ayaId)

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
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, suarNames[aya.suraNum-1])
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, suarNames[aya.suraNum-1])
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, reciterNames[reciterId])
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, reciterNames[reciterId])
            .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, aya.id.toLong())
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
                (if (duration) apm.getDuration() else 0).toLong()
            )
            .putLong("page_num", aya.pageNum.toLong())
            .build()

        mediaSession.setMetadata(mediaMetadata)
    }

    private fun getAya(id: Int) = verses[id-1]

    private fun updateDurationRecord(amount: Int) {
        val old = preferencesDS.getLong(Preference.RecitationsPlaybackRecord)
        val new = old + amount * 1000

        preferencesDS.setLong(Preference.RecitationsPlaybackRecord, new)

        updateRecordCounter = 0
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
        val mediaItems = mutableListOf()
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

    private fun stopForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) stopForeground(STOP_FOREGROUND_DETACH)
        else stopForeground(false)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(Global.TAG, "In onUnbind of AyaPlayerService")
        updateDurationRecord(updateRecordCounter)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()

        handler.removeCallbacks(runnable)

        apm.release()

        if (wifiLock.isHeld) wifiLock.release()

        abandonAudioFocus()

        receiverManager.unregister()
    }

}