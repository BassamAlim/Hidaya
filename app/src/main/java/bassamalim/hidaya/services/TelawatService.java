package bassamalim.hidaya.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import bassamalim.hidaya.R;
import bassamalim.hidaya.activities.TelawatClient;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.models.ReciterCard;
import bassamalim.hidaya.other.Global;

@RequiresApi(api = Build.VERSION_CODES.O)
public class TelawatService extends MediaBrowserServiceCompat implements
        AudioManager.OnAudioFocusChangeListener {

    private static final String MY_MEDIA_ROOT_ID = "media_root_id";
    private static final String MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id";
    private static final String ACTION_PLAY = "bassamalim.hidaya.services.TelawatService.play";
    private static final String ACTION_PAUSE = "bassamalim.hidaya.services.TelawatService.pause";
    private static final String ACTION_NEXT = "bassamalim.hidaya.services.TelawatService.next";
    private static final String ACTION_PREV = "bassamalim.hidaya.services.TelawatService.prev";
    private static final String ACTION_STOP = "bassamalim.hidaya.services.TelawatService.stop";
    private NotificationCompat.Action playAction;
    private NotificationCompat.Action pauseAction;
    private NotificationCompat.Action nextAction;
    private NotificationCompat.Action prevAction;
    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;
    private MediaControllerCompat controller;
    private MediaMetadataCompat mediaMetadata;
    private String channelId = "channel ID";
    private final int id = 333;
    private SharedPreferences pref;
    private AppDatabase db;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private Notification notification;
    private MediaPlayer player;
    private AudioManager am;
    private final IntentFilter intentFilter = new IntentFilter();
    private AudioFocusRequest audioFocusRequest;
    private WifiManager.WifiLock wifiLock;
    private ArrayList<String> surahNames;
    private String mediaId;
    private String reciterName;
    private String playType;
    private int reciterId;
    private int versionId;
    private int surahIndex;
    private ReciterCard.RecitationVersion version;
    private int shuffle;
    private int continueFrom;

    @Override
    public void onCreate() {
        super.onCreate();

        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "HidayaDB")
                .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build();

        getSuraNames();

        initSession();

        setupActions();

        initMediaSessionMetadata();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        return super.onStartCommand(intent, flags, startId);
    }

    final MediaSessionCompat.Callback callback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlayFromMediaId(String givenMediaId, Bundle extras) {
            Log.d(Global.TAG, "In onPlayFromMediaId of TelawatService");

            playType = extras.getString("play_type");
            if (!givenMediaId.equals(mediaId) || playType.equals("continue")) {
                Log.d(Global.TAG, "Old mediaID: " + mediaId +
                        ", New mediaId: " + givenMediaId);

                mediaId = givenMediaId;

                mediaSession.setSessionActivity(getContentIntent());

                reciterId = Integer.parseInt(givenMediaId.substring(0, 3));
                versionId = Integer.parseInt(givenMediaId.substring(3, 5));
                surahIndex = Integer.parseInt(givenMediaId.substring(5));
                reciterName = extras.getString("reciter_name");
                version = (ReciterCard.RecitationVersion) extras.getSerializable("version");

                if (playType.equals("continue"))
                    continueFrom = pref.getInt("last_telawa_progress", 0);

                buildNotification();
                initPlayer();

                if (controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_NONE)
                    onPlay();
                else
                    playOther();
            }
        }

        @Override
        public void onPlay() {
            Log.d(Global.TAG, "In onPlay of TelawatService");

            // Request audio focus for playback, this registers the afChangeListener
            int result = am.requestAudioFocus(audioFocusRequest);

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // Start the service
                startService(new Intent(getApplicationContext(), TelawatService.class));
                // Set the session active  (and update metadata and state)
                mediaSession.setActive(true);

                // start the player (custom call)
                if (controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED ||
                        controller.getPlaybackState().getState() ==
                                PlaybackStateCompat.STATE_STOPPED)
                    player.start();
                else
                    startPlaying(surahIndex);

                updatePbState(PlaybackStateCompat.STATE_PLAYING);
                updateNotification(true);
                refresh();

                // Register Receiver
                registerReceiver(receiver, intentFilter);
                // Put the service in the foreground, post notification
                startForeground(id, notification);
            }
        }

        @Override
        public void onStop() {
            Log.d(Global.TAG, "In onStop of TelawatService");

            // Abandon audio focus
            am.abandonAudioFocusRequest(audioFocusRequest);

            cleanUp();
            unregisterReceiver(receiver);
            handler.removeCallbacks(runnable);
            saveForLater(player.getCurrentPosition());
            // stop the player (custom call)
            player.stop();
            // Stop the service
            stopSelf();
            // Set the session inactive  (and update metadata and state)
            mediaSession.setActive(false);
            updatePbState(PlaybackStateCompat.STATE_STOPPED);

            // Take the service out of the foreground
            stopForeground(false);
        }

        @Override
        public void onPause() {
            Log.d(Global.TAG, "In onPause of TelawatService");

            // Update metadata and state
            updatePbState(PlaybackStateCompat.STATE_PAUSED);
            updateNotification(false);

            saveForLater(player.getCurrentPosition());

            // pause the player
            player.pause();
            handler.removeCallbacks(runnable);

            // Take the service out of the foreground, retain the notification
            stopForeground(false);
        }

        @Override
        public void onFastForward() {
            super.onFastForward();
            player.seekTo(player.getCurrentPosition()+10000);
            updatePbState(controller.getPlaybackState().getState());
        }

        @Override
        public void onRewind() {
            super.onRewind();
            player.seekTo(player.getCurrentPosition()-10000);
            updatePbState(controller.getPlaybackState().getState());
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            skipToNext();
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            skipToPrevious();
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            player.seekTo((int) pos);
            updatePbState(controller.getPlaybackState().getState());
        }

        @Override
        public void onSetRepeatMode(int repeatMode) {
            super.onSetRepeatMode(repeatMode);
            player.setLooping(repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE);
        }

        @Override
        public void onSetShuffleMode(int shuffleMode) {
            super.onSetShuffleMode(shuffleMode);
            shuffle = shuffleMode;
        }
    };

    private void playOther() {
        mediaSession.setActive(true);
        // start the player
        startPlaying(surahIndex);

        // Update state
        updatePbState(PlaybackStateCompat.STATE_PLAYING);
        updateNotification(true);
    }

    private void skipToNext() {
        int temp = surahIndex;
        if (shuffle == PlaybackStateCompat.SHUFFLE_MODE_NONE) {
            do {
                temp++;
            } while(temp < 114 && !version.getSuras().contains("," + (temp+1) + ","));
        }
        else if (shuffle == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
            Random random = new Random();
            do {
                temp = random.nextInt(114);
            } while(!version.getSuras().contains("," + (temp+1) + ","));
        }

        if (temp < 114) {
            surahIndex = temp;
            updateMetadata(false);
            updateNotification(true);
            startPlaying(surahIndex);
            updatePbState(PlaybackStateCompat.STATE_PLAYING);
        }
    }

    private void skipToPrevious() {
        int temp = surahIndex;
        if (shuffle == PlaybackStateCompat.SHUFFLE_MODE_NONE) {
            do {
                temp--;
            } while(temp >= 0 && !version.getSuras().contains("," + (temp+1) + ","));
        }
        else if (shuffle == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
            Random random = new Random();
            do {
                temp = random.nextInt(114);
            } while(!version.getSuras().contains("," + (temp+1) + ","));
        }

        if (temp >= 0) {
            surahIndex = temp;
            updateMetadata(false);
            updateNotification(true);
            startPlaying(surahIndex);
            updatePbState(PlaybackStateCompat.STATE_PLAYING);
        }
    }

    @Override
    public void onAudioFocusChange(int i) {
        switch(i) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (player != null)
                    player.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (player != null && player.isPlaying())
                    player.setVolume(0.3f, 0.3f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS:
                if (player != null && player.isPlaying())
                    callback.onPause();
                break;
        }
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                    Log.d(Global.TAG, "In ACTION_BECOMING_NOISY");
                    callback.onPause();
                    break;
                case ACTION_PLAY:
                    Log.d(Global.TAG, "In ACTION_PLAY");
                    callback.onPlay();
                    break;
                case ACTION_PAUSE:
                    Log.d(Global.TAG, "In ACTION_PAUSE");
                    callback.onPause();
                    break;
                case ACTION_NEXT:
                    Log.d(Global.TAG, "In ACTION_NEXT");
                    skipToNext();
                    break;
                case ACTION_PREV:
                    Log.d(Global.TAG, "In ACTION_PREV");
                    skipToPrevious();
                    break;
                case ACTION_STOP:
                    Log.d(Global.TAG, "In ACTION_STOP");
                    callback.onStop();
                    break;
            }
        }
    };

    private void initSession() {
        // Create a MediaSessionCompat
        mediaSession = new MediaSessionCompat(this, "TelawatService");

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
        stateBuilder = new PlaybackStateCompat.Builder().setActions(
                PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mediaSession.setPlaybackState(stateBuilder.build());

        // callback() has methods that handle callbacks from a media controller
        mediaSession.setCallback(callback);

        // Set the session's token so that client activities can communicate with it.
        setSessionToken(mediaSession.getSessionToken());
    }

    private void setupActions() {
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        intentFilter.addAction(ACTION_PLAY);
        intentFilter.addAction(ACTION_PAUSE);
        intentFilter.addAction(ACTION_NEXT);
        intentFilter.addAction(ACTION_PREV);
        intentFilter.addAction(ACTION_STOP);

        String pkg = getPackageName();
        int flags = PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE;

        playAction = new NotificationCompat.Action(R.drawable.ic_play_arrow, "Play",
                PendingIntent.getBroadcast(this, id,
                        new Intent(ACTION_PLAY).setPackage(pkg), flags));

        pauseAction = new NotificationCompat.Action(R.drawable.ic_baseline_pause, "Pause",
                PendingIntent.getBroadcast(this, id,
                        new Intent(ACTION_PAUSE).setPackage(pkg), flags));

        nextAction = new NotificationCompat.Action(R.drawable.ic_skip_next, "next",
                PendingIntent.getBroadcast(this, id,
                        new Intent(ACTION_NEXT).setPackage(pkg), flags));

        prevAction = new NotificationCompat.Action(R.drawable.ic_skip_previous, "previous",
                PendingIntent.getBroadcast(this, id,
                        new Intent(ACTION_PREV).setPackage(pkg), flags));
    }

    private void buildNotification() {
        // Get the session's metadata
        controller = mediaSession.getController();
        mediaMetadata = controller.getMetadata();
        MediaDescriptionCompat description = mediaMetadata.getDescription();

        mediaSession.setSessionActivity(getContentIntent());

        createNotificationChannel();

        // Create a NotificationCompat.Builder
        notificationBuilder = new NotificationCompat.Builder(this, channelId);

        notificationBuilder
                // Add the metadata for the currently playing track
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setSubText(description.getDescription())
                .setLargeIcon(description.getIconBitmap())
                // Enable launching the player by clicking the notification
                .setContentIntent(controller.getSessionActivity())
                // Stop the service when the notification is swiped away
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_STOP))
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
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2)
                        // Add a cancel button
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(
                                this, PlaybackStateCompat.ACTION_STOP)));
        notification = notificationBuilder.build();

        // Display the notification and place the service in the foreground
        startForeground(id, notification);
    }

    private void initMediaSessionMetadata() {
        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();

        //Notification icon in card
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON,
                BitmapFactory.decodeResource(getResources(), R.color.surface_M));
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                BitmapFactory.decodeResource(getResources(), R.color.surface_M));
        //lock screen icon for pre lollipop
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART,
                BitmapFactory.decodeResource(getResources(), R.drawable.low_quality_foreground));

        metadataBuilder.putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, "");
        metadataBuilder.putText(MediaMetadataCompat.METADATA_KEY_TITLE, "");
        metadataBuilder.putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, "");
        metadataBuilder.putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, "");

        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, 0);
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, 0);

        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId);

        mediaSession.setMetadata(metadataBuilder.build());
    }

    private void updateMetadata(boolean duration) {
        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();

        //Notification icon in card
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON,
                BitmapFactory.decodeResource(getResources(), R.color.surface_M));
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                BitmapFactory.decodeResource(getResources(), R.color.surface_M));
        //lock screen icon for pre lollipop
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART,
                BitmapFactory.decodeResource(getResources(), R.drawable.low_quality_foreground));

        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId);

        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE,
                surahNames.get(surahIndex));
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE,
                surahNames.get(surahIndex));
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, reciterName);
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, reciterName);
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION,
                version.getRewaya());

        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, surahIndex);
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, version.getCount());
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
                duration ? player.getDuration() : 0);

        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId);

        mediaMetadata = metadataBuilder.build();
        mediaSession.setMetadata(mediaMetadata);
    }

    private void updatePbState(int state) {
        stateBuilder.setState(state, player.getCurrentPosition(), 1)
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO);

        mediaSession.setPlaybackState(stateBuilder.build());
    }

    private final Runnable runnable = () -> {
        if (player != null && controller.getPlaybackState().getState()
                == PlaybackStateCompat.STATE_PLAYING)
            refresh();
    };

    private void refresh() {
        updatePbState(controller.getPlaybackState().getState());
        handler.postDelayed(runnable, 1000);
    }

    private void updateNotification(boolean playing) {
        if (playing)
            notificationBuilder.clearActions()
                    .addAction(prevAction).addAction(pauseAction).addAction(nextAction);
        else
            notificationBuilder.clearActions()
                    .addAction(prevAction).addAction(playAction).addAction(nextAction);

        notification = notificationBuilder.build();
        notificationManager.notify(id, notification);
    }

    private void initPlayer() {
        player = new MediaPlayer();
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        wifiLock = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "myLock");
        wifiLock.acquire();

        player.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(this)
                .setAudioAttributes(attrs)
                .build();

        player.setOnPreparedListener(mp -> {
            if (playType.equals("continue"))
                player.seekTo(continueFrom);

            player.start();

            updateMetadata(true);    // For the duration
            updatePbState(PlaybackStateCompat.STATE_PLAYING);
            updateNotification(true);
        });
        player.setOnCompletionListener(mp -> skipToNext());
        player.setOnErrorListener((mp, what, extra) -> {
            Log.e(Global.TAG, "Error in TelawatService player: " + what);
            return true;
        });
    }

    private void startPlaying(int surah) {
        player.reset();

        if (tryOffline(surah))
            return;

        String text = String.format(Locale.US, "%s/%03d.mp3",
                version.getServer(), surah+1);
        try {
            player.setDataSource(getApplicationContext(), Uri.parse(text));
            player.prepareAsync();
        }
        catch (IOException e) {
            e.printStackTrace();
            Log.e(Global.TAG, "Problem in TelawatService player");
        }
    }

    private boolean tryOffline(int surah) {
        String path = getExternalFilesDir(null) + "/Telawat/" + reciterId + "/"
                + version.getVersionId() + "/" + surah + ".mp3";
        try {
            player.setDataSource(path);
            player.prepare();
            Log.i(Global.TAG, "Playing Offline");
            return true;
        }
        catch (FileNotFoundException f) {
            Log.i(Global.TAG, "Not available offline");
            return false;
        }
        catch (IOException e) {
            e.printStackTrace();
            Log.e(Global.TAG, "Problem in TelawatService player");
            return false;
        }
    }

    private void createNotificationChannel() {
        CharSequence name;
        String description = "quran listening";
        channelId = "Telawat";
        name = "تلاوات";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel notificationChannel  = new NotificationChannel(
                channelId, name, importance);
        notificationChannel.setDescription(description);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    private void getSuraNames() {
        surahNames = (ArrayList<String>) db.suraDao().getNames();
    }

    private PendingIntent getContentIntent() {
        Intent intent = new Intent(this, TelawatClient.class).setAction("back")
                .putExtra("media_id", mediaId);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;

        return PendingIntent.getActivity(this, 36, intent, flags);
    }

    @Nullable @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName,
                                 int clientUid, @Nullable Bundle rootHints) {
        // (Optional) Control the level of access for the specified package name.
        if (allowBrowsing(clientPackageName, clientUid)) {
            // Returns a root ID that clients can use with onLoadChildren() to retrieve
            // the content hierarchy.
            return new BrowserRoot(MY_MEDIA_ROOT_ID, null);
        } else {
            // Clients can connect, but this BrowserRoot is an empty hierarchy
            // so onLoadChildren returns nothing. This disables the ability to browse for content.
            return new BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, null);
        }
    }

    @Override
    public void onLoadChildren(@NonNull String parentId,
                               @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        /*//  Browsing not allowed
        if (TextUtils.equals(MY_EMPTY_MEDIA_ROOT_ID, parentMediaId)) {
            result.sendResult(null);
            return;
        }
        // Assume for example that the music catalog is already loaded/cached.
        List<MediaItem> mediaItems = new ArrayList<>();
        // Check if this is the root menu:
        if (MY_MEDIA_ROOT_ID.equals(parentMediaId)) {
            // Build the MediaItem objects for the top level,
            // and put them in the mediaItems list...
        } else {
            // Examine the passed parentMediaId to see which submenu we're at,
            // and put the children of that menu in the mediaItems list...
        }
        result.sendResult(mediaItems);*/
    }

    private boolean allowBrowsing(String clientPackageName, int clientUid) {
        return true;
    }

    private void saveForLater(int progress) {
        String text = "سورة " + surahNames.get(surahIndex) + " للقارئ " + reciterName + " برواية " +
                version.getRewaya();

        SharedPreferences.Editor editor = pref.edit();
        editor.putString("last_played_media_id", mediaId);
        editor.putString("last_played_text", text);
        editor.putInt("last_telawa_progress", progress);
        editor.apply();
    }

    private void cleanUp() {
        if (wifiLock != null) {
            wifiLock.release();
            wifiLock = null;
        }
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(Global.TAG, "In onUnbind of TelawatService");
        saveForLater(player.getCurrentPosition());
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanUp();
    }
}
