package com.bassamalim.athkar.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    private static final String ACTION_PLAY = "com.bassamalim.athkar.services.radioservice.play";
    private static final String ACTION_PAUSE = "com.bassamalim.athkar.services.radioservice.pause";
    private static final String ACTION_NEXT = "com.bassamalim.athkar.services.radioservice.next";
    private static final String ACTION_PREV = "com.bassamalim.athkar.services.radioservice.prev";
    private NotificationCompat.Action playAction;
    private NotificationCompat.Action pauseAction;
    private NotificationCompat.Action nextAction;
    private NotificationCompat.Action prevAction;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat controller;
    private MediaMetadataCompat mediaMetadata;
    private MediaDescriptionCompat description;
    private Context context;
    private String channelId = "channel ID";
    private final int id = 333;
    private Handler handler = new Handler(Looper.getMainLooper());
    private AudioManager.OnAudioFocusChangeListener afChangeListener;
    private Notification notification;
    private MediaPlayer player;
    private AudioManager am;
    private AudioAttributes attrs;
    private PlaybackStateCompat.Builder stateBuilder;
    private final IntentFilter intentFilter = new IntentFilter();
    private AudioFocusRequest audioFocusRequest;
    private int reciter;
    private ArrayList<String> surahNames;
    private String reciterName;
    private RecitationVersion version;
    private int surahIndex;
    private WifiManager.WifiLock wifiLock;

    @Override
    public void onCreate() {
        Log.i(Constants.TAG, "in onCreate in RadioService");
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

            surahIndex = Integer.parseInt(mediaId);
            Log.i(Constants.TAG, "id is " + surahIndex);
            reciter = extras.getInt("reciter", 0);
            version = (RecitationVersion) extras.getSerializable("version");
            surahNames = extras.getStringArrayList("surah_names");
            reciterName = extras.getString("reciter_name");
            onPlay();
            updateMetadata();
        }
        @Override
        public void onPlay() {
            Log.i(Constants.TAG, "in onPlay of callback in RadioService");

            // Request audio focus for playback, this registers the afChangeListener
            int result = am.requestAudioFocus(audioFocusRequest);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // Start the service
                startService(new Intent(context, RadioService.class));
                // Set the session active  (and update metadata and state)
                mediaSession.setActive(true);

                // start the player (custom call)
                if (controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED
                        || controller.getPlaybackState().getState()
                        == PlaybackStateCompat.STATE_PLAYING)
                    player.start();
                else
                    play(surahIndex);

                updateMetadata();
                updatePbState(PlaybackStateCompat.STATE_PLAYING);
                refresh();

                // Register BECOME_NOISY BroadcastReceiver
                registerReceiver(receiver, intentFilter);
                // Put the service in the foreground, post notification
                startForeground(id, notification);
            }
        }

        @Override
        public void onStop() {
            Log.i(Constants.TAG, "in onStop of callback in RadioService");

            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            // Abandon audio focus
            am.abandonAudioFocusRequest(audioFocusRequest);

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

        @Override
        public void onPause() {
            Log.i(Constants.TAG, "in onPause of callback in RadioService");
            // Update metadata and state
            // pause the player (custom call)
            player.pause();
            updatePbState(PlaybackStateCompat.STATE_PAUSED);

            // Take the service out of the foreground, retain the notification
            stopForeground(false);
        }

        @Override
        public void onFastForward() {
            super.onFastForward();
            player.seekTo(player.getCurrentPosition()+10000);
        }

        @Override
        public void onRewind() {
            super.onRewind();
            player.seekTo(player.getCurrentPosition()-10000);
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            playNext();
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            playPrevious();
        }

        @Override
        public void onSeekTo(long pos) {
            Log.i(Constants.TAG, "in onSeekTo of callback in RadioService");
            super.onSeekTo(pos);
            player.seekTo((int) pos);
        }
    };

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(Constants.TAG, "in receiver");
            switch (intent.getAction()) {
                case ACTION_BECOMING_NOISY:
                    Log.i(Constants.TAG, "in receiver in ACTION_BECOMING_NOISY RadioService");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        callback.onPause();
                    else
                        player.pause();
                    break;
                case ACTION_PLAY:
                    Log.i(Constants.TAG, "in receiver in ACTION_PLAY RadioService");
                    if (mediaSession.getController().getPlaybackState().getState()
                            == PlaybackStateCompat.STATE_PLAYING) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            callback.onPause();
                        else
                            player.pause();


                    }
                    else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            callback.onPlay();
                        else
                            player.start();
                    }
                    break;
                case ACTION_NEXT:
                    Log.i(Constants.TAG, "in receiver in ACTION_NEXT RadioService");
                    playNext();
                    break;
                case ACTION_PREV:
                    Log.i(Constants.TAG, "in receiver in ACTION_PREV RadioService");
                    playPrevious();
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
        stateBuilder = new PlaybackStateCompat.Builder().setActions(
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
        description = mediaMetadata.getDescription();

        createNotificationChannel();

        // Create a NotificationCompat.Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);

        builder
                // Add the metadata for the currently playing track
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setSubText(description.getDescription())
                .setLargeIcon(BitmapFactory.decodeResource(
                        getResources(), R.drawable.ic_launcher_foreground))
                // Enable launching the player by clicking the notification
                .setContentIntent(controller.getSessionActivity())
                // Stop the service when the notification is swiped away
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                        PlaybackStateCompat.ACTION_STOP))
                // Make the transport controls visible on the lockscreen
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                // Add an app icon and set its accent color
                // Be careful about the color
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setColor(ContextCompat.getColor(context, R.color.accent))
                // Add buttons
                .addAction(prevAction).addAction(playAction).addAction(nextAction)
                // So there will be no notification tone
                .setSilent(true)
                // So the user wouldn't swipe it off
                .setOngoing(true)
                // Take advantage of MediaStyle features
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(1)
                        // Add a cancel button
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(
                                context, PlaybackStateCompat.ACTION_STOP)));
        notification = builder.build();

        // Display the notification and place the service in the foreground
        startForeground(id, notification);
    }

    private void initMediaSessionMetadata() {
        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
        //Notification icon in card
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON,
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground));
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground));

        //lock screen icon for pre lollipop
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART,
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground));
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, "تلاوات");
        metadataBuilder.putString(
                MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, "القارئ");
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION,
                "القراءة");

        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, -1);
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, 0);

        mediaSession.setMetadata(metadataBuilder.build());
    }

    private void updateMetadata() {
        Log.i(Constants.TAG, "in update metadata in RadioService");
        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();

        //Notification icon in card
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON,
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground));
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground));
        //lock screen icon for pre lollipop
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART,
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground));

        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE,
                surahNames.get(surahIndex));
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, reciterName);
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION,
                version.getRewaya());

        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, surahIndex);
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, version.getCount());
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, player.getDuration());

        mediaMetadata = metadataBuilder.build();
        mediaSession.setMetadata(mediaMetadata);
    }

    private void updatePbState(int state) {
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();

        stateBuilder.setState(state, player.getCurrentPosition(), 1);

        mediaSession.setPlaybackState(stateBuilder.build());
    }

    private final Runnable runnable = () -> {
        Log.i(Constants.TAG, "in the runnable");
        if (player != null && controller.getPlaybackState().getState()
                == PlaybackStateCompat.STATE_PLAYING)
            refresh();
    };

    private void refresh() {
        updatePbState(mediaSession.getController().getPlaybackState().getState());
        Log.i(Constants.TAG, "in the runnable progress is: " +
                mediaSession.getController().getPlaybackState().getPosition());
        handler.postDelayed(runnable, 1000);
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
        attrs = new AudioAttributes.Builder()
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
        intentFilter.addAction(ACTION_PLAY);
        intentFilter.addAction(ACTION_NEXT);
        intentFilter.addAction(ACTION_PREV);

        String pkg = getPackageName();
        int flags;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            flags = PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        }
        else {
            flags = PendingIntent.FLAG_CANCEL_CURRENT;
        }

        playAction = new NotificationCompat.Action(R.drawable.ic_play, "play",
                PendingIntent.getBroadcast(context, REQUEST_CODE,
                        new Intent(ACTION_PLAY).setPackage(pkg), flags));
        nextAction = new NotificationCompat.Action(R.drawable.ic_player_next, "next",
                PendingIntent.getBroadcast(context, REQUEST_CODE,
                        new Intent(ACTION_NEXT).setPackage(pkg), flags));
        prevAction = new NotificationCompat.Action(R.drawable.ic_player_previous, "previous",
                PendingIntent.getBroadcast(context, REQUEST_CODE,
                        new Intent(ACTION_PREV).setPackage(pkg), flags));
    }

    private void setFocusListeners() {
        afChangeListener = focusChange -> {
            Log.i(Constants.TAG, "in focusChangeListener in RadioClient");
            switch( focusChange ) {
                case AudioManager.AUDIOFOCUS_LOSS: {
                    Log.i(Constants.TAG, "in loss of focusChangeListener in RadioClient");
                    if (player.isPlaying()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            callback.onStop();
                        else
                            player.stop();
                    }
                    break;
                }
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                    Log.i(Constants.TAG,
                            "in lossTransient of focusChangeListener in RadioClient");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        callback.onPause();
                    else
                        player.pause();
                    break;
                }
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                    Log.i(Constants.TAG, "in canDuck of focusChangeListener in RadioClient");
                    if (player != null)
                        player.setVolume(0.3f, 0.3f);
                    break;
                }
                case AudioManager.AUDIOFOCUS_GAIN: {
                    Log.i(Constants.TAG, "in gain of focusChangeListener in RadioClient");
                    if (player != null) {
                        if (!player.isPlaying()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                callback.onPlay();
                            else
                                player.start();
                        }
                        player.setVolume(1.0f, 1.0f);
                    }
                    break;
                }
            }
        };
    }

    private void play(int surah) {
        Log.i(Constants.TAG, "in play in RadioService");
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
            });
            //player.setOnCompletionListener(mp -> playNext());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playNext() {
        int temp = surahIndex;
        do {
            temp++;
        } while(temp < 114 && !version.getSuras().contains("," + (temp+1) + ","));

        if (temp < 114) {
            surahIndex = temp;
            play(surahIndex);
            updateMetadata();
        }
    }

    private void playPrevious() {
        int temp = surahIndex;
        do {
            temp--;
        } while(temp >= 0 && !version.getSuras().contains("," + (temp+1) + ","));

        if (temp >= 0) {
            surahIndex = temp;
            play(surahIndex);
            updateMetadata();
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
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    @Nullable @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName,
                                 int clientUid, @Nullable Bundle rootHints) {
        Log.i(Constants.TAG, "in onGetRoot in RadioService");

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
        Log.i(Constants.TAG, "in onLoadChildren in RadioService");

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

}
