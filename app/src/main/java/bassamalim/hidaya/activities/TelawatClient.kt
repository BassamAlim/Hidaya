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
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
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

    private lateinit var binding: ActivityTelawatClientBinding
    private lateinit var db: AppDatabase
    private lateinit var pref: SharedPreferences
    private lateinit var mediaBrowser: MediaBrowserCompat
    private lateinit var controller: MediaControllerCompat
    private lateinit var tc: MediaControllerCompat.TransportControls
    private lateinit var seekBar: SeekBar
    private lateinit var playPause: ImageButton
    private lateinit var repeatBtn: ImageButton
    private lateinit var shuffleBtn: ImageButton
    private lateinit var action: String
    private lateinit var mediaId: String
    private var reciterId = 0
    private var versionId = 0
    private var surahIndex = 0
    private lateinit var reciterName: String
    private lateinit var version: RecitationVersion
    private lateinit var surahNames: List<String>
    private var repeat = 0
    private var shuffle = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.myOnActivityCreated(this)
        binding = ActivityTelawatClientBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.home.setOnClickListener { onBackPressed() }

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "HidayaDB")
            .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build()

        pref = PreferenceManager.getDefaultSharedPreferences(this)

        data

        retrieveState()

        mediaBrowser = MediaBrowserCompat(
            this, ComponentName(this, TelawatService::class.java),
            connectionCallbacks, null
        )

        initViews()

        setListeners()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.i(Global.TAG, "In OnNewIntent")

        data

        sendPlayRequest() // Maybe this causes the problem of restarting when device unlocked
    }

    override fun onStart() {
        super.onStart()
        mediaBrowser.connect()
    }

    override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onStop() {
        Log.i(Global.TAG, "in onStop of TelawatClient")
        super.onStop()
        if (MediaControllerCompat.getMediaController(this@TelawatClient) != null) {
            MediaControllerCompat.getMediaController(this@TelawatClient)
                .unregisterCallback(controllerCallback)
        }
        mediaBrowser.disconnect()
    }

    private val connectionCallbacks: MediaBrowserCompat.ConnectionCallback = object :
        MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            // Get the token for the MediaSession
            val token: MediaSessionCompat.Token = mediaBrowser.sessionToken

            // Create a MediaControllerCompat
            val mediaController = MediaControllerCompat(this@TelawatClient, token)

            // Save the controller
            MediaControllerCompat.setMediaController(this@TelawatClient, mediaController)

            // Finish building the UI
            buildTransportControls()

            data

            if (action != "back" &&
                (controller.playbackState.state == PlaybackStateCompat.STATE_NONE ||
                        mediaId != controller.metadata.getString(
                    MediaMetadataCompat.METADATA_KEY_MEDIA_ID))
            )
                sendPlayRequest()
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
            action = intent.action!!
            mediaId = intent.getStringExtra("media_id")!!

            reciterId = mediaId.substring(0, 3).toInt()
            versionId = mediaId.substring(3, 5).toInt()
            surahIndex = mediaId.substring(5).toInt()

            reciterName = db.telawatRecitersDao().getNames()[reciterId]

            val telawa: TelawatVersionsDB =
                db.telawatVersionsDao().getVersion(reciterId, versionId)
            version = RecitationVersion(
                versionId, telawa.getUrl(), telawa.getRewaya(),
                telawa.getCount(), telawa.getSuras(), null
            )

            surahNames = db.suarDao().getNames()
        }

    private fun retrieveState() {
        repeat = pref.getInt("telawat_repeat_mode", 0)
        shuffle = pref.getInt("telawat_shuffle_mode", 0)
    }

    private fun initViews() {
        seekBar = binding.seekbar
        playPause = binding.playPause
        repeatBtn = binding.repeat
        shuffleBtn = binding.shuffle

        binding.suraNamescreen.text = surahNames[surahIndex]
        binding.reciterNamescreen.text = reciterName
        binding.versionNamescreen.text = version.getRewaya()

        if (repeat == PlaybackStateCompat.REPEAT_MODE_ONE)
            repeatBtn.background =
                ResourcesCompat.getDrawable(resources, R.drawable.rounded_dialog, theme)
        if (shuffle == PlaybackStateCompat.SHUFFLE_MODE_ALL)
            shuffleBtn.background =
                ResourcesCompat.getDrawable(resources, R.drawable.rounded_dialog, theme)
    }

    private fun setListeners() {
        repeatBtn.setOnClickListener {
            if (repeat == PlaybackStateCompat.REPEAT_MODE_NONE) {
                repeat = PlaybackStateCompat.REPEAT_MODE_ONE
                tc.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE)

                val editor: SharedPreferences.Editor = pref.edit()
                editor.putInt("telawat_repeat_mode", repeat)
                editor.apply()

                repeatBtn.background = ResourcesCompat.getDrawable(
                    resources, R.drawable.rounded_dialog, theme
                )
            }
            else if (repeat == PlaybackStateCompat.REPEAT_MODE_ONE) {
                repeat = PlaybackStateCompat.REPEAT_MODE_NONE
                tc.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE)

                val editor: SharedPreferences.Editor = pref.edit()
                editor.putInt("telawat_repeat_mode", repeat)
                editor.apply()

                repeatBtn.background = ResourcesCompat.getDrawable(
                    resources, R.drawable.ripple_circle, theme
                )
            }
        }

        shuffleBtn.setOnClickListener {
            if (shuffle == PlaybackStateCompat.SHUFFLE_MODE_NONE) {
                shuffle = PlaybackStateCompat.SHUFFLE_MODE_ALL
                tc.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL)

                val editor: SharedPreferences.Editor = pref.edit()
                editor.putInt("telawat_shuffle_mode", shuffle)
                editor.apply()

                shuffleBtn.background = ResourcesCompat.getDrawable(
                    resources, R.drawable.rounded_dialog, theme
                )
            }
            else if (shuffle == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
                shuffle = PlaybackStateCompat.SHUFFLE_MODE_NONE
                tc.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE)

                val editor: SharedPreferences.Editor = pref.edit()
                editor.putInt("telawat_shuffle_mode", shuffle)
                editor.apply()

                shuffleBtn.background = ResourcesCompat.getDrawable(
                    resources, R.drawable.ripple_circle, theme
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
        tc.playFromMediaId(mediaId, bundle)
    }

    private fun updateButton(state: Int) {
        when(state) {
            PlaybackStateCompat.STATE_PLAYING -> {
                binding.bufferingCircle.visibility = View.GONE
                playPause.visibility = View.VISIBLE
                playPause.setImageDrawable(
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_player_pause, theme)
                )
            }
            PlaybackStateCompat.STATE_PAUSED -> {
                binding.bufferingCircle.visibility = View.GONE
                playPause.visibility = View.VISIBLE
                playPause.setImageDrawable(
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_player_play, theme)
                )
            }
            PlaybackStateCompat.STATE_BUFFERING -> {
                playPause.visibility = View.GONE
                binding.bufferingCircle.visibility = View.VISIBLE
            }
        }
    }

    private fun enableControls() {
        // Attach a listeners to the buttons
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) tc.seekTo(progress.toLong())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        playPause.setOnClickListener {
            // Since this is a play/pause button, you'll need to test the current state
            // and choose the action accordingly
            val pbState: Int = controller.playbackState.state

            if (pbState == PlaybackStateCompat.STATE_PLAYING) tc.pause()
            else tc.play()
        }

        binding.nextTrack.setOnClickListener { tc.skipToNext() }
        binding.previousTrack.setOnClickListener { tc.skipToPrevious() }
        binding.fastForward.setOnClickListener { tc.fastForward() }
        binding.rewind.setOnClickListener { tc.rewind() }
    }

    private fun disableControls() {
        playPause.setOnClickListener(null)
        binding.nextTrack.setOnClickListener(null)
        binding.previousTrack.setOnClickListener(null)
        binding.fastForward.setOnClickListener(null)
        binding.rewind.setOnClickListener(null)
        seekBar.setOnSeekBarChangeListener(null)
    }

    private fun buildTransportControls() {
        enableControls()

        controller = MediaControllerCompat.getMediaController(this@TelawatClient)
        tc = controller.transportControls

        // Display the initial state
        updateMetadata(controller.metadata)
        updatePbState(controller.playbackState)

        // Register a Callback to stay in sync
        controller.registerCallback(controllerCallback)
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
            mediaBrowser.disconnect()
        }
    }

    private fun updateMetadata(metadata: MediaMetadataCompat) {
        surahIndex = metadata.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER).toInt()
        binding.suraNamescreen.text = surahNames[surahIndex]

        val duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION).toInt()
        binding.durationScreen.text = formatTime(duration)
        seekBar.max = duration
    }

    private fun updatePbState(state: PlaybackStateCompat) {
        seekBar.progress = state.position.toInt()
        seekBar.secondaryProgress = state.bufferedPosition.toInt()
        binding.progressScreen.text = formatTime(state.position.toInt())

        updateButton(state.state)

        if (state.state == PlaybackStateCompat.STATE_PLAYING)
            binding.progressScreen.text = formatTime(state.position.toInt())
    }

    private fun formatTime(time: Int): String {
        val hours = time / (60 * 60 * 1000) % 24
        val minutes = time / (60 * 1000) % 60
        val seconds = time / 1000 % 60
        var hms = String.format(
            Locale.US, "%02d:%02d:%02d", hours, minutes, seconds
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
            intent.putExtra("reciter_name", reciterName)
            intent.putExtra("version_id", versionId)
            startActivity(intent)
            finish()
        }
    }

    override fun onDestroy() {
        Log.i(Global.TAG, "in onDestroy of TelawatClient")
        super.onDestroy()
        mediaBrowser.disconnect()
    }

}