package bassamalim.hidaya.helpers

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.PowerManager
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager
import androidx.room.Room
import bassamalim.hidaya.R
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.database.dbs.AyatTelawaDB
import bassamalim.hidaya.enums.States
import bassamalim.hidaya.models.Ayah
import bassamalim.hidaya.other.Global
import java.util.*

class AyahPlayer(private val context: Context) {

    private var db: AppDatabase? = null
    private var pref: SharedPreferences? = null
    private var players: Array<MediaPlayer?>? = null
    private var wifiLock: WifiManager.WifiLock? = null
    private var lastPlayed: Ayah? = null
    private var lastTracked: Ayah? = null
    private var surahEnding = false
    private var viewType: String? = null
    private var what: Any? = null
    private var allAyahsSize = 0
    private var currentPage = 0
    private var chosenSurah = 0
    private var state: States? = null
    private var lastPlayer = 0
    private var paused = false
    private var repeated = 1
    private var on = false
    private var coordinator: Coordinator? = null

    interface Coordinator {
        fun onUiUpdate(state: States)
        fun getAyah(index: Int): Ayah
        fun nextPage()
    }

    fun setCoordinator(listener: Coordinator?) {
        coordinator = listener
    }

    private fun initiate() {
        pref = PreferenceManager.getDefaultSharedPreferences(context)
        db = Room.databaseBuilder(
            context.applicationContext, AppDatabase::class.java,
            "HidayaDB"
        ).createFromAsset("databases/HidayaDB.db").allowMainThreadQueries()
            .build()
        val theme: String? = pref!!.getString(
            context.getString(R.string.theme_key),
            context.getString(R.string.default_theme)
        )
        what = if (theme == "ThemeL") ForegroundColorSpan(
            context.resources.getColor(
                R.color.track_L, context.theme
            )
        ) else ForegroundColorSpan(
            context.resources.getColor(
                R.color.track_M, context.theme
            )
        )
    }

    /**
     * The function's purpose is to prepare the first player to play the given `startAyah`.
     *
     * @param startAyah The ayah to start playing from.
     */
    fun requestPlay(startAyah: Ayah?) {
        if (!on) initPlayers()
        repeated = 1
        lastPlayed = startAyah
        state = States.Stopped
        preparePlayer(players!![0], startAyah)
    }

