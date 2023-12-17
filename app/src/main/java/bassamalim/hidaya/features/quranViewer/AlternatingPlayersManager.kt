package bassamalim.hidaya.features.quranViewer

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnErrorListener
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.media3.common.util.UnstableApi
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.Prefs
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.database.dbs.AyatDB
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.utils.PrefUtils
import java.util.Locale
import kotlin.math.roundToInt

// TODO: handle repeat

@UnstableApi
@RequiresApi(Build.VERSION_CODES.O)
class AlternatingPlayersManager(
    private val ctx: Context,
    private val sp: SharedPreferences,
    private val db: AppDatabase,
    private val callback: PlayerCallback
) : OnPreparedListener, OnCompletionListener, OnErrorListener {

    private val NUMBER_OF_PLAYERS = 2
    private val aps = Array(NUMBER_OF_PLAYERS) { AlternatePlayer(MediaPlayer()) }
    private var playerIdx = 0
    private val ayat = db.ayatDao().getAll()
    var ayaIdx = -1
    private var isPaused = false

    init {
        aps.map { ap ->
            ap.mp.setOnPreparedListener(this)
            ap.mp.setOnCompletionListener(this)
            ap.mp.setOnErrorListener(this)

            ap.mp.setWakeMode(ctx, PowerManager.PARTIAL_WAKE_LOCK)
        }
    }

    override fun onPrepared(mp: MediaPlayer) {
        Log.d(Global.TAG, "in onPrepared")

        val currentPlayerIdx = idx(mp)
        val nxtPlayerIdx = nxtIdx(mp)

        aps[currentPlayerIdx].state = PlayerState.PREPARED

        if (aps[nxtPlayerIdx].state == PlayerState.NONE
            || aps[nxtPlayerIdx].state == PlayerState.COMPLETED) {
            play(playerIdx = currentPlayerIdx, ayaIdx = aps[currentPlayerIdx].ayaIdx)

            if (shouldStop(aps[currentPlayerIdx].ayaIdx + 1))
                aps[nxtPlayerIdx].state = PlayerState.STOPPED
            else
                prepare(  // prepare next
                    ap = nxt(currentPlayerIdx),
                    ayaIdx = aps[currentPlayerIdx].ayaIdx + 1
                )
        }
    }

    override fun onCompletion(mp: MediaPlayer) {
        Log.d(Global.TAG, "in onCompletion")

        val currentPlayerIdx = idx(mp)
        val nxtPlayerIdx = nxtIdx(mp)

        aps[currentPlayerIdx].state = PlayerState.COMPLETED
        aps[currentPlayerIdx].repeated++

        if (shouldRepeat(currentPlayerIdx))
            aps[currentPlayerIdx].mp.start()
        else {
            when (aps[nxtPlayerIdx].state) {
                PlayerState.PREPARED -> {
                    play(playerIdx = nxtPlayerIdx, ayaIdx = aps[nxtPlayerIdx].ayaIdx)

                    if (shouldStop(aps[currentPlayerIdx].ayaIdx + NUMBER_OF_PLAYERS))
                        aps[currentPlayerIdx].state = PlayerState.STOPPED
                    else
                        prepare(
                            ap = currentPlayerIdx,
                            ayaIdx = aps[currentPlayerIdx].ayaIdx + NUMBER_OF_PLAYERS
                        )
                }
                PlayerState.STOPPED -> {  // finished
                    callback.updatePbState(PlaybackStateCompat.STATE_STOPPED)

                    reset()
                }
                else -> {}
            }
        }
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        Log.d(Global.TAG, "in onError")

        Toast.makeText(
            ctx,
            ctx.getString(R.string.error_fetching_data),
            Toast.LENGTH_SHORT
        ).show()

        reset()

        callback.updatePbState(PlaybackStateCompat.STATE_STOPPED)

        return true
    }

    fun playFromMediaId(ayaIdx: Int) {
        Log.d(Global.TAG, "in playFromMediaId in AlternatingPlayersManager with ayaIdx: $ayaIdx")

        if (ayaIdx != this.ayaIdx) playNew(ayaIdx)
        else if (isPaused) resume()

        callback.updatePbState(PlaybackStateCompat.STATE_PLAYING)
    }

    fun resume() {
        aps[playerIdx].mp.start()

        callback.updatePbState(PlaybackStateCompat.STATE_PLAYING)
    }

    fun pause() {
        aps[playerIdx].mp.pause()

        isPaused = true
        callback.updatePbState(PlaybackStateCompat.STATE_PAUSED)
    }

    fun seekTo(pos: Long) {
        aps[playerIdx].mp.seekTo(pos.toInt())
    }

    fun previousAya() {
        if (ayaIdx > 0)
            playNew(ayaIdx - 1)
    }

    fun nextAya() {
        if (ayaIdx < ayat.size - 1)
            playNew(ayaIdx + 1)
    }

    fun getCurrentPosition() = aps[playerIdx].mp.currentPosition

    fun getDuration() = aps[playerIdx].mp.duration

    private fun reset() {
        aps.map { ap ->
            ap.mp.reset()
            ap.state = PlayerState.NONE
        }
    }

    fun isNotInitialized(): Boolean {
        return !aps.map { ap ->
            ap.state == PlayerState.NONE
        }.contains(false)
    }

    fun release() {
        aps.map { ap ->
            ap.mp.stop()
            ap.mp.release()
            ap.state = PlayerState.NONE
        }

        callback.updatePbState(PlaybackStateCompat.STATE_STOPPED)
    }

    fun setAudioAttributes(audioAttributes: AudioAttributes) {
        aps.map { ap ->
            ap.mp.setAudioAttributes(audioAttributes)
        }
    }

    fun setVolume(volume: Float) {
        aps.map { ap ->
            ap.mp.setVolume(volume, volume)
        }
    }

    private fun playNew(ayaIdx: Int) {
        this.ayaIdx = ayaIdx

        reset()

        prepare(ap = 0, ayaIdx = ayaIdx)  // prepare first
    }

    private fun play(playerIdx: Int, ayaIdx: Int) {
        aps[playerIdx].state = PlayerState.PLAYING
        this.playerIdx = playerIdx
        this.ayaIdx = ayaIdx
        aps[playerIdx].mp.start()
        callback.track(ayaId = ayaIdx+1)
    }

    private fun prepare(ap: Int, ayaIdx: Int) {
        aps[ap].state = PlayerState.PREPARING
        aps[ap].ayaIdx = ayaIdx

        val uri: Uri
        try {
            aps[ap].mp.reset()
            uri = getUri(ayat[ayaIdx])
            aps[ap].mp.setDataSource(ctx, uri)
            aps[ap].mp.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(Global.TAG, "Reciter not found in ayat telawa")
        }
    }

    private fun getRepeat(): Int {
        val repeat = PrefUtils.getFloat(sp, Prefs.AyaRepeat).roundToInt()
        return if (repeat == 11) Int.MAX_VALUE else repeat
    }

    private fun shouldRepeat(playerIdx: Int): Boolean {
        return aps[playerIdx].repeated < getRepeat()
    }

    private fun shouldStop(targetAya: Int): Boolean {
        return targetAya >= ayat.size
                || (PrefUtils.getBoolean(sp, Prefs.StopOnSuraEnd)
                && ayat[targetAya].suraNum != ayat[targetAya].suraNum)
                || (PrefUtils.getBoolean(sp, Prefs.StopOnPageEnd)
                && ayat[targetAya].page != ayat[targetAya].page)
    }

    private fun getUri(aya: AyatDB): Uri {
        val choice = PrefUtils.getString(sp, Prefs.AyaReciter).toInt()
        val sources = db.ayatTelawaDao().getReciter(choice)

        var uri = "https://www.everyayah.com/data/"
        uri += sources[0].source
        uri += String.format(Locale.US, "%03d%03d.mp3", aya.suraNum, aya.ayaNum)

        return Uri.parse(uri)
    }

    private fun nxt(i: Int) = (i + 1) % NUMBER_OF_PLAYERS
    private fun idx(mp: MediaPlayer) = aps.indexOf(aps.find { ap -> ap.mp == mp })
    private fun nxtIdx(mp: MediaPlayer) = nxt(idx(mp))

}