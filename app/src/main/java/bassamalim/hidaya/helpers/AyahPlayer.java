package bassamalim.hidaya.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;
import androidx.room.Room;

import java.util.List;
import java.util.Locale;

import bassamalim.hidaya.R;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.database.dbs.AyatTelawaDB;
import bassamalim.hidaya.enums.States;
import bassamalim.hidaya.models.Ayah;
import bassamalim.hidaya.other.Global;

public class AyahPlayer {

    private final Context context;
    private AppDatabase db;
    private SharedPreferences pref;
    private MediaPlayer[] players;
    private WifiManager.WifiLock wifiLock;
    private Ayah lastPlayed;
    private Ayah lastTracked;
    private boolean surahEnding;
    private Object what;
    private int allAyahsSize;
    private int currentPage;
    private int chosenSurah;
    private States state;
    private int lastPlayer;
    private boolean paused;
    private int repeated = 0;

    private Coordinator coordinator;
    public interface Coordinator {
        void onUiUpdate(States state);
        Ayah getAyah(int index);
        void nextPage();
    }
    public void setCoordinator(Coordinator listener) {
        coordinator = listener;
    }

    public AyahPlayer(Context context) {
        this.context = context;

        initiate();
        initPlayers();
    }

    private void initiate() {
        pref = PreferenceManager.getDefaultSharedPreferences(context);

        db = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class,
                "HidayaDB").createFromAsset("databases/HidayaDB.db").allowMainThreadQueries()
                .build();

        String theme = pref.getString(context.getString( R.string.theme_key), "ThemeM");

