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
import bassamalim.hidaya.R
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.database.dbs.AyatDB
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.utils.DBUtils
import bassamalim.hidaya.utils.PrefUtils
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
    private lateinit var allAyas: List<AyatDB>
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
    private val notificationId = 101
    private var lastPlayedIdx = -1
    private var reciterId = -1
    private var pausedPlayer = -1
    private var chosenSurah = -1
    private var timesPlayed = 1
    private var currentPage = -1
    private var updateRecordCounter = 0
    private var surahEnding = false
    private var resume = true
    private var channelId = "channel ID"
    private val actionPLAY = "bassamalim.hidaya.services.AyahPlayerService.PLAY"
    private val actionPAUSE = "bassamalim.hidaya.services.AyahPlayerService.PAUSE"
    private val actionNEXT = "bassamalim.hidaya.services.AyahPlayerService.NEXT"
    private val actionPREV = "bassamalim.hidaya.services.AyahPlayerService.PREVIOUS"
    private val actionSTOP = "bassamalim.hidaya.services.AyahPlayerService.STOP"

    private lateinit var coordinator: Coordinator
    interface Coordinator {
        fun onUiUpdate(state: Int)
        fun nextPage()
        fun track(ayaId: Int)
    }
    fun setCoordinator(coordinator: Coordinator) {
        this.coordinator = coordinator
    }

    override fun onCreate() {
        super.onCreate()
        // Perform one-time setup procedures

        db = DBUtils.getDB(this)
        pref = PreferenceManager.getDefaultSharedPreferences(this)

        initSession()
        initPlayers()
        setupActions()

        allAyas = db.ayahDao().getAll()
        reciterNames = db.ayatRecitersDao().getNames()
        suarNames =
            if (PrefUtils.getLanguage(this, pref) == "en") db.suarDao().getNamesEn()
            else db.suarDao().getNames()

        initMetadata()
    }

    //The system calls this method when an activity, requests the service be started
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //Handle Intent action from MediaSession.TransportControls
        MediaButtonReceiver.handleIntent(mediaSession, intent)

        return START_NOT_STICKY
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

        player.setAudioAttributes(attrs)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(this)
                .setAudioAttributes(attrs)
                .build()
        }

        players.map { mediaPlayer ->
            mediaPlayer.setOnPreparedListener(this)
            mediaPlayer.setOnCompletionListener(this)
            mediaPlayer.setOnErrorListener(this)
        }
    }

    private fun setupActions() {
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        intentFilter.addAction(actionPLAY)
        intentFilter.addAction(actionPAUSE)
        intentFilter.addAction(actionNEXT)
        intentFilter.addAction(actionPREV)
        intentFilter.addAction(actionSTOP)

        val pkg = packageName
        val flags = PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val description = "quran listening"
            channelId = "AyahPlayer"
            val name = getString(R.string.recitations)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(channelId, name, importance)
            notificationChannel.description = description
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    val callback: MediaSessionCompat.Callback = object : MediaSessionCompat.Callback() {
        override fun onPlayFromMediaId(givenMediaId: String, extras: Bundle) {
            Log.i(Global.TAG, "In onPlayFromMediaId of AyahPlayerService")

            reciterId = PrefUtils.getString(pref, getString(R.string.aya_reciter_key), "13").toInt()

            val initialize = lastPlayedIdx == -1

            lastPlayedIdx = allAyas.indexOfFirst { aya -> aya.id == givenMediaId.toInt() }

            if (initialize) {
                // Start the service
                startService(Intent(applicationContext, AyahPlayerService::class.java))
                updateMetadata(false)
            }
            else resume = false

            onPlay()
        }

        override fun onPlay() {
            Log.i(Global.TAG, "In onPlay of AyahPlayerService")

            buildNotification()

            coordinator.onUiUpdate(PlaybackStateCompat.STATE_PLAYING)
            updateNotification(true)

            // Request audio focus for playback, this registers the afChangeListener
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && audioManager.requestAudioFocus(audioFocusRequest)
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
            if (getState() == PlaybackStateCompat.STATE_PAUSED && resume) {
                resume()
                refresh()
            }
            else playNew(lastPlayedIdx)

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

            updateDurationRecord(updateRecordCounter)

            handler.removeCallbacks(runnable)
            removeAudioFocus()

            // Take the service out of the foreground, retain the notification
            stopForeground()

            coordinator.onUiUpdate(PlaybackStateCompat.STATE_PAUSED)
        }

        override fun onStop() {
            Log.i(Global.TAG, "In onStop of AyahPlayerService")

            coordinator.onUiUpdate(PlaybackStateCompat.STATE_PAUSED)
            updatePbState(PlaybackStateCompat.STATE_STOPPED)

            handler.removeCallbacks(runnable)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                audioManager.abandonAudioFocusRequest(audioFocusRequest) // Abandon audio focus
            unregisterReceiver()
            if (wifiLock.isHeld) wifiLock.release()

            updateDurationRecord(updateRecordCounter)

            releasePlayers()

            stopSelf()    // Stop the service
            mediaSession.isActive = false    // Set the session inactive
            stopForeground()
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

    /**
     * The function's purpose is to prepare the first player to play the given `aya`.
     *
     * @param ayaIdx The ayah to start playing from.
     */
    private fun playNew(ayaIdx: Int) {
        resetPlayers()

        timesPlayed = 1

        preparePlayer(players[0], ayaIdx)
    }

    override fun onPrepared(mp: MediaPlayer) {
        val p1 = players[index(mp)]
        val p2 = players[oIndex(mp)]

        if (p2.isPlaying) p2.setNextMediaPlayer(p1)
        else {
            p1.start()
            coordinator.track(allAyas[lastPlayedIdx].id)
            if (lastPlayedIdx + 1 < allAyas.size) preparePlayer(p2, lastPlayedIdx + 1)
        }

        reciterId = PrefUtils.getString(pref, getString(R.string.aya_reciter_key), "13").toInt()
        updateMetadata(true) // For the duration

        refresh()
    }

    override fun onCompletion(mp: MediaPlayer) {
        val p1 = players[index(mp)]
        val p2 = players[oIndex(mp)]

        val repeat = PrefUtils.getInt(pref, getString(R.string.aya_repeat_key), 1)
        if (repeat == 11) {
            preparePlayer(p1, lastPlayedIdx)
            p2.reset()
            timesPlayed = 1
        }
        else if (timesPlayed < repeat) {
            preparePlayer(p1, lastPlayedIdx)
            p2.reset()
            timesPlayed++
        }
        else {
            timesPlayed = 1
            if (getState() == PlaybackStateCompat.STATE_PAUSED) {
                if (lastPlayedIdx < allAyas.size) {
                    val newAyaIdx = lastPlayedIdx + 1
                    val newAyah = allAyas[newAyaIdx]
                    coordinator.track(newAyah.id)
                    if (newAyah.id < allAyas.size) preparePlayer(p1, newAyaIdx)
                    lastPlayedIdx = newAyaIdx
                }
                else ended()
            }
            else if (lastPlayedIdx < allAyas.size + 1) {
                val newAyaIdx = lastPlayedIdx + 1
                val newAyah = allAyas[newAyaIdx]
                coordinator.track(newAyah.id)
                val newerAyaIdx = newAyaIdx + 1
                lastPlayedIdx = newAyaIdx
                if (newAyah.id < allAyas.size) preparePlayer(p1, newerAyaIdx)
            }
            else ended()
        }

        updateDurationRecord(updateRecordCounter)
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        // Not found
        Toast.makeText(
            this,
            getString(R.string.recitation_not_available),
            Toast.LENGTH_SHORT
        ).show()

        updatePbState(PlaybackStateCompat.STATE_STOPPED)
        coordinator.onUiUpdate(PlaybackStateCompat.STATE_PAUSED)

        getUri(allAyas[lastPlayedIdx])

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
        var currentPosition = 0L
        try {
            currentPosition = player.currentPosition.toLong()
        } catch (_: IllegalStateException) {}

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
        val aya = allAyas[lastPlayedIdx]

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
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, suarNames[aya.sura_num])
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, suarNames[aya.sura_num])
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, reciterNames[reciterId])
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, reciterNames[reciterId])
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, aya.aya_num.toString())
            .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, aya.aya_num.toLong())
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
     * @param ayaIdx the ayah to play
     */
    private fun preparePlayer(player: MediaPlayer, ayaIdx: Int) {
        val aya = allAyas[ayaIdx]

        if (PrefUtils.getBoolean(pref, getString(R.string.stop_on_sura_key), false)
            && aya.sura_num != chosenSurah) {
            if (surahEnding) stopPlaying()
            else surahEnding = true
            return
        }

        player.reset()

        val uri: Uri
        try {
            uri = getUri(aya)
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
            || lastPlayedIdx + 2 > allAyas.size)
            return

        lastPlayedIdx++
        coordinator.track(allAyas[lastPlayedIdx].id)

        for (i in 0..1) {
            if (players[i].isPlaying) {
                preparePlayer(players[i], lastPlayedIdx)
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
        if (getState() != PlaybackStateCompat.STATE_PLAYING || lastPlayedIdx - 1 < 0) return

        lastPlayedIdx--
        coordinator.track(allAyas[lastPlayedIdx].id)

        for (i in 0..1) {
            if (players[i].isPlaying) {
                preparePlayer(players[i], lastPlayedIdx)
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
                players[i].pause()
                pausedPlayer = i
            }
        }
        resume = true
    }

    /**
     * Resume the last player that was playing.
     */
    fun resume() {
        players[pausedPlayer].start()
        pausedPlayer = -1
    }

    private fun ended() {
        val quranPages = 604
        if (PrefUtils.getBoolean(pref, getString(R.string.stop_on_page_key), false)) stopPlaying()
        else if (currentPage < quranPages && lastPlayedIdx + 1 == allAyas.size) {
            coordinator.nextPage()

            callback.onPlayFromMediaId("0", null)
        }
    }

    private fun stopPlaying() {
        updatePbState(PlaybackStateCompat.STATE_STOPPED)
        coordinator.onUiUpdate(PlaybackStateCompat.STATE_PAUSED)

        resetPlayers()
        releasePlayers()
    }

    private fun getUri(ayah: AyatDB): Uri {
        val choice = PrefUtils.getString(pref, getString(R.string.aya_reciter_key), "13").toInt()
        val sources = db.ayatTelawaDao().getReciter(choice)

        var uri = "https://www.everyayah.com/data/"
        uri += sources[0].getSource()
        uri += String.format(Locale.US, "%03d%03d.mp3", ayah.sura_num, ayah.aya_num)

        return Uri.parse(uri)
    }

    fun getState(): Int {
        return controller.playbackState.state
    }

    fun setChosenPage(currentPage: Int) {
        this.currentPage = currentPage
    }

    fun setChosenSurah(chosenSurah: Int) {
        this.chosenSurah = chosenSurah
    }

    private fun o(i: Int): Int {
        return (i + 1) % 2
    }

    fun finish() {
        releasePlayers()
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
        val old = PrefUtils.getLong(pref, "telawat_playback_record", 0L)
        val new = old + amount * 1000

        pref.edit()
            .putLong("telawat_playback_record", new)
            .apply()

        updateRecordCounter = 0
    }

    private fun resetPlayers() {
        players[0].reset()
        players[1].reset()
    }

    private fun releasePlayers() {
        players[0].release()
        players[1].release()
    }

    private fun stopForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) stopForeground(STOP_FOREGROUND_DETACH)
        else stopForeground(false)
    }

    private fun unregisterReceiver() {
        try {
            unregisterReceiver(receiver)
        } catch (_: IllegalArgumentException) {}
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