    /**
     * Initialize the two media players and the wifi lock
     */
    private fun initPlayers() {
        players = arrayOf<MediaPlayer?>(MediaPlayer(), MediaPlayer())
        players!![0]!!.setWakeMode(context.applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        players!![1]!!.setWakeMode(context.applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        wifiLock = (context.applicationContext.getSystemService(
            Context.WIFI_SERVICE
        ) as WifiManager).createWifiLock(WifiManager.WIFI_MODE_FULL, "myLock")
        wifiLock!!.acquire()
        val attributes: AudioAttributes =
            AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA).build()
        players!![0]!!.setAudioAttributes(attributes)
        players!![1]!!.setAudioAttributes(attributes)
        setPlayersListeners()
        on = true
    }

    /**
     * The function is responsible for setting the listeners for the two media players.
     * The first listener is responsible for preparing the next media player when the current one is
     * done playing.
     * The second listener is responsible for resetting the current media player when the next one
     * is prepared.
     * The third listener is responsible for handling errors.
     */
    private fun setPlayersListeners() {
        for (i in 0..1) {
            players!![i]!!.setOnPreparedListener {
                if (players!![n(i)]!!.isPlaying) players!![n(i)]!!.setNextMediaPlayer(
                    players!![i]
                ) else if (state == States.Paused) {
                    if (allAyahsSize > lastPlayed!!.getIndex()) lastPlayed =
                        coordinator!!.getAyah(lastPlayed!!.getIndex())
                } else {
                    players!![i]!!.start()
                    state = States.Playing
                    track(lastPlayed)
                    if (allAyahsSize > lastPlayed!!.getIndex() + 1) preparePlayer(
                        players!![n(i)],
                        coordinator!!.getAyah(lastPlayed!!.getIndex() + 1)
                    )
                }
            }
            players!![i]!!.setOnCompletionListener {
                val repeat: Int = pref!!.getString(
                    context.getString(
                        R.string.aya_repeat_mode_key
                    ), "1"
                )!!.toInt()
                if ((repeat == 2 || repeat == 3 || repeat == 5 || repeat == 10)
                    && repeated < repeat
                ) {
                    preparePlayer(players!![i], lastPlayed)
                    players!![n(i)]!!.reset()
                    repeated++
                } else if (repeat == 0) {
                    repeated = 0
                    preparePlayer(players!![i], lastPlayed)
                    players!![n(i)]!!.reset()
                } else {
                    repeated = 1
                    if (paused) {
                        paused = false
                        if (allAyahsSize > lastPlayed!!.getIndex()) {
                            val newAyah: Ayah = coordinator!!.getAyah(lastPlayed!!.getIndex() + 1)
                            track(newAyah)
                            if (allAyahsSize > newAyah.getIndex()) preparePlayer(
                                players!![i],
                                coordinator!!.getAyah(newAyah.getIndex())
                            )
                            lastPlayed = newAyah
                        } else ended()
                    } else if (allAyahsSize > lastPlayed!!.getIndex() + 1) {
                        val newAyah: Ayah = coordinator!!.getAyah(lastPlayed!!.getIndex() + 1)
                        track(newAyah)
                        if (allAyahsSize > newAyah.getIndex() + 1) preparePlayer(
                            players!![i],
                            coordinator!!.getAyah(newAyah.getIndex() + 1)
                        )
                        lastPlayed = newAyah
                    } else ended()
                }
            }
            players!![i]!!.setOnErrorListener { _: MediaPlayer?, _: Int, _: Int ->
                notFound()
                true
            }
        }
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
    private fun preparePlayer(player: MediaPlayer?, ayah: Ayah?) {
        if (pref!!.getBoolean(context.getString(R.string.stop_on_sura_key), false)
            && ayah!!.getSurahNum() != chosenSurah
        ) {
            if (surahEnding) stopPlaying() else surahEnding = true
            return
        }
        player!!.reset()
        val uri: Uri
        try {
            uri = getUri(ayah, 0)
            player.setDataSource(context.applicationContext, uri)
            player.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(Global.TAG, "Reciter not found in ayat telawa")
        }
    }

    /**
     * It takes an aya, and
     * tracks it by applying a span to it
     *
     * @param ayah the Ayah object that is being tracked
     */
    private fun track(ayah: Ayah?) {
        if (lastTracked != null) {
            lastTracked!!.getSS()!!.removeSpan(what)
            lastTracked!!.getScreen()!!.text = lastTracked!!.getSS()
        }
        if (viewType == "list") ayah!!.getSS()!!.setSpan(
            what, 0, ayah.getText()!!.length - 1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        ) else ayah!!.getSS()!!.setSpan(
            what, ayah.getStart(), ayah.getEnd(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ayah.getScreen()!!.text = ayah.getSS() // heavy, but the only working way
        lastTracked = ayah
    }

    /**
     * If the player is playing and there is a next ayah
     * prepare the next ayah and reset the other player
     */
    fun nextAyah() {
        if (state != States.Playing || lastPlayed!!.getIndex() + 2 > allAyahsSize) return
        lastPlayed = coordinator!!.getAyah(lastPlayed!!.getIndex() + 1)
        track(lastPlayed)
        for (i in 0..1) {
            if (players!![i]!!.isPlaying) {
                preparePlayer(players!![i], lastPlayed)
                players!![n(i)]!!.reset()
                break
            }
        }
    }

    /**
     * If the player is playing and there is a previous ayah
     * prepare the next ayah and reset the other player
     */
    fun prevAyah() {
        if (state != States.Playing || lastPlayed!!.getIndex() - 1 < 0) return
        lastPlayed = coordinator!!.getAyah(lastPlayed!!.getIndex() - 1)
        track(lastPlayed)
        for (i in 0..1) {
            if (players!![i]!!.isPlaying) {
                preparePlayer(players!![i], lastPlayed)
                players!![n(i)]!!.reset()
                break
            }
        }
    }

    /**
     * Pause the two players
     */
    fun pause() {
        paused = true
        state = States.Paused
        for (i in 0..1) {
            if (players!![i]!!.isPlaying()) {
                Log.d(Global.TAG, "Paused $i")
                players!![i]!!.pause()
                players!![n(i)]!!.reset()
                preparePlayer(players!![n(i)], lastPlayed)
                lastPlayer = i
            }
        }
    }

    /**
     * Resume the last player that was playing.
     */
    fun resume() {
        Log.d(Global.TAG, "Resume P$lastPlayer")
        state = States.Playing
        players!![lastPlayer]!!.start()
    }

    private fun ended() {
        val QURAN_PAGES = 604
        if (pref!!.getBoolean(
                context.getString(R.string.stop_on_page_key),
                false
            )
        ) stopPlaying() else if (currentPage < QURAN_PAGES && lastPlayed!!.getIndex() + 1 == allAyahsSize) {
            coordinator!!.nextPage()
            requestPlay(coordinator!!.getAyah(0))
        }
    }

    fun stopPlaying() {
        state = States.Stopped
        coordinator!!.onUiUpdate(States.Paused)
        for (i in 0..1) {
            players!![i]!!.reset()
            players!![i]!!.release()
        }
        on = false
        if (lastTracked != null) {
            lastTracked!!.getSS()!!.removeSpan(what)
            lastTracked!!.getScreen()!!.text = lastTracked!!.getSS()
        }
    }

    private fun getUri(ayah: Ayah?, change: Int): Uri {
        val size: Int = db!!.ayatTelawaDao()!!.size
        val choice: Int = pref!!.getString(
            context.getString(R.string.aya_reciter_key), "13"
        )!!.toInt()
        val sources: List<AyatTelawaDB?>? =
            db!!.ayatTelawaDao()!!.getReciter((choice + change) % (size - 1))
        var uri = "https://www.everyayah.com/data/"
        uri += sources!![0]!!.getSource()
        uri += String.format(Locale.US, "%03d%03d.mp3", ayah!!.getSurahNum(), ayah.getAyahNum())
        return Uri.parse(uri)
    }

    private fun notFound() {
        Toast.makeText(
            context, context.getString(R.string.recitation_not_available),
            Toast.LENGTH_SHORT
        ).show()
        state = States.Stopped
        coordinator!!.onUiUpdate(States.Paused)
        for (i in 0..1) players!![i]!!.reset()
        if (lastTracked != null) {
            lastTracked!!.getSS()!!.removeSpan(what)
            lastTracked!!.getScreen()!!.text = lastTracked!!.getSS()
        }
        getUri(lastPlayed, 1)
    }

    fun getLastPlayed(): Ayah? {
        return lastPlayed
    }

    fun getState(): States? {
        return state
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

    fun setViewType(viewType: String?) {
        this.viewType = viewType
    }

    private fun n(i: Int): Int {
        return (i + 1) % 2
    }

    fun finish() {
        if (players != null) {
            for (i in 0..1) {
                if (players!![i] != null) {
                    players!![i]!!.release()
                    players!![i] = null
                }
            }
            if (wifiLock != null) {
                wifiLock!!.release()
                wifiLock = null
            }
        }
    }

    init {
        initiate()
    }
}