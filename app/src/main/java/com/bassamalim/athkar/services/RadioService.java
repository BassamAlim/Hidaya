package com.bassamalim.athkar.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import com.bassamalim.athkar.R;
import com.bassamalim.athkar.models.RecitationVersion;
import com.bassamalim.athkar.other.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RadioService extends MediaBrowserServiceCompat {

    private static final String MY_MEDIA_ROOT_ID = "media_root_id";
    private static final String MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id";
    private final int REQUEST_CODE = 333;
    private static final String ACTION_BECOMING_NOISY = AudioManager.ACTION_AUDIO_BECOMING_NOISY;
    private static final String ACTION_PLAY_PAUSE =
            "com.bassamalim.athkar.services.radioservice.playpause";
    private static final String ACTION_NEXT = "com.bassamalim.athkar.services.radioservice.next";
    private static final String ACTION_PREV = "com.bassamalim.athkar.services.radioservice.prev";
    private NotificationCompat.Action playPauseAction;
    private NotificationCompat.Action nextAction;
    private NotificationCompat.Action prevAction;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat controller;
    private MediaMetadataCompat mediaMetadata;
    private Context context;
    private String channelId = "channel ID";
    private final int id = 333;
    private int flags;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private AudioManager.OnAudioFocusChangeListener afChangeListener;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private Notification notification;
    private MediaPlayer player;
    private AudioManager am;
    private final IntentFilter intentFilter = new IntentFilter();
    private AudioFocusRequest audioFocusRequest;
    private ArrayList<String> surahNames;
    private String reciterName;
    private int reciterIndex;
    private RecitationVersion version;
    private int surahIndex;
    private WifiManager.WifiLock wifiLock;

    @Override
    public void onCreate() {
        super.onCreate();

        context = RadioService.this;

        initSession();

        setActions();
        buildNotification();

        initPlayer();

        registerReceiver(receiver, intentFilter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    MediaSessionCompat.Callback callback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            super.onPlayFromMediaId(mediaId, extras);

            surahNames = extras.getStringArrayList("surah_names");
            int newReciter = extras.getInt("reciter_index", 0);
            String newReciterName = extras.getString("reciter_name");
            RecitationVersion newVersion = (RecitationVersion)
                    extras.getSerializable("version");
            int newSurah = Integer.parseInt(mediaId);

            if (controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_NONE ||
                    newReciter != reciterIndex || newSurah != surahIndex ||
                    newVersion.getIndex() != version.getIndex()) {

                reciterIndex = newReciter;
                reciterName = newReciterName;
                version = newVersion;
                surahIndex = newSurah;

                if (controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_NONE)
                    onPlay();
                else
                    playNew();
            }
        }

        @Override
        public void onPlay() {
            play();
        }

        @Override
        public void onStop() {
            stop();
        }

        @Override
        public void onPause() {
            pause();
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
    };

    private void playNew() {
        // start the player
        startPlaying(surahIndex);

        // Update state
        updatePbState(PlaybackStateCompat.STATE_PLAYING);
        updateNotification(true);
    }

    private void play() {
        // Request audio focus for playback, this registers the afChangeListener
        int result = AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            result = am.requestAudioFocus(audioFocusRequest);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // Start the service
            startService(new Intent(context, RadioService.class));
            // Set the session active  (and update metadata and state)
            mediaSession.setActive(true);

            // start the player (custom call)
            if (controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED)
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

    private void pause() {
        // Update metadata and state
        // pause the player (custom call)
        player.pause();

        updatePbState(PlaybackStateCompat.STATE_PAUSED);
        updateNotification(false);

        // Take the service out of the foreground, retain the notification
        stopForeground(false);
    }

    private void stop() {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        // Abandon audio focus
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            am.abandonAudioFocusRequest(audioFocusRequest);

        cleanUp();
        unregisterReceiver(receiver);
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

    private void skipToNext() {
        int temp = surahIndex;
        do {
            temp++;
        } while(temp < 114 && !version.getSuras().contains("," + (temp+1) + ","));

        if (temp < 114) {
            surahIndex = temp;
            startPlaying(surahIndex);
            updatePbState(PlaybackStateCompat.STATE_PLAYING);
        }
    }

    private void skipToPrevious() {
        int temp = surahIndex;
        do {
            temp--;
        } while(temp >= 0 && !version.getSuras().contains("," + (temp+1) + ","));

        if (temp >= 0) {
            surahIndex = temp;
            startPlaying(surahIndex);
            updatePbState(PlaybackStateCompat.STATE_PLAYING);
        }
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_BECOMING_NOISY:
                    Log.i(Constants.TAG, "In ACTION_BECOMING_NOISY");
                    pause();
                    break;
                case ACTION_PLAY_PAUSE:
                    if (controller.getPlaybackState().getState()
                            == PlaybackStateCompat.STATE_PLAYING) {
                        Log.i(Constants.TAG, "In ACTION_PAUSE");
                        pause();
                    }
                    else if (controller.getPlaybackState().getState() ==
                            PlaybackStateCompat.STATE_PAUSED) {
                        Log.i(Constants.TAG, "In ACTION_PLAY");
                        play();
                    }
                    break;
                case ACTION_NEXT:
                    Log.i(Constants.TAG, "In ACTION_NEXT");
                    skipToNext();
                    break;
                case ACTION_PREV:
                    Log.i(Constants.TAG, "In ACTION_PREV");
                    skipToPrevious();
                    break;
            }
        }
    };

    private void initSession() {
        // Create a MediaSessionCompat
        mediaSession = new MediaSessionCompat(context, "RadioService");

        // Enable callbacks from MediaButtons and TransportControls
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder().setActions(
                PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mediaSession.setPlaybackState(stateBuilder.build());

        // callback() has methods that handle callbacks from a media controller
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            mediaSession.setCallback(callback);

        // Set the session's token so that client activities can communicate with it.
        setSessionToken(mediaSession.getSessionToken());
    }

    private void buildNotification() {
        // Given a media session and its context (usually the component containing the session)

        initMediaSessionMetadata();

        // Get the session's metadata
        controller = mediaSession.getController();
        mediaMetadata = controller.getMetadata();
        MediaDescriptionCompat description = mediaMetadata.getDescription();

        /*Intent intent = new Intent(context, RadioClient.class).setAction("back").putExtra(
                "reciter_index", reciterIndex).putExtra("version", version).putExtra(
                        "surah_index", surahIndex).putExtra("surah_names", surahNames);
        PendingIntent pi = PendingIntent.getActivity(context, 36,
                intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        mediaSession.setSessionActivity(pi);*/

        createNotificationChannel();

        // Create a NotificationCompat.Builder
        notificationBuilder = new NotificationCompat.Builder(context, channelId);

        notificationBuilder
                // Add the metadata for the currently playing track
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setSubText(description.getDescription())
                /*.setLargeIcon(BitmapFactory.decodeResource(
                        getResources(), R.drawable.launcher_foreground))
                // Enable launching the player by clicking the notification
                .setContentIntent(controller.getSessionActivity())
                // Stop the service when the notification is swiped away
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                        PlaybackStateCompat.ACTION_STOP))
                // Make the transport controls visible on the lockscreen
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                // Add an app icon and set its accent color
                // Be careful about the color
                .setSmallIcon(R.drawable.launcher_foreground)
                /*.setColorized(true)
                .setColor(getResources().getColor(R.color.accent))*/
                // Add buttons
                .addAction(prevAction).addAction(playPauseAction).addAction(nextAction)
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
                                context, PlaybackStateCompat.ACTION_STOP)));
        notification = notificationBuilder.build();

        // Display the notification and place the service in the foreground
        startForeground(id, notification);
    }

    private void initMediaSessionMetadata() {
        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();

        //Notification icon in card
        /*metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON,
                BitmapFactory.decodeResource(getResources(), R.drawable.launcher_foreground));
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                BitmapFactory.decodeResource(getResources(), R.drawable.launcher_foreground));
        //lock screen icon for pre lollipop
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART,
                BitmapFactory.decodeResource(getResources(), R.drawable.launcher_foreground));*/

        metadataBuilder.putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, "اسم السورة");
        metadataBuilder.putText(MediaMetadataCompat.METADATA_KEY_TITLE, "اسم السورة");
        metadataBuilder.putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, "اسم القارئ");
        metadataBuilder.putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, "نوع القراءة");

        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, 0);
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, 0);

        mediaSession.setMetadata(metadataBuilder.build());
    }

    private void updateMetadata() {
        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();

        //Notification icon in card
        /*metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON,
                BitmapFactory.decodeResource(getResources(), R.drawable.launcher_foreground));
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                BitmapFactory.decodeResource(getResources(), R.drawable.launcher_foreground));
        //lock screen icon for pre lollipop
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART,
                BitmapFactory.decodeResource(getResources(), R.drawable.launcher_foreground));*/

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
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, player.getDuration());
        metadataBuilder.putLong("reciter_index", reciterIndex);
        metadataBuilder.putLong("version_index", version.getIndex());

        mediaMetadata = metadataBuilder.build();
        mediaSession.setMetadata(mediaMetadata);
    }

    private void updatePbState(int state) {
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();

        stateBuilder.setState(state, player.getCurrentPosition(), 1)
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO);

        mediaSession.setPlaybackState(stateBuilder.build());
    }

    private final Runnable runnable = () -> {
        //Log.i(Constants.TAG, "in the runnable");
        if (player != null && controller.getPlaybackState().getState()
                == PlaybackStateCompat.STATE_PLAYING)
            refresh();
    };

    private void refresh() {
        updatePbState(controller.getPlaybackState().getState());
        handler.postDelayed(runnable, 1000);
    }

    private void updateNotification(boolean playing) {
        if (playing) {
            playPauseAction = new NotificationCompat.Action(R.drawable.ic_baseline_pause,
                    "play_pause", PendingIntent.getBroadcast(context, REQUEST_CODE,
                            new Intent(ACTION_PLAY_PAUSE).setPackage(getPackageName()), flags));
        }
        else {
            playPauseAction = new NotificationCompat.Action(R.drawable.ic_play_arrow,
                    "play_pause", PendingIntent.getBroadcast(context, REQUEST_CODE,
                            new Intent(ACTION_PLAY_PAUSE).setPackage(getPackageName()), flags));
        }
        notificationBuilder.clearActions()
                .addAction(prevAction).addAction(playPauseAction).addAction(nextAction);

        notification = notificationBuilder.build();
        notificationManager.notify(id, notification);
    }

    private void initPlayer() {
        player = new MediaPlayer();
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        //mediaPlayer.setLooping(true);
        wifiLock = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "myLock");
        wifiLock.acquire();
        player.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        setFocusListeners();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(afChangeListener)
                    .setAudioAttributes(attrs)
                    .build();
        }
    }

    private void setActions() {
        intentFilter.addAction(ACTION_BECOMING_NOISY);
        intentFilter.addAction(ACTION_PLAY_PAUSE);
        intentFilter.addAction(ACTION_NEXT);
        intentFilter.addAction(ACTION_PREV);

        String pkg = getPackageName();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
            flags = PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        else
            flags = PendingIntent.FLAG_CANCEL_CURRENT;

        playPauseAction = new NotificationCompat.Action(R.drawable.ic_play_arrow, "play_pause",
                PendingIntent.getBroadcast(context, REQUEST_CODE,
                        new Intent(ACTION_PLAY_PAUSE).setPackage(pkg), flags));
        nextAction = new NotificationCompat.Action(R.drawable.ic_skip_next, "next",
                PendingIntent.getBroadcast(context, REQUEST_CODE,
                        new Intent(ACTION_NEXT).setPackage(pkg), flags));
        prevAction = new NotificationCompat.Action(R.drawable.ic_skip_previous, "previous",
                PendingIntent.getBroadcast(context, REQUEST_CODE,
                        new Intent(ACTION_PREV).setPackage(pkg), flags));
    }

    private void setFocusListeners() {
        afChangeListener = focusChange -> {
            switch( focusChange ) {
                case AudioManager.AUDIOFOCUS_LOSS: {
                    if (player.isPlaying())
                        stop();
                    break;
                }
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                    pause();
                    break;
                }
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                    if (player != null)
                        player.setVolume(0.3f, 0.3f);
                    break;
                }
                case AudioManager.AUDIOFOCUS_GAIN: {
                    if (player != null) {
                        if (!player.isPlaying())
                            play();
                        player.setVolume(1.0f, 1.0f);
                    }
                    break;
                }
            }
        };
    }

    private void startPlaying(int surah) {
        String text = String.format(Locale.US, "%s/%03d.mp3",
                version.getServer(), surah+1);
        Uri uri = Uri.parse(text);
        try {
            player.reset();
            player.setDataSource(getApplicationContext(), uri);
            player.prepareAsync();
            player.setOnPreparedListener(mp -> {
                player.start();
                updateMetadata();    // For the duration
                updatePbState(PlaybackStateCompat.STATE_PLAYING);
                updateNotification(true);
            });
            player.setOnCompletionListener(mp -> skipToNext());
            player.setOnErrorListener((mp, what, extra) -> {
                Log.e(Constants.TAG, "Error in RadioService player: " + what);
                return true;
            });
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

    private void cleanUp() {
        if (wifiLock != null)
            wifiLock.release();
        if (player != null) {
            player.release();
            player = null;
        }
    }

}
