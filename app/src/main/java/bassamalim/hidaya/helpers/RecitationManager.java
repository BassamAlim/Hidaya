package bassamalim.hidaya.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.util.Log;

import androidx.preference.PreferenceManager;
import androidx.room.Room;

import java.io.IOException;
import java.util.List;

import bassamalim.hidaya.R;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.database.dbs.AyatTelawaDB;
import bassamalim.hidaya.enums.States;
import bassamalim.hidaya.models.Ayah;
import bassamalim.hidaya.other.Global;

public class RecitationManager {

    private final Context context;
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
    private AppDatabase db;

    private Coordinator coordinator;
    public interface Coordinator {
        void onUiUpdate(States state);
        Ayah getAyah(int index);
        void nextPage();
    }
    public void setCoordinator(Coordinator listener) {
        coordinator = listener;
    }

    public RecitationManager(Context context) {
        this.context = context;

        initiate();
        initPlayers();
    }

    private void initiate() {
        pref = PreferenceManager.getDefaultSharedPreferences(context);

        db = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class,
                "HidayaDB").createFromAsset("databases/HidayaDB.db").allowMainThreadQueries()
                .build();

        what = new BackgroundColorSpan(context.getResources().getColor(R.color.track));
    }

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

    private void setPlayersListeners() {
        MediaPlayer.OnErrorListener errorListener = (mediaPlayer, i, i1) -> {
            Log.e(Global.TAG, "Problem in players");
            return true;
        };

        for (int i = 0; i < 2; i++) {
            int finalI = i;
            players[i].setOnPreparedListener(mediaPlayer -> {
                Log.i(Global.TAG, "in p1 onPrepared");
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
                Log.i(Global.TAG, "in p1 onCompletion");
                if (paused) {
                    paused = false;
                    if (allAyahsSize > lastPlayed.getIndex()) {
                        Ayah newAyah = coordinator.getAyah(lastPlayed.getIndex()+1);
                        track(newAyah);
                        if (allAyahsSize > newAyah.getIndex())
                            preparePlayer(players[finalI], coordinator.getAyah(newAyah.getIndex()));
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
            });
            players[finalI].setOnErrorListener(errorListener);
        }
    }

    public void requestPlay(Ayah startAyah) {
        lastPlayed = startAyah;
        state = States.Stopped;
        preparePlayer(players[0], startAyah);
    }

    private void preparePlayer(MediaPlayer player, Ayah ayah) {
        if (pref.getBoolean("stop_on_surah", false) && ayah.getSurah() != chosenSurah) {
            if (surahEnding)
                stopPlaying();
            else
                surahEnding = true;
            return;
        }

        player.reset();
        try {
            Uri uri = getUri(ayah);
            player.setDataSource(context.getApplicationContext(), uri);

            for (int i = 0; i < 2; i++) {
                if (player == players[i])
                    Log.d(Global.TAG, uri + "  On P" + i);
            }
        } catch (IOException e) {e.printStackTrace();}
        player.prepareAsync();
    }

    private void track(Ayah subject) {
        if (lastTracked != null) {
            lastTracked.getSS().removeSpan(what);
            lastTracked.getScreen().setText(lastTracked.getSS());
        }
        subject.getSS().setSpan(what, subject.getStart(), subject.getEnd(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        subject.getScreen().setText(subject.getSS());    // heavy, but the only working way
        lastTracked = subject;
    }

    public void nextAyah() {
        if (state != States.Playing || lastPlayed.getIndex()+2 > allAyahsSize)
            return;

        lastPlayed = coordinator.getAyah(lastPlayed.getIndex()+1);
        for (int i = 0; i < 2; i++) {
            if (players[i].isPlaying()) {
                preparePlayer(players[i], lastPlayed);
                players[n(i)].reset();
                break;
            }
        }
    }

    public void prevAyah() {
        if (state != States.Playing || lastPlayed.getIndex()-1 < 0)
            return;

        lastPlayed = coordinator.getAyah(lastPlayed.getIndex()-1);
        for (int i = 0; i < 2; i++) {
            if (players[i].isPlaying()) {
                preparePlayer(players[i], lastPlayed);
                players[n(i)].reset();
                break;
            }
        }
    }

    public void pause() {
        for (int i = 0; i < 2; i++) {
            if (players[i].isPlaying()) {
                Log.d(Global.TAG, "Paused " + i);
                players[i].pause();
                players[n(i)].reset();
                preparePlayer(players[n(i)], lastPlayed);
                lastPlayer = i;
            }
        }
        paused = true;
        state = States.Paused;
    }

    public void resume() {
        Log.d(Global.TAG, "Resume P" + (lastPlayer));
        players[lastPlayer].start();
        state = States.Playing;
    }

    private void ended() {
        int QURAN_PAGES = 604;
        if (pref.getBoolean("stop_on_page", false))
            stopPlaying();
        else if (currentPage < QURAN_PAGES && lastPlayed.getIndex()+1 == allAyahsSize) {
            coordinator.nextPage();
            requestPlay(coordinator.getAyah(0));
        }
    }

    public void stopPlaying() {
        for (int i = 0; i < 2; i++) {
            players[i].reset();
            players[i].release();
        }

        lastTracked.getSS().removeSpan(what);
        lastTracked.getScreen().setText(lastTracked.getSS());

        state = States.Stopped;
        coordinator.onUiUpdate(States.Paused);
    }

    private Uri getUri(Ayah ayah) {
        int choice = pref.getInt("chosen_reciter", 13);
        List<AyatTelawaDB> sources = db.ayatTelawaDao().getReciter(choice);

        String url = "https://www.everyayah.com/data/";
        url += sources.get(0).getSource();
        url += format(ayah.getSurah()) + format(ayah.getAyah()) + ".mp3";

        return Uri.parse(url);
    }

    private String format(int in) {
        String strIn = String.valueOf(in);
        String out = "";
        if (strIn.length() == 1)
            out += "00";
        else if (strIn.length() == 2)
            out += "0";
        out += strIn;
        return out;
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

        if (wifiLock != null)
            wifiLock.release();
    }

}
