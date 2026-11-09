package com.instamusic.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.LruCache;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MusicPlaybackService extends Service {
    public static final String CHANNEL_ID = "instamusic_playback";
    public static final int NOTIFICATION_ID = 101;

    public static final String ACTION_UPDATE_METADATA = "com.instamusic.app.UPDATE_METADATA";
    public static final String ACTION_UPDATE_STATE = "com.instamusic.app.UPDATE_STATE";
    public static final String ACTION_UPDATE_POSITION = "com.instamusic.app.UPDATE_POSITION";
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
    public static final String EXTRA_POSITION_MS = "position_ms";
    public static final String EXTRA_DURATION_MS = "duration_ms";

    private static final int MAX_CACHE_ENTRIES = 30;
    private final LruCache<String, Bitmap> artworkCache = new LruCache<String, Bitmap>(MAX_CACHE_ENTRIES) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            return 1;
        }
    };

    private MediaSession mediaSession;
    private String currentTitle = "INSTA MUSIC";
    private String currentArtist = "Playing";
    private String currentAlbum = "INSTA MUSIC";
    private String currentArtworkUrl = "";
    private Bitmap currentArtwork = null;
    private Bitmap fallbackArtwork = null;
    private boolean isPlaying = false;
    private long currentPositionMs = 0L;
    private long currentDurationMs = 0L;
    private long lastPositionUpdateMs = 0L;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        initMediaSession();
        fallbackArtwork = createFallbackArtwork();
        currentArtwork = fallbackArtwork;

        Notification initialNotification = buildNotification();
        startForegroundCompat(initialNotification);
    }

    private Bitmap createFallbackArtwork() {
        try {
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            if (icon != null) {
                return icon;
            }
        } catch (Exception ignored) {}

        try {
            Bitmap bmp = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            paint.setColor(0xFF14171E);
            canvas.drawRect(0, 0, 300, 300, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(10f);
            paint.setColor(0x44FF6B35);
            canvas.drawCircle(150, 150, 110, paint);

            paint.setColor(0x88FF6B35);
            paint.setStrokeWidth(12f);
            canvas.drawCircle(150, 150, 75, paint);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFFFF5722);
            canvas.drawCircle(150, 150, 32, paint);

            paint.setColor(0xFFFFFFFF);
            canvas.drawCircle(150, 150, 10, paint);

            return bmp;
        } catch (Exception e) {
            return null;
        }
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
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.enableVibration(false);
            channel.setSound(null, null);
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
            updateNotification();
            return START_STICKY;
        }

        String action = intent.getAction();
        if (ACTION_UPDATE_METADATA.equals(action)) {
            String newTitle = intent.getStringExtra(EXTRA_TITLE);
            if (newTitle != null && !newTitle.trim().isEmpty()) currentTitle = newTitle;
            String newArtist = intent.getStringExtra(EXTRA_ARTIST);
            if (newArtist != null && !newArtist.trim().isEmpty()) currentArtist = newArtist;
            String newAlbum = intent.getStringExtra(EXTRA_ALBUM);
            if (newAlbum != null && !newAlbum.trim().isEmpty()) currentAlbum = newAlbum;

            String newArtUrl = intent.getStringExtra(EXTRA_ARTWORK_URL);
            if (intent.hasExtra(EXTRA_IS_PLAYING)) {
                isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, true);
            }

            if (newArtUrl != null && !newArtUrl.trim().isEmpty()) {
                if (!newArtUrl.equals(currentArtworkUrl)) {
                    currentArtworkUrl = newArtUrl;
                    // Switch to fallback immediately so the previous track's art never lingers.
                    currentArtwork = fallbackArtwork;
                    loadArtworkWithCache(currentArtworkUrl);
                }
            } else {
                currentArtworkUrl = "";
                currentArtwork = fallbackArtwork;
            }
            updateNotification();
        } else if (ACTION_UPDATE_STATE.equals(action)) {
            isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, false);
            lastPositionUpdateMs = android.os.SystemClock.elapsedRealtime();
            updateNotification();
        } else if (ACTION_UPDATE_POSITION.equals(action)) {
            currentPositionMs = Math.max(0L, intent.getLongExtra(EXTRA_POSITION_MS, currentPositionMs));
            currentDurationMs = Math.max(0L, intent.getLongExtra(EXTRA_DURATION_MS, currentDurationMs));
            isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, isPlaying);
            lastPositionUpdateMs = android.os.SystemClock.elapsedRealtime();
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

    private void loadArtworkWithCache(final String urlStr) {
        if (urlStr == null || urlStr.trim().isEmpty()) {
            currentArtwork = fallbackArtwork;
            updateNotification();
            return;
        }

        Bitmap cached = artworkCache.get(urlStr);
        if (cached != null) {
            currentArtwork = cached;
            updateNotification();
            return;
        }

        if (currentArtwork == null) {
            currentArtwork = fallbackArtwork;
        }

        final String requestedUrl = urlStr;
        new Thread(() -> {
            HttpURLConnection conn = null;
            InputStream is = null;
            try {
                Bitmap bmp = null;
                if (requestedUrl.startsWith("data:image/")) {
                    int comma = requestedUrl.indexOf(',');
                    if (comma != -1) {
                        byte[] bytes = android.util.Base64.decode(requestedUrl.substring(comma + 1), android.util.Base64.DEFAULT);
                        bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    }
                } else {
                    URL url = new URL(requestedUrl);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("User-Agent", "InstaMusic/1.0 (Android)");
                    conn.setInstanceFollowRedirects(true);
                    conn.setConnectTimeout(6000);
                    conn.setReadTimeout(6000);
                    conn.setDoInput(true);
                    conn.connect();
                    is = conn.getInputStream();
                    bmp = BitmapFactory.decodeStream(is);
                }
                if (bmp != null) {
                    int maxDim = 512;
                    int w = bmp.getWidth();
                    int h = bmp.getHeight();
                    if (w > maxDim || h > maxDim) {
                        float ratio = Math.min((float) maxDim / w, (float) maxDim / h);
                        int targetW = Math.max(1, Math.round(w * ratio));
                        int targetH = Math.max(1, Math.round(h * ratio));
                        bmp = Bitmap.createScaledBitmap(bmp, targetW, targetH, true);
                    }
                    artworkCache.put(requestedUrl, bmp);
                    if (requestedUrl.equals(currentArtworkUrl)) {
                        currentArtwork = bmp;
                        mainHandler.post(this::updateNotification);
                    }
                }
            } catch (Exception e) {
                if (currentArtwork == null) {
                    currentArtwork = fallbackArtwork;
                    mainHandler.post(this::updateNotification);
                }
            } finally {
                if (is != null) {
                    try { is.close(); } catch (Exception ignored) {}
                }
                if (conn != null) {
                    try { conn.disconnect(); } catch (Exception ignored) {}
                }
            }
        }).start();
    }

    private Notification buildNotification() {
        if (mediaSession != null) {
            long actions = PlaybackState.ACTION_PLAY
                | PlaybackState.ACTION_PAUSE
                | PlaybackState.ACTION_SKIP_TO_NEXT
                | PlaybackState.ACTION_SKIP_TO_PREVIOUS
                | PlaybackState.ACTION_STOP;

            int state = isPlaying ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED;
            long position = currentPositionMs;
            if (isPlaying && lastPositionUpdateMs > 0L) {
                position += Math.max(0L, android.os.SystemClock.elapsedRealtime() - lastPositionUpdateMs);
            }
            if (currentDurationMs > 0L) position = Math.min(position, currentDurationMs);
            PlaybackState.Builder stateBuilder = new PlaybackState.Builder()
                .setActions(actions)
                .setState(state, position, isPlaying ? 1.0f : 0.0f);
            mediaSession.setPlaybackState(stateBuilder.build());

            Bitmap artToUse = currentArtwork != null ? currentArtwork : fallbackArtwork;
            MediaMetadata.Builder metaBuilder = new MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, currentTitle)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, currentArtist)
                .putString(MediaMetadata.METADATA_KEY_ALBUM, currentAlbum);
            if (currentDurationMs > 0L) {
                metaBuilder.putLong(MediaMetadata.METADATA_KEY_DURATION, currentDurationMs);
            }
            if (artToUse != null) {
                metaBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, artToUse);
                metaBuilder.putBitmap(MediaMetadata.METADATA_KEY_ART, artToUse);
            }
            mediaSession.setMetadata(metaBuilder.build());
        }

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

        Bitmap art = currentArtwork != null ? currentArtwork : fallbackArtwork;

        nb.setContentTitle(currentTitle)
          .setContentText(currentArtist + "  •  " + formatTime(currentPositionMs) + " / " + formatTime(currentDurationMs))
          .setSubText(currentAlbum)
          .setSmallIcon(R.drawable.ic_music_note)
          .setContentIntent(pOpenApp)
          .setVisibility(Notification.VISIBILITY_PUBLIC)
          .setCategory(Notification.CATEGORY_TRANSPORT)
          .setOngoing(isPlaying)
          .setShowWhen(false)
          .addAction(R.drawable.ic_prev, "Previous", pPrev)
          .addAction(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play, isPlaying ? "Pause" : "Play", pPlayPause)
          .addAction(R.drawable.ic_next, "Next", pNext);

        if (art != null) {
            nb.setLargeIcon(art);
        }

        Notification.MediaStyle style = new Notification.MediaStyle();
        if (mediaSession != null) {
            style.setMediaSession(mediaSession.getSessionToken());
        }
        style.setShowActionsInCompactView(0, 1, 2);
        nb.setStyle(style);

        return nb.build();
    }

    private String formatTime(long millis) {
        long totalSeconds = Math.max(0L, millis) / 1000L;
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        return String.format(java.util.Locale.ROOT, "%02d:%02d", minutes, seconds);
    }

    private void updateNotification() {
        Notification notification = buildNotification();
        startForegroundCompat(notification);
    }

    private void startForegroundCompat(Notification notification) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
            } else {
                startForeground(NOTIFICATION_ID, notification);
            }
        } catch (Exception e) {
            try {
                startForeground(NOTIFICATION_ID, notification);
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void onDestroy() {
        if (mediaSession != null) {
            mediaSession.setActive(false);
            mediaSession.release();
        }
        artworkCache.evictAll();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
