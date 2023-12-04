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

@UnstableApi
@RequiresApi(Build.VERSION_CODES.O)
class AlternatingPlayersManager(
    private val ctx: Context,
    private val sp: SharedPreferences,
    private val db: AppDatabase,
    private val callback: AyaPlayerService.PlayerCallback
) : OnPreparedListener, OnCompletionListener, OnErrorListener {

    private val aps = arrayOf(AlternatePlayer(MediaPlayer()), AlternatePlayer(MediaPlayer()))
    private var player = 0
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

        val idx = idx(mp)
        val oIdx = oIdx(mp)

        aps[idx].state = PlayerState.PREPARED

        if (aps[oIdx].state == PlayerState.NONE || aps[oIdx].state == PlayerState.COMPLETED) {
            aps[idx].state = PlayerState.PLAYING
            player = idx
            aps[idx].mp.start()
            callback.track(ayaId = aps[idx].ayaIdx+1)

            prepareNext(idx)
        }
    }

    override fun onCompletion(mp: MediaPlayer) {
        Log.d(Global.TAG, "in onCompletion")

        val idx = idx(mp)
        val oIdx = oIdx(mp)

        aps[idx].state = PlayerState.COMPLETED

        if (aps[oIdx].state == PlayerState.PREPARED) {
            aps[oIdx].state = PlayerState.PLAYING
            player = oIdx
            aps[oIdx].mp.start()
            callback.track(ayaId = aps[idx].ayaIdx+2)  // +1 for next aya, and +1 for idx to id

            prepareNext(oIdx)
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

        callback.setPbState(PlaybackStateCompat.STATE_STOPPED)

        return true
    }

    fun playFromMediaId(ayaIdx: Int) {
        if (ayaIdx != this.ayaIdx) playNew(ayaIdx)
        else if (isPaused) resume()

        callback.setPbState(PlaybackStateCompat.STATE_PLAYING)
    }

    fun play() {
        resume()

        callback.setPbState(PlaybackStateCompat.STATE_PLAYING)
    }

    fun pause() {
        aps[player].mp.pause()

        isPaused = true
        callback.setPbState(PlaybackStateCompat.STATE_PAUSED)
    }

    fun seekTo(pos: Long) {
        aps[player].mp.seekTo(pos.toInt())
    }

    fun previousAya() {
        if (ayaIdx > 0)
            playNew(ayaIdx - 1)
    }

    fun nextAya() {
        if (ayaIdx < ayat.size - 1)
            playNew(ayaIdx + 1)
    }

    fun getCurrentPosition() = aps[player].mp.currentPosition

    fun getDuration() = aps[player].mp.duration

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

        callback.setPbState(PlaybackStateCompat.STATE_STOPPED)
    }

    fun setAudioAttributes(audioAttributes: AudioAttributes) {
        aps.map { ap ->
            ap.mp.setAudioAttributes(audioAttributes)
        }
    }

    fun setIsLooping(isLooping: Boolean) {
        aps.map { ap ->
            ap.mp.isLooping = isLooping
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

        prepareFirst(ayaIdx)
    }

    private fun resume() {
        aps[player].mp.start()
    }

    private fun prepareFirst(ayaIdx: Int) {
        prepare(ap = 0, ayaIdx = ayaIdx)
    }

    private fun prepareNext(current: Int) {
        val currentAya = aps[current].ayaIdx

        if (shouldContinue(currentAya))
            prepare(ap = o(current), ayaIdx = currentAya + 1)
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

    private fun shouldContinue(currentAya: Int): Boolean {
        val newAya = currentAya + 1
        return newAya < ayat.size
                && aps[player].repeated < getRepeat()
                && !(PrefUtils.getBoolean(sp, Prefs.StopOnSuraEnd) && ayat[currentAya].suraNum != ayat[newAya].suraNum)
                && !(PrefUtils.getBoolean(sp, Prefs.StopOnPageEnd) && ayat[currentAya].page != ayat[newAya].page)
    }

    private fun getUri(aya: AyatDB): Uri {
        val choice = PrefUtils.getString(sp, Prefs.AyaReciter).toInt()
        val sources = db.ayatTelawaDao().getReciter(choice)

        var uri = "https://www.everyayah.com/data/"
        uri += sources[0].source
        uri += String.format(Locale.US, "%03d%03d.mp3", aya.suraNum, aya.ayaNum)

        return Uri.parse(uri)
    }

    private fun o(i: Int) = (i + 1) % 2

    private fun idx(mp: MediaPlayer): Int {
        return if (mp == aps[0].mp) 0
        else 1
    }

    private fun oIdx(mp: MediaPlayer): Int {
        return if (mp == aps[0].mp) 1
        else 0
    }

}