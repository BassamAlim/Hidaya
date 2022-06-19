package bassamalim.hidaya.activities

import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import androidx.room.Room
import bassamalim.hidaya.R
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.database.dbs.TelawatVersionsDB
import bassamalim.hidaya.databinding.ActivityTelawatClientBinding
import bassamalim.hidaya.models.Reciter.RecitationVersion
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.other.Utils
import bassamalim.hidaya.services.TelawatService
import java.util.*

@RequiresApi(api = Build.VERSION_CODES.O)
class TelawatClient : AppCompatActivity() {
    private var binding: ActivityTelawatClientBinding? = null
    private var db: AppDatabase? = null
    private var pref: SharedPreferences? = null
    private var mediaBrowser: MediaBrowserCompat? = null
    private var controller: MediaControllerCompat? = null
    private var tc: MediaControllerCompat.TransportControls? = null
    private var surahNamescreen: TextView? = null
    private var seekBar: SeekBar? = null
    private var progressScreen: TextView? = null
    private var durationScreen: TextView? = null
    private var playPause: ImageButton? = null
    private var nextBtn: ImageButton? = null
    private var prevBtn: ImageButton? = null
    private var forwardBtn: ImageButton? = null
    private var rewindBtn: ImageButton? = null
    private var repeatBtn: ImageButton? = null
    private var shuffleBtn: ImageButton? = null
    private var action: String? = null
    private var mediaId: String? = null
    private var reciterId = 0
    private var versionId = 0
    private var surahIndex = 0
    private var reciterName: String? = null
    private var version: RecitationVersion? = null
    private var surahNames: List<String>? = null
    private var repeat = 0
    private var shuffle = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.myOnActivityCreated(this)
        binding = ActivityTelawatClientBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        binding!!.home.setOnClickListener { onBackPressed() }
        db = Room.databaseBuilder(
            applicationContext, AppDatabase::class.java,
            "HidayaDB"
        ).createFromAsset("databases/HidayaDB.db").allowMainThreadQueries()
            .build()
        pref = PreferenceManager.getDefaultSharedPreferences(this)
        data
        retrieveState()
        mediaBrowser = MediaBrowserCompat(
            this, ComponentName(
                this,
                TelawatService::class.java
            ), connectionCallbacks, null
        ) // optional Bundle
        initViews()
        setListeners()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(Global.TAG, "In OnNewIntent")
        data
        sendPlayRequest() // Maybe this causes the problem of restarting when device unlocked
    }

    override fun onStart() {
        super.onStart()
        mediaBrowser!!.connect()
    }

    override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onStop() {
        super.onStop()
        if (MediaControllerCompat.getMediaController(this@TelawatClient) != null) {
            MediaControllerCompat.getMediaController(this@TelawatClient)
                .unregisterCallback(controllerCallback)
        }
        mediaBrowser!!.disconnect()
    }

    private val connectionCallbacks: MediaBrowserCompat.ConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            // Get the token for the MediaSession
            val token: MediaSessionCompat.Token = mediaBrowser!!.sessionToken

            // Create a MediaControllerCompat
            val mediaController = MediaControllerCompat(
                this@TelawatClient, token
            )

            // Save the controller
            MediaControllerCompat.setMediaController(this@TelawatClient, mediaController)

            // Finish building the UI
            buildTransportControls()
            data
            if (action != "back" &&
                (controller!!.playbackState.state == PlaybackStateCompat.STATE_NONE ||
                        mediaId != controller!!.metadata.getString(
                    MediaMetadataCompat.METADATA_KEY_MEDIA_ID
                ))
            ) sendPlayRequest()
        }

        override fun onConnectionSuspended() {
            Log.e(Global.TAG, "Connection suspended in TelawatClient")
            // The Service has crashed.
            // Disable transport controls until it automatically reconnects
            disableControls()
        }

        override fun onConnectionFailed() {
            Log.e(Global.TAG, "Connection failed in TelawatClient")
            // The Service has refused our connection
            disableControls()
        }
    }
    private val data: Unit
        get() {
            val intent: Intent = intent
            action = intent.action
            mediaId = intent.getStringExtra("media_id")
            reciterId = mediaId!!.substring(0, 3).toInt()
            versionId = mediaId!!.substring(3, 5).toInt()
            surahIndex = mediaId!!.substring(5).toInt()
            reciterName = db!!.telawatRecitersDao().getNames()[reciterId]
            val telawa: TelawatVersionsDB = db!!.telawatVersionsDao().getVersion(reciterId, versionId)
            version = RecitationVersion(
                versionId, telawa.getUrl(), telawa.getRewaya(),
                telawa.getCount(), telawa.getSuras(), null
            )
            surahNames = db!!.suarDao().getNames()
        }

    private fun retrieveState() {
        repeat = pref!!.getInt("telawat_repeat_mode", 0)
        shuffle = pref!!.getInt("telawat_shuffle_mode", 0)
    }

    private fun initViews() {
        surahNamescreen = binding!!.suraNamescreen
        seekBar = binding!!.seekbar
        durationScreen = binding!!.durationScreen
        progressScreen = binding!!.progressScreen
        playPause = binding!!.playPause
        nextBtn = binding!!.nextTrack
        prevBtn = binding!!.previousTrack
        forwardBtn = binding!!.fastForward
        rewindBtn = binding!!.rewind
        repeatBtn = binding!!.repeat
        shuffleBtn = binding!!.shuffle
        surahNamescreen!!.text = surahNames!![surahIndex]
        binding!!.reciterNamescreen.text = reciterName
        binding!!.versionNamescreen.text = version!!.getRewaya()
        if (repeat == PlaybackStateCompat.REPEAT_MODE_ONE) repeatBtn!!.background = ResourcesCompat.getDrawable(
            resources,
            R.drawable.rounded_dialog, theme
        )
        if (shuffle == PlaybackStateCompat.SHUFFLE_MODE_ALL) shuffleBtn!!.background =
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.rounded_dialog, theme
            )
    }

    private fun setListeners() {
        repeatBtn!!.setOnClickListener {
            if (repeat == PlaybackStateCompat.REPEAT_MODE_NONE) {
                repeat = PlaybackStateCompat.REPEAT_MODE_ONE
                tc!!.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE)
                val editor: SharedPreferences.Editor = pref!!.edit()
                editor.putInt("telawat_repeat_mode", repeat)
                editor.apply()
                repeatBtn!!.background = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.rounded_dialog, theme
                )
            } else if (repeat == PlaybackStateCompat.REPEAT_MODE_ONE) {
                repeat = PlaybackStateCompat.REPEAT_MODE_NONE
                tc!!.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE)
                val editor: SharedPreferences.Editor = pref!!.edit()
                editor.putInt("telawat_repeat_mode", repeat)
                editor.apply()
                repeatBtn!!.background = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ripple_circle, theme
                )
            }
        }
        shuffleBtn!!.setOnClickListener {
            if (shuffle == PlaybackStateCompat.SHUFFLE_MODE_NONE) {
                shuffle = PlaybackStateCompat.SHUFFLE_MODE_ALL
                tc!!.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL)
                val editor: SharedPreferences.Editor = pref!!.edit()
                editor.putInt("telawat_shuffle_mode", shuffle)
                editor.apply()
                shuffleBtn!!.background = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.rounded_dialog, theme
                )
            } else if (shuffle == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
                shuffle = PlaybackStateCompat.SHUFFLE_MODE_NONE
                tc!!.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE)
                val editor: SharedPreferences.Editor = pref!!.edit()
                editor.putInt("telawat_shuffle_mode", shuffle)
                editor.apply()
                shuffleBtn!!.background = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ripple_circle, theme
                )
            }
        }
    }

    private fun sendPlayRequest() {
        // Pass media data
        val bundle = Bundle()
        bundle.putString("play_type", action)
        bundle.putString("reciter_name", reciterName)
        bundle.putSerializable("version", version)

        // Start Playback
        tc!!.playFromMediaId(mediaId, bundle)
    }

    private fun updateButton(playing: Boolean) {
        if (playing) playPause!!.setImageDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_player_pause, theme
            )
        ) else playPause!!.setImageDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_player_play, theme
            )
        )
    }

    private fun enableControls() {
        // Attach a listeners to the buttons
        seekBar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) tc!!.seekTo(progress.toLong())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        playPause!!.setOnClickListener {
            // Since this is a play/pause button, you'll need to test the current state
            // and choose the action accordingly
            val pbState: Int = controller!!.playbackState.state
            if (pbState == PlaybackStateCompat.STATE_PLAYING) tc!!.pause() else tc!!.play()
        }
        nextBtn!!.setOnClickListener { tc!!.skipToNext() }
        prevBtn!!.setOnClickListener { tc!!.skipToPrevious() }
        forwardBtn!!.setOnClickListener { tc!!.fastForward() }
        rewindBtn!!.setOnClickListener { tc!!.rewind() }
    }

    private fun disableControls() {
        playPause!!.setOnClickListener(null)
        nextBtn!!.setOnClickListener(null)
        prevBtn!!.setOnClickListener(null)
        forwardBtn!!.setOnClickListener(null)
        rewindBtn!!.setOnClickListener(null)
        seekBar!!.setOnSeekBarChangeListener(null)
    }

    private fun buildTransportControls() {
        enableControls()
        controller = MediaControllerCompat.getMediaController(this@TelawatClient)
        tc = controller!!.transportControls

        // Display the initial state
        updateMetadata(controller!!.metadata)
        updatePbState(controller!!.playbackState)

        // Register a Callback to stay in sync
        controller!!.registerCallback(controllerCallback)
    }

    private var controllerCallback: MediaControllerCompat.Callback = object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            // To change the metadata inside the app when the user changes it from the notification
            updateMetadata(metadata)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            // To change the playback state inside the app when the user changes it
            // from the notification
            updatePbState(state)
        }

        override fun onSessionDestroyed() {
            mediaBrowser!!.disconnect()
        }
    }

    private fun updateMetadata(metadata: MediaMetadataCompat) {
        surahIndex = metadata.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER).toInt()
        surahNamescreen!!.text = surahNames!![surahIndex]
        val duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION).toInt()
        durationScreen!!.text = formatTime(duration)
        seekBar!!.max = duration
    }

    private fun updatePbState(state: PlaybackStateCompat) {
        seekBar!!.progress = state.position.toInt()
        progressScreen!!.text = formatTime(state.position.toInt())
        when (state.state) {
            PlaybackStateCompat.STATE_PLAYING -> {
                updateButton(true)
                progressScreen!!.text = formatTime(state.position.toInt())
            }
            PlaybackStateCompat.STATE_NONE -> {
                run { updateButton(false) }
            }
            PlaybackStateCompat.STATE_STOPPED -> {
                run { updateButton(false) }
            }
            PlaybackStateCompat.STATE_PAUSED -> {
                run { updateButton(false) }
            }
            PlaybackStateCompat.STATE_FAST_FORWARDING -> {
                run { updateButton(false) }
            }
            PlaybackStateCompat.STATE_REWINDING -> {
                run { updateButton(false) }
            }
            PlaybackStateCompat.STATE_BUFFERING -> {
                run { updateButton(false) }
            }
            PlaybackStateCompat.STATE_ERROR -> {
                run { updateButton(false) }
            }
            PlaybackStateCompat.STATE_CONNECTING -> {
                run { updateButton(false) }
            }
            PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS -> {
                run { updateButton(false) }
            }
            PlaybackStateCompat.STATE_SKIPPING_TO_NEXT -> {
                run { updateButton(false) }
            }
            PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM -> {
                updateButton(false)
            }
        }
    }

    private fun formatTime(time: Int): String {
        val hours = time / (60 * 60 * 1000) % 24
        val minutes = time / (60 * 1000) % 60
        val seconds = time / 1000 % 60
        var hms = String.format(
            Locale.US, "%02d:%02d:%02d",
            hours, minutes, seconds
        )
        if (hms.startsWith("0")) {
            hms = hms.substring(1)
            if (hms.startsWith("0")) hms = hms.substring(2)
        }
        return hms
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (isTaskRoot) {
            val intent = Intent(this, TelawatSuarCollectionActivity::class.java)
            intent.putExtra("reciter_id", reciterId)
            intent.putExtra("version_id", versionId)
            startActivity(intent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
        mediaBrowser = null
    }
}