        if (theme.equals("ThemeM"))
            what = new ForegroundColorSpan(context.getResources().getColor(
                    R.color.track_M, context.getTheme()));
        else
            what = new ForegroundColorSpan(context.getResources().getColor(
                    R.color.track_L, context.getTheme()));
    }

    /**
     * Initialize the two media players and the wifi lock
     */
    private void initPlayers() {
        players = new MediaPlayer[]{new MediaPlayer(), new MediaPlayer()};

        players[0].setWakeMode(context.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        players[1].setWakeMode(context.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        wifiLock = ((WifiManager) context.getApplicationContext().getSystemService(
                Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "myLock");
        wifiLock.acquire();

        AudioAttributes attributes = new AudioAttributes.Builder().setContentType(AudioAttributes
                .CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build();

        players[0].setAudioAttributes(attributes);
        players[1].setAudioAttributes(attributes);

        setPlayersListeners();
    }

    /**
     * The function is responsible for setting the listeners for the two media players.
     * The first listener is responsible for preparing the next media player when the current one is
     * done playing.
     * The second listener is responsible for resetting the current media player when the next one
     * is prepared.
     * The third listener is responsible for handling errors.
     */
    private void setPlayersListeners() {
        for (int i = 0; i < 2; i++) {
            int finalI = i;

            players[i].setOnPreparedListener(mediaPlayer -> {
                if (players[n(finalI)].isPlaying())
                    players[n(finalI)].setNextMediaPlayer(players[finalI]);
                else if (state == States.Paused) {
                    if (allAyahsSize > lastPlayed.getIndex())
                        lastPlayed = coordinator.getAyah(lastPlayed.getIndex());
                }
                else {
                    players[finalI].start();
                    state = States.Playing;
                    track(lastPlayed);
                    if (allAyahsSize > lastPlayed.getIndex()+1)
                        preparePlayer(players[n(finalI)],
                                coordinator.getAyah(lastPlayed.getIndex()+1));
                }
            });

            players[i].setOnCompletionListener(mediaPlayer -> {
                int repeat = Integer.parseInt(pref.getString(context.getString(
                        R.string.aya_repeat_mode_key), "1"));
                if ((repeat == 1 || repeat == 2) && repeated < repeat) {
                    preparePlayer(players[finalI], lastPlayed);
                    players[n(finalI)].reset();
                    repeated++;
                }
                else if (repeat == 3) {
                    preparePlayer(players[finalI], lastPlayed);
                    players[n(finalI)].reset();
                }
                else {
                    repeated = 0;
                    if (paused) {
                        paused = false;
                        if (allAyahsSize > lastPlayed.getIndex()) {
                            Ayah newAyah = coordinator.getAyah(lastPlayed.getIndex()+1);
                            track(newAyah);
                            if (allAyahsSize > newAyah.getIndex())
                                preparePlayer(players[finalI],
                                        coordinator.getAyah(newAyah.getIndex()));
                            lastPlayed = newAyah;
                        }
                        else
                            ended();
                    }
                    else if (allAyahsSize > lastPlayed.getIndex()+1) {
                        Ayah newAyah = coordinator.getAyah(lastPlayed.getIndex()+1);
                        track(newAyah);
                        if (allAyahsSize > newAyah.getIndex()+1)
                            preparePlayer(players[finalI],
                                    coordinator.getAyah(newAyah.getIndex()+1));
                        lastPlayed = newAyah;
                    }
                    else
                        ended();
                }
            });

            players[i].setOnErrorListener((mp, what1, extra) -> {
                notFound();
                return true;
            });
        }
    }

    /**
     * The function's purpose is to prepare the first player to play the given `startAyah`.
     *
     * @param startAyah The ayah to start playing from.
     */
    public void requestPlay(Ayah startAyah) {
        lastPlayed = startAyah;
        state = States.Stopped;
        preparePlayer(players[0], startAyah);
    }

    /**
     * It checks if the user wants the player to stop on the end of the sura and the given aya is
     * from a new sura, meaning the sura is ending
     *  if so it checks the flag that says that there is no more ayas to prepare
     *      if so it stops playing
     *  if not it sets the flag to no more ayas
     *
     * if not it prepares the player by resetting it and setting the data source and calling
     * MediaPlayer's prepare()
     *
     * @param player The MediaPlayer object that will be used to play the audio.
     * @param ayah the ayah to play
     */
    private void preparePlayer(MediaPlayer player, Ayah ayah) {
        if (pref.getBoolean(context.getString(R.string.stop_on_sura_key), false) && ayah.getSurah() != chosenSurah) {
            if (surahEnding)
                stopPlaying();
            else
                surahEnding = true;
            return;
        }

        player.reset();

        Uri uri;
        try {
            uri = getUri(ayah, 0);
            player.setDataSource(context.getApplicationContext(), uri);
            player.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(Global.TAG, "Reciter not found in ayat telawa");
        }
    }

    /**
     * It takes an aya, and
     * tracks it by applying a span to it
     *
     * @param ayah the Ayah object that is being tracked
     */
    private void track(Ayah ayah) {
        if (lastTracked != null) {
            lastTracked.getSS().removeSpan(what);
            lastTracked.getScreen().setText(lastTracked.getSS());
        }
        ayah.getSS().setSpan(what, ayah.getStart(), ayah.getEnd(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ayah.getScreen().setText(ayah.getSS());    // heavy, but the only working way
        lastTracked = ayah;
    }

    /**
     * If the player is playing and there is a next ayah
     * prepare the next ayah and reset the other player
     */
    public void nextAyah() {
        if (state != States.Playing || lastPlayed.getIndex()+2 > allAyahsSize)
            return;

        lastPlayed = coordinator.getAyah(lastPlayed.getIndex()+1);
        track(lastPlayed);

        for (int i = 0; i < 2; i++) {
            if (players[i].isPlaying()) {
                preparePlayer(players[i], lastPlayed);
                players[n(i)].reset();
                break;
            }
        }
    }

    /**
     * If the player is playing and there is a previous ayah
     * prepare the next ayah and reset the other player
     */
    public void prevAyah() {
        if (state != States.Playing || lastPlayed.getIndex()-1 < 0)
            return;

        lastPlayed = coordinator.getAyah(lastPlayed.getIndex()-1);
        track(lastPlayed);

        for (int i = 0; i < 2; i++) {
            if (players[i].isPlaying()) {
                preparePlayer(players[i], lastPlayed);
                players[n(i)].reset();
                break;
            }
        }
    }

    /**
     * Pause the two players
     */
    public void pause() {
        paused = true;
        state = States.Paused;
        for (int i = 0; i < 2; i++) {
            if (players[i].isPlaying()) {
                Log.d(Global.TAG, "Paused " + i);
                players[i].pause();
                players[n(i)].reset();
                preparePlayer(players[n(i)], lastPlayed);
                lastPlayer = i;
            }
        }
    }

    public void resume() {
        Log.d(Global.TAG, "Resume P" + (lastPlayer));
        state = States.Playing;
        players[lastPlayer].start();
    }

    private void ended() {
        int QURAN_PAGES = 604;
        if (pref.getBoolean(context.getString(R.string.stop_on_page_key), false))
            stopPlaying();
        else if (currentPage < QURAN_PAGES && lastPlayed.getIndex()+1 == allAyahsSize) {
            coordinator.nextPage();
            requestPlay(coordinator.getAyah(0));
        }
    }

    public void stopPlaying() {
        state = States.Stopped;
        coordinator.onUiUpdate(States.Paused);

        for (int i = 0; i < 2; i++) {
            players[i].reset();
            players[i].release();
        }

        if (lastTracked != null) {
            lastTracked.getSS().removeSpan(what);
            lastTracked.getScreen().setText(lastTracked.getSS());
        }
    }

    private Uri getUri(Ayah ayah, int change) {
        int size = db.ayatTelawaDao().getSize();

        int choice = Integer.parseInt(pref.getString(
                context.getString(R.string.aya_reciter_key), "13"));
        List<AyatTelawaDB> sources = db.ayatTelawaDao().getReciter((choice + change) % (size-1));

        String uri = "https://www.everyayah.com/data/";
        uri += sources.get(0).getSource();
        uri += String.format(Locale.US, "%03d%03d.mp3", ayah.getSurah(), ayah.getAyah());

        return Uri.parse(uri);
    }

    private void notFound() {
        Toast.makeText(context, "لا يتوفر تسجيل الآية لهذا القارئ", Toast.LENGTH_SHORT).show();

        state = States.Stopped;
        coordinator.onUiUpdate(States.Paused);

        for (int i = 0; i < 2; i++)
            players[i].reset();

        if (lastTracked != null) {
            lastTracked.getSS().removeSpan(what);
            lastTracked.getScreen().setText(lastTracked.getSS());
        }

        getUri(lastPlayed, 1);
    }

    public Ayah getLastPlayed() {
        return lastPlayed;
    }

    public States getState() {
        return state;
    }

    public void setAllAyahsSize(int allAyahsSize) {
        this.allAyahsSize = allAyahsSize;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public void setChosenSurah(int chosenSurah) {
        this.chosenSurah = chosenSurah;
    }

    private int n(int i) {
        return (i + 1) % 2;
    }

    public void end() {
        for (int i = 0; i < 2; i++) {
            if (players[i] != null) {
                players[i].release();
                players[i] = null;
            }
        }
        if (wifiLock != null) {
            wifiLock.release();
            wifiLock = null;
        }
    }
}
