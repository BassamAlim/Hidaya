package bassamalim.hidaya.services

import android.app.*
import android.content.*
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer
import android.media.MediaPlayer.*
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.*
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import androidx.preference.PreferenceManager
import androidx.room.Room
import bassamalim.hidaya.R
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.database.dbs.AyatTelawaDB
import bassamalim.hidaya.models.Aya
import bassamalim.hidaya.other.Global
import java.util.*

class AyahPlayerService : Service(),
    OnCompletionListener, OnPreparedListener, OnErrorListener, OnAudioFocusChangeListener {

    private val iBinder = LocalBinder()
    private val intentFilter = IntentFilter()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var players: Array<MediaPlayer>
    private lateinit var player: MediaPlayer
    private lateinit var wifiLock: WifiManager.WifiLock
    private lateinit var db: AppDatabase
    private lateinit var pref: SharedPreferences
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var audioFocusRequest: AudioFocusRequest
    private lateinit var controller: MediaControllerCompat
    lateinit var transportControls: MediaControllerCompat.TransportControls
    private lateinit var currentAyah: Aya
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
    private lateinit var reciterNames: List<String>
    private lateinit var suarNames: List<String>
    private lateinit var viewType: String
    private var lastPlayed: Aya? = null
    private val notificationId = 101
    private var reciterId = -1
    private var pausedPlayer = -1
    private var chosenSurah = -1
    private var repeated = 1
    private var currentPage = -1
    private var allAyahsSize = -1
    private var updateRecordCounter = 0
    private var surahEnding = false
    private var channelId = "channel ID"
    private val actionPLAY = "bassamalim.hidaya.services.AyahPlayerService.PLAY"
    private val actionPAUSE = "bassamalim.hidaya.services.AyahPlayerService.PAUSE"
    private val actionNEXT = "bassamalim.hidaya.services.AyahPlayerService.NEXT"
    private val actionPREV = "bassamalim.hidaya.services.AyahPlayerService.PREVIOUS"
    private val actionSTOP = "bassamalim.hidaya.services.AyahPlayerService.STOP"

    private lateinit var coordinator: Coordinator
    interface Coordinator {
        fun onUiUpdate(state: Int)
        fun getAyah(index: Int): Aya
        fun nextPage()
        fun track(ayaIndex: Int)
    }
    fun setCoordinator(coordinator: Coordinator) {
        this.coordinator = coordinator
    }

    override fun onCreate() {
        super.onCreate()
        // Perform one-time setup procedures

        db = Room.databaseBuilder(this, AppDatabase::class.java, "HidayaDB")
        .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build()
        pref = PreferenceManager.getDefaultSharedPreferences(this)

        initSession()
        initPlayers()
        setupActions()

        reciterNames = db.ayatRecitersDao().getNames()
        suarNames =
            if (pref.getString(
                    getString(R.string.language_key), getString(R.string.default_language)
                ) == "en")
                db.suarDao().getNamesEn()
            else db.suarDao().getNames()

        initMetadata()
    }

    //The system calls this method when an activity, requests the service be started
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        //Handle Intent action from MediaSession.TransportControls
        MediaButtonReceiver.handleIntent(mediaSession, intent)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder {
        return iBinder
    }

    private fun initSession() {
        // Create a MediaSessionCompat
        mediaSession = MediaSessionCompat(this, "AyahPlayerService")

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
        stateBuilder = PlaybackStateCompat.Builder().setActions(
            PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE
        )
        mediaSession.setPlaybackState(stateBuilder.build())

        // callback() has methods that handle callbacks from a media controller
        mediaSession.setCallback(callback)

        //set MediaSession -> ready to receive media commands
        mediaSession.isActive = true

        //Get MediaSessions transport controls
        transportControls = mediaSession.controller.transportControls
    }

    /**
     * Initialize the two media players and the wifi lock
     */
    private fun initPlayers() {
        players = arrayOf(MediaPlayer(), MediaPlayer())
        player = players[0]

        players[0].setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        players[1].setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)

        wifiLock = (applicationContext.getSystemService(Context.WIFI_SERVICE)
                as WifiManager).createWifiLock(WifiManager.WIFI_MODE_FULL_LOW_LATENCY, "myLock")

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val attrs: AudioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()

        player.setAudioAttributes(attrs)

        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setOnAudioFocusChangeListener(this)
            .setAudioAttributes(attrs)
            .build()

        for (i in 0..1) {
            players[i].setOnPreparedListener(this)
            players[i].setOnCompletionListener(this)
            players[i].setOnErrorListener(this)
        }
    }

    private fun setupActions() {
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        intentFilter.addAction(actionPLAY)
        intentFilter.addAction(actionPAUSE)
        intentFilter.addAction(actionNEXT)
        intentFilter.addAction(actionPREV)
        intentFilter.addAction(actionSTOP)

        val pkg: String = packageName
        val flags: Int = PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE

        playAction = NotificationCompat.Action(
            R.drawable.ic_play_arrow, "Play", PendingIntent.getBroadcast(
                this@AyahPlayerService, notificationId,
                Intent(actionPLAY).setPackage(pkg), flags)
        )

        pauseAction = NotificationCompat.Action(
            R.drawable.ic_baseline_pause, "Pause", PendingIntent.getBroadcast(
                this@AyahPlayerService, notificationId,
                Intent(actionPAUSE).setPackage(pkg), flags
            )
        )

        nextAction = NotificationCompat.Action(
            R.drawable.ic_skip_next, "next", PendingIntent.getBroadcast(
                this@AyahPlayerService, notificationId,
                Intent(actionNEXT).setPackage(pkg), flags
            )
        )

        prevAction = NotificationCompat.Action(
            R.drawable.ic_skip_previous, "previous", PendingIntent.getBroadcast(
                this@AyahPlayerService, notificationId,
                Intent(actionPREV).setPackage(pkg), flags
            )
        )
    }

    private fun buildNotification() {
        // Get the session's metadata
        controller = mediaSession.controller
        mediaMetadata = controller.metadata
        val description: MediaDescriptionCompat = mediaMetadata.description

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
            .setSmallIcon(R.drawable.launcher_foreground)
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
        val name: CharSequence
        val description = "quran listening"
        channelId = "AyahPlayer"
        name = getString(R.string.recitations)
        val importance: Int = NotificationManager.IMPORTANCE_DEFAULT
        val notificationChannel = NotificationChannel(channelId, name, importance)
        notificationChannel.description = description
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)
    }

    val callback: MediaSessionCompat.Callback = object : MediaSessionCompat.Callback() {
        override fun onPlayFromMediaId(givenMediaId: String, extras: Bundle) {
            Log.i(Global.TAG, "In onPlayFromMediaId of AyahPlayerService")

            currentAyah = extras.getSerializable("aya") as Aya
            reciterId = pref.getString(getString(R.string.aya_reciter_key), "13")!!.toInt()

            // Start the service
            startService(Intent(applicationContext, AyahPlayerService::class.java))
            buildNotification()
            updateMetadata(false)

            onPlay()
        }

        override fun onPlay() {
            Log.i(Global.TAG, "In onPlay of AyahPlayerService")

            coordinator.onUiUpdate(PlaybackStateCompat.STATE_PLAYING)
            updateNotification(true)

            // Request audio focus for playback, this registers the afChangeListener
            if (audioManager.requestAudioFocus(audioFocusRequest)
                != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                return

            // Set the session active  (and update metadata and state)
            mediaSession.isActive = true
            // Put the service in the foreground, post notification
            startForeground(notificationId, notification)

            // Register Receiver
            registerReceiver(receiver, intentFilter)

            wifiLock.acquire()

            // start the player (custom call)
            if (getState() == PlaybackStateCompat.STATE_PAUSED) {
                Log.d(Global.TAG, "Resumed")
                resume()
                refresh()
            }
            else requestPlay(currentAyah)

            updatePbState(PlaybackStateCompat.STATE_PLAYING)
            coordinator.onUiUpdate(PlaybackStateCompat.STATE_PLAYING)
            updateNotification(true)
        }

        override fun onPause() {
            Log.i(Global.TAG, "In onPause of AyahPlayerService")

            // Update metadata and state
            updatePbState(PlaybackStateCompat.STATE_PAUSED)
            updateNotification(false)

            // pause the player
            pause()

            Log.d(Global.TAG, "NOW: ${getState()}")

            updateDurationRecord(updateRecordCounter)

            handler.removeCallbacks(runnable)
            removeAudioFocus()

            // Take the service out of the foreground, retain the notification
            stopForeground(false)

//            coordinator.onUiUpdate(PlaybackStateCompat.STATE_PAUSED)
//            updatePbState(PlaybackStateCompat.STATE_PAUSED)
//            updateNotification(false)
        }

        override fun onStop() {
            Log.i(Global.TAG, "In onStop of AyahPlayerService")

            coordinator.onUiUpdate(PlaybackStateCompat.STATE_PAUSED)
            updatePbState(PlaybackStateCompat.STATE_STOPPED)

            handler.removeCallbacks(runnable)
            audioManager.abandonAudioFocusRequest(audioFocusRequest)    // Abandon audio focus
            unregisterReceiver()
            if (wifiLock.isHeld) wifiLock.release()

            updateDurationRecord(updateRecordCounter)

            releasePlayers()

            stopSelf()    // Stop the service
            mediaSession.isActive = false    // Set the session inactive
            stopForeground(false)    // Take the service out of the foreground
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            nextAyah()
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            previousAyah()
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            player.seekTo(pos.toInt())
            updatePbState(getState())
        }

        override fun onSetRepeatMode(repeatMode: Int) {
            super.onSetRepeatMode(repeatMode)
            player.isLooping = repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE
        }
    }

    override fun onPrepared(mp: MediaPlayer) {
        val p1 = players[index(mp)]
        val p2 = players[oIndex(mp)]

        if (p2.isPlaying) p2.setNextMediaPlayer(p1)
        else if (getState() == PlaybackStateCompat.STATE_PAUSED) {
            if (allAyahsSize > lastPlayed!!.ayaIndex)
                lastPlayed = coordinator.getAyah(lastPlayed!!.ayaIndex)
        }
        else {
            p1.start()
            coordinator.track(lastPlayed!!.ayaIndex)
            if (allAyahsSize > lastPlayed!!.ayaIndex + 1)
                preparePlayer(p2, coordinator.getAyah(lastPlayed!!.ayaIndex + 1))
        }

        reciterId = pref.getString(getString(R.string.aya_reciter_key), "13")!!.toInt()
        updateMetadata(true) // For the duration

        refresh()
    }

    override fun onCompletion(mp: MediaPlayer) {
        val p1 = players[index(mp)]
        val p2 = players[oIndex(mp)]

        val repeat: Int = pref.getString(getString(R.string.aya_repeat_mode_key), "1")!!.toInt()
        if ((repeat == 2 || repeat == 3 || repeat == 5 || repeat == 10)
            && repeated < repeat) {
            preparePlayer(p1, lastPlayed)
            p2.reset()
            repeated++
        }
        else if (repeat == 0) {
            repeated = 0
            preparePlayer(p1, lastPlayed)
            p2.reset()
        }
        else {
            repeated = 1
            if (getState() == PlaybackStateCompat.STATE_PAUSED) {
                if (allAyahsSize > lastPlayed!!.ayaIndex) {
                    val newAyah: Aya = coordinator.getAyah(lastPlayed!!.ayaIndex + 1)
                    coordinator.track(newAyah.ayaIndex)
                    if (allAyahsSize > newAyah.ayaIndex)
                        preparePlayer(p1, coordinator.getAyah(newAyah.ayaIndex))
                    lastPlayed = newAyah
                }
                else ended()
            }
            else if (allAyahsSize > lastPlayed!!.ayaIndex + 1) {
                val newAyah: Aya = coordinator.getAyah(lastPlayed!!.ayaIndex + 1)
                coordinator.track(newAyah.ayaIndex)
                if (allAyahsSize > newAyah.ayaIndex + 1)
                    preparePlayer(p1, coordinator.getAyah(newAyah.ayaIndex + 1))
                lastPlayed = newAyah
            }
            else ended()
        }

        updateDurationRecord(updateRecordCounter)
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        // Not found
        Toast.makeText(this, getString(R.string.recitation_not_available), Toast.LENGTH_SHORT).show()

        updatePbState(PlaybackStateCompat.STATE_STOPPED)
        coordinator.onUiUpdate(PlaybackStateCompat.STATE_PAUSED)

        getUri(lastPlayed)

        return true
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                AudioManager.ACTION_AUDIO_BECOMING_NOISY -> {
                    Log.i(Global.TAG, "In ACTION_BECOMING_NOISY")
                    callback.onPause()
                }
                actionPLAY -> {
                    Log.i(Global.TAG, "In ACTION_PLAY")
                    callback.onPlay()
                }
                actionPAUSE -> {
                    Log.i(Global.TAG, "In ACTION_PAUSE")
                    callback.onPause()
                }
                actionNEXT -> {
                    Log.i(Global.TAG, "In ACTION_NEXT")
                    nextAyah()
                }
                actionPREV -> {
                    Log.i(Global.TAG, "In ACTION_PREV")
                    previousAyah()
                }
                actionSTOP -> {
                    Log.i(Global.TAG, "In ACTION_STOP")
                    callback.onStop()
                }
            }
        }
    }

    /**
     * The function's purpose is to prepare the first player to play the given `startAyah`.
     *
     * @param startAyah The ayah to start playing from.
     */
    private fun requestPlay(startAyah: Aya) {
        repeated = 1
        lastPlayed = startAyah
        preparePlayer(players[0], startAyah)
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
        if (getState() == PlaybackStateCompat.STATE_PLAYING) refresh()
    }

    private fun refresh() {
        updatePbState(getState())

        if (updateRecordCounter++ == 10) updateDurationRecord(updateRecordCounter)

        handler.postDelayed(runnable, 1000)
    }

    private fun updatePbState(state: Int) {
        Log.d(Global.TAG, "In updatePbState for $state")

        var currentPosition = 0L
        try {
            currentPosition = player.currentPosition.toLong()
        } catch (e: IllegalStateException) {}

        stateBuilder.setState(state, currentPosition, 1F)
            .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
            .setBufferedPosition(controller.playbackState.bufferedPosition)
        mediaSession.setPlaybackState(stateBuilder.build())
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                players[0].setVolume(1.0f, 1.0f)
                players[1].setVolume(1.0f, 1.0f)
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (getState() == PlaybackStateCompat.STATE_PLAYING) {
                    players[0].setVolume(0.3f, 0.3f)
                    players[1].setVolume(0.3f, 0.3f)
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT, AudioManager.AUDIOFOCUS_LOSS -> {
                if (getState() == PlaybackStateCompat.STATE_PLAYING) callback.onPause()
            }
        }
    }

    private fun removeAudioFocus(): Boolean {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocusRequest(audioFocusRequest)
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
                    BitmapFactory.decodeResource(resources, R.drawable.launcher_foreground)
                )
                .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, "")
                .putText(MediaMetadataCompat.METADATA_KEY_TITLE, "")
                .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, "")
                .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, "")
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, 0)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
                .build()
        )
    }

    private fun updateMetadata(duration: Boolean) {
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
                BitmapFactory.decodeResource(resources, R.drawable.launcher_foreground)
            )
            .putString(
                MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, suarNames[currentAyah.suraNum]
            )
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, suarNames[currentAyah.suraNum])
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, reciterNames[reciterId])
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, reciterNames[reciterId])
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION,
                currentAyah.ayaNum.toString())
            .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, currentAyah.ayaNum.toLong())
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
                (if (duration) player.duration else 0).toLong()
            )
            .build()

        mediaSession.setMetadata(mediaMetadata)
    }

    /**
     * It checks if the user wants the player to stop on the end of the sura and the given aya is
     * from a new sura, meaning the sura is ending
     * if so it checks the flag that says that there is no more ayas to prepare
     * if so it stops playing
     * if not it sets the flag to no more ayas
     * if not it prepares the player by resetting it and setting the data source and calling
     * MediaPlayer's prepare()
     *
     * @param player The MediaPlayer object that will be used to play the audio.
     * @param ayah the ayah to play
     */
    private fun preparePlayer(player: MediaPlayer, ayah: Aya?) {
        if (pref.getBoolean(getString(R.string.stop_on_sura_key), false)
            && ayah!!.suraNum != chosenSurah) {
            if (surahEnding) stopPlaying()
            else surahEnding = true
            return
        }

        player.reset()

        val uri: Uri
        try {
            uri = getUri(ayah)
            player.setDataSource(applicationContext, uri)
            player.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(Global.TAG, "Reciter not found in ayat telawa")
        }
    }

    /**
     * If the player is playing and there is a next ayah
     * prepare the next ayah and reset the other player
     */
    fun nextAyah() {
        if (getState() != PlaybackStateCompat.STATE_PLAYING
            || lastPlayed!!.ayaIndex + 2 > allAyahsSize)
            return

        lastPlayed = coordinator.getAyah(lastPlayed!!.ayaIndex + 1)
        coordinator.track(lastPlayed!!.ayaIndex)

        for (i in 0..1) {
            if (players[i].isPlaying) {
                preparePlayer(players[i], lastPlayed)
                players[o(i)].reset()
                break
            }
        }

        updateMetadata(false)
    }

    /**
     * If the player is playing and there is a previous ayah
     * prepare the next ayah and reset the other player
     */
    fun previousAyah() {
        if (getState() != PlaybackStateCompat.STATE_PLAYING || lastPlayed!!.ayaIndex - 1 < 0)
            return

        lastPlayed = coordinator.getAyah(lastPlayed!!.ayaIndex - 1)
        coordinator.track(lastPlayed!!.ayaIndex)

        for (i in 0..1) {
            if (players[i].isPlaying) {
                preparePlayer(players[i], lastPlayed)
                players[o(i)].reset()
                break
            }
        }

        updateMetadata(false)
    }

    /**
     * Pause the two players
     */
    private fun pause() {
        for (i in 0..1) {
            if (players[i].isPlaying) {
                Log.d(Global.TAG, "Paused $i")
                players[i].pause()
                pausedPlayer = i
            }
        }
    }

    /**
     * Resume the last player that was playing.
     */
    fun resume() {
        Log.d(Global.TAG, "Resume P$pausedPlayer")
        players[pausedPlayer].start()
        pausedPlayer = -1
    }

    private fun ended() {
        val quranPages = 604
        if (pref.getBoolean(getString(R.string.stop_on_page_key), false)) stopPlaying()
        else if (currentPage < quranPages && lastPlayed!!.ayaIndex + 1 == allAyahsSize) {
            coordinator.nextPage()
            requestPlay(coordinator.getAyah(0))
        }
    }

    private fun stopPlaying() {
        updatePbState(PlaybackStateCompat.STATE_STOPPED)
        coordinator.onUiUpdate(PlaybackStateCompat.STATE_PAUSED)

        for (i in 0..1) {
            players[i].reset()
            players[i].release()
        }
    }

    private fun getUri(ayah: Aya?): Uri {
        val choice: Int = pref.getString(getString(R.string.aya_reciter_key), "13")!!.toInt()
        val sources: List<AyatTelawaDB?> = db.ayatTelawaDao().getReciter(choice)

        var uri = "https://www.everyayah.com/data/"
        uri += sources[0]!!.getSource()
        uri += String.format(Locale.US, "%03d%03d.mp3", ayah!!.suraNum, ayah.ayaNum)

        return Uri.parse(uri)
    }

    fun getState(): Int {
        return controller.playbackState.state
    }

    fun setAllAyahsSize(allAyahsSize: Int) {
        this.allAyahsSize = allAyahsSize
    }

    fun setCurrentPage(currentPage: Int) {
        this.currentPage = currentPage
    }

    fun setChosenSurah(chosenSurah: Int) {
        this.chosenSurah = chosenSurah
    }

    fun setViewType(viewType: String) {
        this.viewType = viewType
    }

    private fun o(i: Int): Int {
        return (i + 1) % 2
    }

    fun finish() {
        for (i in 0..1) players[i].release()
        wifiLock.release()
    }

    private fun index(mp: MediaPlayer): Int {
        return if (mp == players[0]) 0
        else 1
    }

    private fun oIndex(mp: MediaPlayer): Int {
        return if (mp == players[0]) 1
        else 0
    }

    private fun updateDurationRecord(amount: Int) {
        val old = pref.getLong("telawat_playback_record", 0L)
        val new = old + amount * 1000

        val editor = pref.edit()
        editor.putLong("telawat_playback_record", new)
        editor.apply()

        updateRecordCounter = 0
    }

    private fun releasePlayers() {
        players[0].release()
        players[1].release()
    }

    private fun unregisterReceiver() {
        try {
            unregisterReceiver(receiver)
        } catch (e: IllegalArgumentException) {}
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(Global.TAG, "In onUnbind of AyahPlayerService")
        updateDurationRecord(updateRecordCounter)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()

        handler.removeCallbacks(runnable)

        releasePlayers()

        removeAudioFocus()

        unregisterReceiver()
    }

    inner class LocalBinder : Binder() {
        val service: AyahPlayerService
            get() = this@AyahPlayerService
    }

}