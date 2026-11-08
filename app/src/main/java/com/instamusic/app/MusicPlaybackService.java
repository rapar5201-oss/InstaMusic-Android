package com.instamusic.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.IBinder;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MusicPlaybackService extends Service {
    public static final String CHANNEL_ID = "instamusic_playback";
    public static final int NOTIFICATION_ID = 101;

    public static final String ACTION_UPDATE_METADATA = "com.instamusic.app.UPDATE_METADATA";
    public static final String ACTION_UPDATE_STATE = "com.instamusic.app.UPDATE_STATE";
    public static final String ACTION_STOP = "com.instamusic.app.STOP";

    public static final String ACTION_PLAY = "com.instamusic.app.PLAY";
    public static final String ACTION_PAUSE = "com.instamusic.app.PAUSE";
    public static final String ACTION_PREV = "com.instamusic.app.PREV";
    public static final String ACTION_NEXT = "com.instamusic.app.NEXT";

    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_ARTIST = "artist";
    public static final String EXTRA_ALBUM = "album";
    public static final String EXTRA_ARTWORK_URL = "artwork_url";
    public static final String EXTRA_IS_PLAYING = "is_playing";

    private MediaSession mediaSession;
    private String currentTitle = "INSTA MUSIC";
    private String currentArtist = "Playing in background";
    private String currentAlbum = "INSTA MUSIC";
    private String currentArtworkUrl = "";
    private Bitmap currentArtwork = null;
    private boolean isPlaying = false;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        initMediaSession();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("InstaMusic playback controls and media notification");
            channel.setShowBadge(false);
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }

    private void initMediaSession() {
        mediaSession = new MediaSession(this, "InstaMusicSession");
        mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() {
                sendActionToApp(ACTION_PLAY);
            }
            @Override
            public void onPause() {
                sendActionToApp(ACTION_PAUSE);
            }
            @Override
            public void onSkipToNext() {
                sendActionToApp(ACTION_NEXT);
            }
            @Override
            public void onSkipToPrevious() {
                sendActionToApp(ACTION_PREV);
            }
            @Override
            public void onStop() {
                stopForeground(true);
                stopSelf();
            }
        });
        mediaSession.setActive(true);
    }

    private void sendActionToApp(String action) {
        Intent intent = new Intent(action);
        intent.setPackage(getPackageName());
        sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            return START_STICKY;
        }

        String action = intent.getAction();
        if (ACTION_UPDATE_METADATA.equals(action)) {
            currentTitle = intent.getStringExtra(EXTRA_TITLE);
            if (currentTitle == null || currentTitle.trim().isEmpty()) currentTitle = "INSTA MUSIC";
            currentArtist = intent.getStringExtra(EXTRA_ARTIST);
            if (currentArtist == null || currentArtist.trim().isEmpty()) currentArtist = "Streaming";
            currentAlbum = intent.getStringExtra(EXTRA_ALBUM);
            if (currentAlbum == null || currentAlbum.trim().isEmpty()) currentAlbum = "INSTA MUSIC";
            String newArt = intent.getStringExtra(EXTRA_ARTWORK_URL);
            if (newArt != null && !newArt.equals(currentArtworkUrl)) {
                currentArtworkUrl = newArt;
                loadArtworkAsync(currentArtworkUrl);
            }
            if (intent.hasExtra(EXTRA_IS_PLAYING)) {
                isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, true);
            }
            updateNotification();
        } else if (ACTION_UPDATE_STATE.equals(action)) {
            isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, false);
            updateNotification();
        } else if (ACTION_STOP.equals(action)) {
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        } else if (ACTION_PLAY.equals(action)) {
            sendActionToApp(ACTION_PLAY);
            isPlaying = true;
            updateNotification();
        } else if (ACTION_PAUSE.equals(action)) {
            sendActionToApp(ACTION_PAUSE);
            isPlaying = false;
            updateNotification();
        } else if (ACTION_PREV.equals(action)) {
            sendActionToApp(ACTION_PREV);
        } else if (ACTION_NEXT.equals(action)) {
            sendActionToApp(ACTION_NEXT);
        }

        return START_STICKY;
    }

    private void loadArtworkAsync(String urlStr) {
        if (urlStr == null || urlStr.isEmpty()) return;
        new Thread(() -> {
            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(4000);
                conn.setReadTimeout(4000);
                conn.setDoInput(true);
                conn.connect();
                InputStream is = conn.getInputStream();
                Bitmap bmp = BitmapFactory.decodeStream(is);
                if (bmp != null) {
                    currentArtwork = bmp;
                    updateNotification();
                }
            } catch (Exception ignored) {}
        }).start();
    }

    private void updateNotification() {
        if (mediaSession == null) return;

        long actions = PlaybackState.ACTION_PLAY
            | PlaybackState.ACTION_PAUSE
            | PlaybackState.ACTION_SKIP_TO_NEXT
            | PlaybackState.ACTION_SKIP_TO_PREVIOUS
            | PlaybackState.ACTION_STOP;

        int state = isPlaying ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED;
        PlaybackState.Builder stateBuilder = new PlaybackState.Builder()
            .setActions(actions)
            .setState(state, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1.0f);
        mediaSession.setPlaybackState(stateBuilder.build());

        MediaMetadata.Builder metaBuilder = new MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, currentTitle)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, currentArtist)
            .putString(MediaMetadata.METADATA_KEY_ALBUM, currentAlbum);
        if (currentArtwork != null) {
            metaBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, currentArtwork);
            metaBuilder.putBitmap(MediaMetadata.METADATA_KEY_ART, currentArtwork);
        }
        mediaSession.setMetadata(metaBuilder.build());

        Intent openAppIntent = new Intent(this, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pOpenApp = PendingIntent.getActivity(
            this, 0, openAppIntent,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0
        );

        PendingIntent pPrev = PendingIntent.getService(
            this, 1, new Intent(this, MusicPlaybackService.class).setAction(ACTION_PREV),
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0
        );

        Intent playPauseIntent = new Intent(this, MusicPlaybackService.class)
            .setAction(isPlaying ? ACTION_PAUSE : ACTION_PLAY);
        PendingIntent pPlayPause = PendingIntent.getService(
            this, 2, playPauseIntent,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT
        );

        PendingIntent pNext = PendingIntent.getService(
            this, 3, new Intent(this, MusicPlaybackService.class).setAction(ACTION_NEXT),
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0
        );

        Notification.Builder nb;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nb = new Notification.Builder(this, CHANNEL_ID);
        } else {
            nb = new Notification.Builder(this);
        }

        nb.setContentTitle(currentTitle)
          .setContentText(currentArtist)
          .setSubText(currentAlbum)
          .setSmallIcon(R.drawable.ic_music_note)
          .setContentIntent(pOpenApp)
          .setVisibility(Notification.VISIBILITY_PUBLIC)
          .setOngoing(isPlaying)
          .addAction(R.drawable.ic_prev, "Previous", pPrev)
          .addAction(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play, isPlaying ? "Pause" : "Play", pPlayPause)
          .addAction(R.drawable.ic_next, "Next", pNext);

        if (currentArtwork != null) {
            nb.setLargeIcon(currentArtwork);
        }

        Notification.MediaStyle style = new Notification.MediaStyle();
        style.setMediaSession(mediaSession.getSessionToken());
        style.setShowActionsInCompactView(0, 1, 2);
        nb.setStyle(style);

        Notification notification = nb.build();
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        if (mediaSession != null) {
            mediaSession.setActive(false);
            mediaSession.release();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}