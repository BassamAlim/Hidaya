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

import org.json.JSONArray;

import java.io.IOException;
import java.util.List;

import bassamalim.hidaya.R;
import bassamalim.hidaya.enums.States;
import bassamalim.hidaya.models.Ayah;
import bassamalim.hidaya.models.AyatTelawaDB;
import bassamalim.hidaya.other.AppDatabase;
import bassamalim.hidaya.other.Constants;

public class RecitationManager {

    private final Context context;
    private SharedPreferences pref;
    private MediaPlayer player1;
    private MediaPlayer player2;
    private WifiManager.WifiLock wifiLock;
    private Ayah lastPlayed;
    private Ayah lastTracked;
    private boolean surahEnding;
    private Object what;
    private int allAyahsSize;
    private JSONArray recitationsArr;
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
    }

    private void initiate() {
        pref = PreferenceManager.getDefaultSharedPreferences(context);

        db = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class,
                "HidayaDB").createFromAsset("databases/HidayaDB.db").allowMainThreadQueries()
                .build();

        player1 = new MediaPlayer();
        player2 = new MediaPlayer();

        what = new BackgroundColorSpan(context.getResources().getColor(R.color.track));
    }

    public void setPlayers(Ayah startAyah) {
        player1.setWakeMode(context.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player2.setWakeMode(context.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        //mediaPlayer.setLooping(true);

        wifiLock = ((WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "myLock");
        wifiLock.acquire();

        AudioAttributes attributes = new AudioAttributes.Builder().setContentType(AudioAttributes
                .CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build();
        player1.setAudioAttributes(attributes);
        player2.setAudioAttributes(attributes);

        setPlayersListeners();

        lastPlayed = startAyah;

        preparePlayer(player1, startAyah);
    }

    private void setPlayersListeners() {
        MediaPlayer.OnErrorListener errorListener = (mediaPlayer, i, i1) -> {
            Log.e(Constants.TAG, "Problem in players");
            return true;
        };

        player1.setOnPreparedListener(mediaPlayer -> {
            Log.i(Constants.TAG, "in p1 onPrepared");
            if (player2.isPlaying())
                player2.setNextMediaPlayer(player1);
            else if (state == States.Paused) {
                if (allAyahsSize > lastPlayed.getIndex())
                    lastPlayed = coordinator.getAyah(lastPlayed.getIndex());
            }
            else {
                player1.start();
                state = States.Playing;
                track(lastPlayed);
                if (allAyahsSize > lastPlayed.getIndex()+1)
                    preparePlayer(player2, coordinator.getAyah(lastPlayed.getIndex()+1));
            }
        });
        player1.setOnCompletionListener(mediaPlayer -> {
            Log.i(Constants.TAG, "in p1 onCompletion");
            if (paused) {
                paused = false;
                if (allAyahsSize > lastPlayed.getIndex()) {
                    Ayah newAyah = coordinator.getAyah(lastPlayed.getIndex()+1);
                    track(newAyah);
                    if (allAyahsSize > newAyah.getIndex())
                        preparePlayer(player1, coordinator.getAyah(newAyah.getIndex()));
                    lastPlayed = newAyah;
                }
                else
                    ended();
            }
            else if (allAyahsSize > lastPlayed.getIndex()+1) {
                Ayah newAyah = coordinator.getAyah(lastPlayed.getIndex()+1);
                track(newAyah);
                if (allAyahsSize > newAyah.getIndex()+1)
                    preparePlayer(player1, coordinator.getAyah(newAyah.getIndex()+1));
                lastPlayed = newAyah;
            }
            else
                ended();
        });
        player1.setOnErrorListener(errorListener);

        player2.setOnPreparedListener(mediaPlayer -> {
            Log.i(Constants.TAG, "in p2 onPrepared");
            if (player1.isPlaying()) {
                player1.setNextMediaPlayer(player2);
            }
            else if (state == States.Paused) {
                if (allAyahsSize > lastPlayed.getIndex())
                    lastPlayed = coordinator.getAyah(lastPlayed.getIndex());
            }
            else {
                player2.start();
                state = States.Playing;
                track(lastPlayed);
                if (allAyahsSize > lastPlayed.getIndex()+1)
                    preparePlayer(player1, coordinator.getAyah(lastPlayed.getIndex() + 1));
            }
        });
        player2.setOnCompletionListener(mediaPlayer -> {
            Log.i(Constants.TAG, "in p2 onCompletion");
            if (paused) {
                if (allAyahsSize > lastPlayed.getIndex()) {
                    paused = false;
                    Ayah newAyah = coordinator.getAyah(lastPlayed.getIndex());
                    track(newAyah);
                    if (allAyahsSize > newAyah.getIndex())
                        preparePlayer(player2, coordinator.getAyah(newAyah.getIndex()));
                    lastPlayed = newAyah;
                }
                else
                    ended();
            }
            else if (allAyahsSize > lastPlayed.getIndex()+1) {
                Ayah newAyah = coordinator.getAyah(lastPlayed.getIndex()+1);
                track(newAyah);
                if (allAyahsSize > newAyah.getIndex()+1)
                    preparePlayer(player2, coordinator.getAyah(newAyah.getIndex()+1));
                lastPlayed = newAyah;
            }
            else
                ended();

        });
        player2.setOnErrorListener(errorListener);
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
            if (player == player1)
                Log.i(Constants.TAG, uri + "  On P1");
            else
                Log.i(Constants.TAG, uri + "  On P2");
            player.setDataSource(context.getApplicationContext(), uri);
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
        if (state != States.Playing || lastPlayed.getIndex()+1 > allAyahsSize)
            return;

        lastPlayed = coordinator.getAyah(lastPlayed.getIndex()+1);
        if (player1.isPlaying()) {
            preparePlayer(player1, lastPlayed);
            player2.reset();
        }
        else  {
            preparePlayer(player2, lastPlayed);
            player1.reset();
        }
    }

    public void prevAyah() {
        if (state != States.Playing || lastPlayed.getIndex()-1 < 0)
            return;

        lastPlayed = coordinator.getAyah(lastPlayed.getIndex()-1);
        if (player1.isPlaying()) {
            preparePlayer(player1, lastPlayed);
            player2.reset();
        }
        else  {
            preparePlayer(player2, lastPlayed);
            player1.reset();
        }
    }

    public void pause() {
        if (player1.isPlaying()) {
            Log.i(Constants.TAG, "paused1");
            player1.pause();
            player2.reset();
            preparePlayer(player2, lastPlayed);
            lastPlayer = 1;
        }
        else if (player2.isPlaying()) {
            Log.i(Constants.TAG, "paused2");
            player2.pause();
            player1.reset();
            preparePlayer(player1, lastPlayed);
            lastPlayer = 2;
        }

        paused = true;
        state = States.Paused;
    }

    public void resume() {
        if (lastPlayer == 1) {
            Log.i(Constants.TAG, "Resume P1");
            player1.start();
        }
        else {
            Log.i(Constants.TAG, "Resume P2");
            player2.start();
        }

        state = States.Playing;
    }

    private void ended() {
        int QURAN_PAGES = 604;
        if (pref.getBoolean("stop_on_page", false))
            stopPlaying();
        else if (currentPage < QURAN_PAGES && lastPlayed.getIndex()+1 == allAyahsSize) {
            coordinator.nextPage();
            setPlayers(coordinator.getAyah(0));
        }
    }

    public void stopPlaying() {
        player1.reset();
        player2.reset();

        lastTracked.getSS().removeSpan(what);
        lastTracked.getScreen().setText(lastTracked.getSS());

        player1.release();
        player2.release();
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

    public void end() {
        if (player1 != null) {
            player1.release();
            player1 = null;
        }
        if (player2 != null) {
            player2.release();
            player2 = null;
        }
        if (wifiLock != null)
            wifiLock.release();
    }

}
