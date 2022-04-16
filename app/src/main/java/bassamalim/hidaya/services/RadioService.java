package bassamalim.hidaya.services;

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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import bassamalim.hidaya.R;
import bassamalim.hidaya.activities.RadioClient;
import bassamalim.hidaya.other.Const;

public class RadioService extends MediaBrowserServiceCompat implements
        AudioManager.OnAudioFocusChangeListener {

    private static final String MY_MEDIA_ROOT_ID = "media_root_id";
    private static final String MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id";
    private static final String ACTION_PLAY_PAUSE =
            "bassamalim.hidaya.services.RadioService.playpause";
    private static final String ACTION_STOP = "bassamalim.hidaya.services.RadioService.stop";
    private NotificationCompat.Action playPauseAction;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat controller;
    private String channelId = "channel ID";
    private final int id = 444;
    private int flags;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private Notification notification;
    private MediaPlayer player;
    private AudioManager am;
    private final IntentFilter intentFilter = new IntentFilter();
    private AudioFocusRequest audioFocusRequest;
    private WifiManager.WifiLock wifiLock;
    private String link;

    @Override
    public void onCreate() {
        super.onCreate();

        initSession();

        setActions();
        initMediaSessionMetadata();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    final MediaSessionCompat.Callback callback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            Log.d(Const.TAG, "In onPlayFromMediaId of RadioClient");
            super.onPlayFromMediaId(mediaId, extras);
            link = mediaId;
            buildNotification();
            initPlayer();
            play();
        }

        @Override
        public void onStop() {
            Log.d(Const.TAG, "In onStop of RadioClient");
            super.onStop();
            stop();
        }

        @Override
        public void onPause() {
            super.onPause();
            Log.d(Const.TAG, "In onPause of RadioClient");
            pause();
        }
    };

    private void play() {
        // Request audio focus for playback, this registers the afChangeListener
        int result = AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            result = am.requestAudioFocus(audioFocusRequest);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // Start the service
            startService(new Intent(getApplicationContext(), RadioService.class));
            // Set the session active  (and update metadata and state)
            mediaSession.setActive(true);

            // start the player (custom call)
            if (controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED ||
                    controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_STOPPED)
                player.start();
            else
                startPlaying();

            updatePbState(PlaybackStateCompat.STATE_PLAYING);
            updateNotification(true);

            // Register Receiver
            registerReceiver(receiver, intentFilter);
            // Put the service in the foreground, post notification
            startForeground(id, notification);
        }
    }

    private void pause() {
        Log.d(Const.TAG, "in pause of RadioService");
        // Update metadata and state
        // pause the player (custom call)
        mediaSession.setActive(false);
        player.pause();

        updatePbState(PlaybackStateCompat.STATE_PAUSED);
        updateNotification(false);

        // Take the service out of the foreground, retain the notification
        stopForeground(false);
    }

    private void stop() {
        Log.d(Const.TAG, "in stop of RadioService");
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

        onDestroy();
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
                    pause();
                break;
        }
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                    Log.d(Const.TAG, "In ACTION_BECOMING_NOISY of RadioService");
                    pause();
                    break;
                case ACTION_PLAY_PAUSE:
                    if (controller.getPlaybackState().getState()
                            == PlaybackStateCompat.STATE_PLAYING) {
                        Log.d(Const.TAG, "In ACTION_PAUSE of RadioService");
                        pause();
                    }
                    else if (controller.getPlaybackState().getState() ==
                            PlaybackStateCompat.STATE_PAUSED) {
                        Log.d(Const.TAG, "In ACTION_PLAY of RadioService");
                        play();
                    }
                    break;
                case ACTION_STOP:
                    Log.d(Const.TAG, "In ACTION_STOP of RadioService");
                    stop();
                    break;
            }
        }
    };

    private void initSession() {
        // Create a MediaSessionCompat
        mediaSession = new MediaSessionCompat(this, "RadioService");

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

        // Get the session's metadata
        controller = mediaSession.getController();
        MediaMetadataCompat mediaMetadata = controller.getMetadata();
        MediaDescriptionCompat description = mediaMetadata.getDescription();

        mediaSession.setSessionActivity(getContentIntent());

        createNotificationChannel();

        // Create a NotificationCompat.Builder
        notificationBuilder = new NotificationCompat.Builder(this, channelId);

        notificationBuilder
                // Add the metadata for the currently playing track
                .setContentTitle(description.getTitle())
                // Stop the service when the notification is swiped away
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_STOP))
                // Make the transport controls visible on the lockscreen
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                // Add an app icon and set its accent color
                // Be careful about the color
                .setSmallIcon(R.drawable.launcher_foreground)
                .setColorized(true)
                .setColor(getResources().getColor(R.color.surface_M, getTheme()))
                // Add buttons
                // Enable launching the player by clicking the notification
                .setContentIntent(controller.getSessionActivity())
                .addAction(playPauseAction)
                // So there will be no notification tone
                .setSilent(true)
                // So the user wouldn't swipe it off
                .setOngoing(true)
                // Take advantage of MediaStyle features
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0)
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

        metadataBuilder.putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, "إذاعة القرآن");
        metadataBuilder.putText(MediaMetadataCompat.METADATA_KEY_TITLE, "إذاعة القرآن");

        mediaSession.setMetadata(metadataBuilder.build());
    }

    private void updatePbState(int state) {
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();

        stateBuilder.setState(state, player.getCurrentPosition(), 1)
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO);

        mediaSession.setPlaybackState(stateBuilder.build());
    }

    private void updateNotification(boolean playing) {
        if (playing) {
            playPauseAction = new NotificationCompat.Action(R.drawable.ic_baseline_pause,
                    "play_pause", PendingIntent.getBroadcast(this, id,
                    new Intent(ACTION_PLAY_PAUSE).setPackage(getPackageName()), flags));
        }
        else {
            playPauseAction = new NotificationCompat.Action(R.drawable.ic_play_arrow,
                    "play_pause", PendingIntent.getBroadcast(this, id,
                    new Intent(ACTION_PLAY_PAUSE).setPackage(getPackageName()), flags));
        }
        notificationBuilder.clearActions().addAction(playPauseAction);

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(this)
                    .setAudioAttributes(attrs)
                    .build();
        }

        player.setOnPreparedListener(mp -> {
            player.start();
            updatePbState(PlaybackStateCompat.STATE_PLAYING);
            updateNotification(true);
        });
        player.setOnCompletionListener(mp -> stop());
        player.setOnErrorListener((mp, what, extra) -> {
            Log.e(Const.TAG, "Error in RadioService player: " + what);
            return true;
        });
    }

    private void setActions() {
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        intentFilter.addAction(ACTION_PLAY_PAUSE);
        intentFilter.addAction(ACTION_STOP);

        String pkg = getPackageName();
        flags = PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE;

        playPauseAction = new NotificationCompat.Action(R.drawable.ic_play_arrow, "play_pause",
                PendingIntent.getBroadcast(this, id,
                        new Intent(ACTION_PLAY_PAUSE).setPackage(pkg), flags));
    }

    private void startPlaying() {
        player.reset();

        thread.start();
    }

    // Other Links:
    // https://www.aloula.sa/83c0bda5-18e7-4c80-9c0a-21e764537d47
    // https://m.live.net.sa:1935/live/quransa/playlist.m3u8

    Thread thread = new Thread(() -> {
        try {    // A mechanism to handle redirects and get the final dynamic link
            Log.d(Const.TAG, "There");
            URL url = new URL(link);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            URL secondURL = new URL(connection.getHeaderField("Location"));
            link = secondURL.toString();
            link = link.replaceFirst("http:", "https:");
            Log.i(Const.TAG, "Dynamic Quran Radio URL: " + link);
            player.setDataSource(getApplicationContext(), Uri.parse(link));
            player.prepareAsync();
        } catch (IOException e) {
            Log.e(Const.TAG, "Problem in RadioService player");
            e.printStackTrace();
        }
    });

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name;
            String description = "quran radio";
            channelId = "QuranRadio";
            name = "إذاعة القرآن";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel  = new NotificationChannel(
                    channelId, name, importance);
            notificationChannel.setDescription(description);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private PendingIntent getContentIntent() {
        Intent intent = new Intent(this, RadioClient.class).setAction("back");

        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;

        return PendingIntent.getActivity(this, 37, intent, flags);
    }

    @Nullable
    @Override
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

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(Const.TAG, "In onUnbind of RadioService");
        return super.onUnbind(intent);
    }
}
