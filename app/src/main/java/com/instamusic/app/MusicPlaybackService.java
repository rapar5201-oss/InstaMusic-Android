package com.instamusic.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.LruCache;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.common.Player.Commands;
import androidx.media3.common.SimpleBasePlayer;
import androidx.media3.common.Tracks;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.session.DefaultMediaNotificationProvider;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;
import androidx.media3.session.SessionCommands;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@OptIn(markerClass = UnstableApi.class)
public class MusicPlaybackService extends MediaSessionService {
    public static final String CHANNEL_ID = "instamusic_playback_v2";
    public static final int NOTIFICATION_ID = 101;

    public static final String ACTION_UPDATE_METADATA = "com.instamusic.app.UPDATE_METADATA";
    public static final String ACTION_UPDATE_STATE = "com.instamusic.app.UPDATE_STATE";
    public static final String ACTION_UPDATE_POSITION = "com.instamusic.app.UPDATE_POSITION";
    public static final String ACTION_STOP = "com.instamusic.app.STOP";

    public static final String ACTION_PLAY = "com.instamusic.app.PLAY";
    public static final String ACTION_PAUSE = "com.instamusic.app.PAUSE";
    public static final String ACTION_PREV = "com.instamusic.app.PREV";
    public static final String ACTION_NEXT = "com.instamusic.app.NEXT";
    public static final String ACTION_SEEK = "com.instamusic.app.SEEK";

    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_ARTIST = "artist";
    public static final String EXTRA_ALBUM = "album";
    public static final String EXTRA_ARTWORK_URL = "artwork_url";
    public static final String EXTRA_UPCOMING_TITLE = "upcoming_title";
    public static final String EXTRA_UPCOMING_ARTIST = "upcoming_artist";
    public static final String EXTRA_IS_PLAYING = "is_playing";
    public static final String EXTRA_POSITION_MS = "position_ms";
    public static final String EXTRA_DURATION_MS = "duration_ms";
    public static final String EXTRA_SEEK_POSITION_MS = "seek_position_ms";

    private static final int MAX_CACHE_ENTRIES = 30;
    private final LruCache<String, byte[]> artworkDataCache = new LruCache<String, byte[]>(MAX_CACHE_ENTRIES) {
        @Override
        protected int sizeOf(String key, byte[] bytes) {
            return 1;
        }
    };

    private MediaSession mediaSession;
    private InstaMusicPlayer customPlayer;

    private String currentTitle = "INSTA MUSIC";
    private String currentArtist = "Playing";
    private String currentAlbum = "INSTA MUSIC";
    private String currentArtworkUrl = "";
    private byte[] currentArtworkData = null;
    private byte[] fallbackArtworkData = null;
    private String upcomingTitle = "";
    private String upcomingArtist = "";

    private boolean isPlaying = false;
    public static volatile boolean isMusicCurrentlyPlaying = false;
    private long currentPositionMs = 0L;
    private long currentDurationMs = 0L;
    private long lastPositionUpdateMs = 0L;

    private PowerManager.WakeLock wakeLock;
    private AudioManager audioManager;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final BroadcastReceiver noisyAudioReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                if (isPlaying) {
                    sendActionToApp(ACTION_PAUSE);
                    isPlaying = false;
                    isMusicCurrentlyPlaying = false;
                    releaseWakeLock();
                    requestPlayerStateUpdate();
                }
            }
        }
    };
    private boolean isNoisyReceiverRegistered = false;

    private final class InstaMusicPlayer extends SimpleBasePlayer {
        public InstaMusicPlayer(Looper looper) {
            super(looper);
        }

        public void refreshState() {
            invalidateState();
        }

        @Override
        protected State getState() {
            Commands commands = new Commands.Builder()
                .add(Player.COMMAND_PLAY_PAUSE)
                .add(Player.COMMAND_SEEK_TO_NEXT)
                .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                .add(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
                .add(Player.COMMAND_SEEK_TO_MEDIA_ITEM)
                .add(Player.COMMAND_SEEK_BACK)
                .add(Player.COMMAND_SEEK_FORWARD)
                .add(Player.COMMAND_STOP)
                .add(Player.COMMAND_GET_CURRENT_MEDIA_ITEM)
                .add(Player.COMMAND_GET_TIMELINE)
                .add(Player.COMMAND_GET_METADATA)
                .build();

            MediaMetadata.Builder metaBuilder = new MediaMetadata.Builder()
                .setTitle(currentTitle)
                .setArtist(currentArtist)
                .setAlbumTitle(currentAlbum)
                .setDisplayTitle(currentTitle);

            byte[] art = currentArtworkData != null ? currentArtworkData : fallbackArtworkData;
            if (art != null) {
                metaBuilder.setArtworkData(art, MediaMetadata.PICTURE_TYPE_FRONT_COVER);
            }

            MediaItem currentItem = new MediaItem.Builder()
                .setMediaId("instamusic_current_track")
                .setMediaMetadata(metaBuilder.build())
                .build();

            long durUs = currentDurationMs > 0L ? currentDurationMs * 1000L : C.TIME_UNSET;
            SimpleBasePlayer.MediaItemData currentData = new SimpleBasePlayer.MediaItemData.Builder("instamusic_current_track")
                .setMediaItem(currentItem)
                .setTracks(Tracks.EMPTY)
                .setDurationUs(durUs)
                .setIsSeekable(currentDurationMs > 0L)
                .build();

            List<SimpleBasePlayer.MediaItemData> playlist = new ArrayList<>();
            playlist.add(currentData);

            if (upcomingTitle != null && !upcomingTitle.trim().isEmpty()) {
                MediaMetadata nextMeta = new MediaMetadata.Builder()
                    .setTitle(upcomingTitle)
                    .setArtist(upcomingArtist != null && !upcomingArtist.trim().isEmpty() ? upcomingArtist : currentArtist)
                    .setAlbumTitle(currentAlbum)
                    .setDisplayTitle(upcomingTitle)
                    .build();

                MediaItem nextItem = new MediaItem.Builder()
                    .setMediaId("instamusic_next_track")
                    .setMediaMetadata(nextMeta)
                    .build();

                SimpleBasePlayer.MediaItemData nextData = new SimpleBasePlayer.MediaItemData.Builder("instamusic_next_track")
                    .setMediaItem(nextItem)
                    .setTracks(Tracks.EMPTY)
                    .setIsSeekable(true)
                    .build();

                playlist.add(nextData);
            }

            // In Media3, when a song is loaded, playbackState must remain STATE_READY even when paused.
            // Setting STATE_IDLE on pause would mislead lock screen, Bluetooth AVRCP, and Android Auto into thinking no track is ready.
            boolean hasTrackLoaded = (currentTitle != null && !currentTitle.trim().isEmpty() && !currentTitle.equalsIgnoreCase("INSTA MUSIC"));
            int playbackState = (hasTrackLoaded || isPlaying) ? Player.STATE_READY : Player.STATE_IDLE;

            return new State.Builder()
                .setAvailableCommands(commands)
                .setPlayWhenReady(isPlaying, Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST)
                .setPlaybackState(playbackState)
                .setPlaylist(playlist)
                .setCurrentMediaItemIndex(0)
                .setContentPositionMs(() -> {
                    if (isPlaying && lastPositionUpdateMs > 0L) {
                        long elapsed = SystemClock.elapsedRealtime() - lastPositionUpdateMs;
                        long pos = currentPositionMs + elapsed;
                        return currentDurationMs > 0L ? Math.min(pos, currentDurationMs) : pos;
                    }
                    return currentPositionMs;
                })
                .build();
        }

        @Override
        protected ListenableFuture<?> handleSetPlayWhenReady(boolean playWhenReady) {
            isPlaying = playWhenReady;
            isMusicCurrentlyPlaying = isPlaying;
            if (isPlaying) {
                acquireWakeLock();
                registerNoisyReceiver();
            } else {
                releaseWakeLock();
                unregisterNoisyReceiver();
            }
            sendActionToApp(playWhenReady ? ACTION_PLAY : ACTION_PAUSE);
            invalidateState();
            return Futures.immediateVoidFuture();
        }

        @Override
        protected ListenableFuture<?> handleSeek(int mediaItemIndex, long positionMs, int seekCommand) {
            if (seekCommand == Player.COMMAND_SEEK_TO_NEXT
                || seekCommand == Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM
                || (mediaItemIndex > 0 && seekCommand == Player.COMMAND_SEEK_TO_MEDIA_ITEM)) {
                sendActionToApp(ACTION_NEXT);
            } else if (seekCommand == Player.COMMAND_SEEK_TO_PREVIOUS
                       || seekCommand == Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM) {
                sendActionToApp(ACTION_PREV);
            } else if (seekCommand == Player.COMMAND_SEEK_FORWARD) {
                long targetPos = currentDurationMs > 0 ? Math.min(currentPositionMs + 10000L, currentDurationMs) : currentPositionMs + 10000L;
                sendSeekToApp(targetPos);
                currentPositionMs = targetPos;
                lastPositionUpdateMs = SystemClock.elapsedRealtime();
                invalidateState();
            } else if (seekCommand == Player.COMMAND_SEEK_BACK) {
                long targetPos = Math.max(0L, currentPositionMs - 10000L);
                sendSeekToApp(targetPos);
                currentPositionMs = targetPos;
                lastPositionUpdateMs = SystemClock.elapsedRealtime();
                invalidateState();
            } else if (seekCommand == Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM
                       || seekCommand == Player.COMMAND_SEEK_TO_MEDIA_ITEM) {
                sendSeekToApp(positionMs);
                currentPositionMs = positionMs;
                lastPositionUpdateMs = SystemClock.elapsedRealtime();
                invalidateState();
            }
            return Futures.immediateVoidFuture();
        }

        @Override
        protected ListenableFuture<?> handleStop() {
            isPlaying = false;
            isMusicCurrentlyPlaying = false;
            releaseWakeLock();
            unregisterNoisyReceiver();
            sendActionToApp(ACTION_PAUSE);
            invalidateState();
            return Futures.immediateVoidFuture();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        fallbackArtworkData = createFallbackArtworkBytes();
        currentArtworkData = fallbackArtworkData;

        customPlayer = new InstaMusicPlayer(Looper.getMainLooper());

        Intent openAppIntent = new Intent(this, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pOpenApp = PendingIntent.getActivity(
            this, 0, openAppIntent,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0
        );

        mediaSession = new MediaSession.Builder(this, customPlayer)
            .setSessionActivity(pOpenApp)
            .setCallback(new MediaSession.Callback() {
                @Override
                public MediaSession.ConnectionResult onConnect(MediaSession session, MediaSession.ControllerInfo controller) {
                    MediaSession.ConnectionResult defaultResult = MediaSession.Callback.super.onConnect(session, controller);
                    SessionCommands.Builder sessionCommands = defaultResult.availableSessionCommands.buildUpon();
                    Player.Commands.Builder playerCommands = defaultResult.availablePlayerCommands.buildUpon();
                    playerCommands.add(Player.COMMAND_PLAY_PAUSE);
                    playerCommands.add(Player.COMMAND_SEEK_TO_NEXT);
                    playerCommands.add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM);
                    playerCommands.add(Player.COMMAND_SEEK_TO_PREVIOUS);
                    playerCommands.add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM);
                    playerCommands.add(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM);
                    playerCommands.add(Player.COMMAND_SEEK_TO_MEDIA_ITEM);
                    playerCommands.add(Player.COMMAND_SEEK_BACK);
                    playerCommands.add(Player.COMMAND_SEEK_FORWARD);
                    playerCommands.add(Player.COMMAND_STOP);
                    return MediaSession.ConnectionResult.accept(sessionCommands.build(), playerCommands.build());
                }
            })
            .build();

        addSession(mediaSession);

        DefaultMediaNotificationProvider notificationProvider = new DefaultMediaNotificationProvider.Builder(this)
            .setChannelId(CHANNEL_ID)
            .setNotificationId(NOTIFICATION_ID)
            .build();
        notificationProvider.setSmallIcon(R.drawable.ic_music_note);
        setMediaNotificationProvider(notificationProvider);
    }

    @Nullable
    @Override
    public MediaSession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }

    private void requestPlayerStateUpdate() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (customPlayer != null) customPlayer.refreshState();
            try {
                triggerNotificationUpdate();
            } catch (Exception ignored) {}
        } else {
            mainHandler.post(() -> {
                if (customPlayer != null) customPlayer.refreshState();
                try {
                    triggerNotificationUpdate();
                } catch (Exception ignored) {}
            });
        }
    }

    private void acquireWakeLock() {
        try {
            if (wakeLock == null) {
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                if (pm != null) {
                    wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "InstaMusic:PlaybackWakeLock");
                    wakeLock.setReferenceCounted(false);
                }
            }
            if (wakeLock != null && !wakeLock.isHeld()) {
                wakeLock.acquire(180 * 60 * 1000L); // 3 hours max safety duration
            }
        } catch (Exception ignored) {}
    }

    private void releaseWakeLock() {
        try {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }
        } catch (Exception ignored) {}
    }



    private void registerNoisyReceiver() {
        if (!isNoisyReceiverRegistered) {
            try {
                IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
                registerReceiver(noisyAudioReceiver, filter);
                isNoisyReceiverRegistered = true;
            } catch (Exception ignored) {}
        }
    }

    private void unregisterNoisyReceiver() {
        if (isNoisyReceiverRegistered) {
            try {
                unregisterReceiver(noisyAudioReceiver);
            } catch (Exception ignored) {}
            isNoisyReceiverRegistered = false;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("InstaMusic playback controls and media notification");
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
            channel.enableVibration(false);
            channel.setSound(null, null);
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }

    private byte[] createFallbackArtworkBytes() {
        try {
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            if (icon != null) {
                return bitmapToByteArray(icon);
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

            return bitmapToByteArray(bmp);
        } catch (Exception e) {
            return null;
        }
    }

    private static byte[] bitmapToByteArray(Bitmap bitmap) {
        if (bitmap == null) return null;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
        return stream.toByteArray();
    }

    private void sendActionToApp(String action) {
        Intent intent = new Intent(action);
        intent.setPackage(getPackageName());
        sendBroadcast(intent);
    }

    private void sendSeekToApp(long positionMs) {
        Intent intent = new Intent(ACTION_SEEK);
        intent.putExtra(EXTRA_SEEK_POSITION_MS, positionMs);
        intent.setPackage(getPackageName());
        sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent == null || intent.getAction() == null) {
            requestPlayerStateUpdate();
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

            String newUpcomingTitle = intent.getStringExtra(EXTRA_UPCOMING_TITLE);
            upcomingTitle = newUpcomingTitle != null ? newUpcomingTitle.trim() : "";
            String newUpcomingArtist = intent.getStringExtra(EXTRA_UPCOMING_ARTIST);
            upcomingArtist = newUpcomingArtist != null ? newUpcomingArtist.trim() : "";

            if (intent.hasExtra(EXTRA_IS_PLAYING)) {
                isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, true);
                isMusicCurrentlyPlaying = isPlaying;
                if (isPlaying) {
                    acquireWakeLock();
                        registerNoisyReceiver();
                } else {
                    releaseWakeLock();
                        unregisterNoisyReceiver();
                }
            }

            String newArtUrl = intent.getStringExtra(EXTRA_ARTWORK_URL);
            if (newArtUrl != null && !newArtUrl.trim().isEmpty()) {
                if (!newArtUrl.equals(currentArtworkUrl)) {
                    currentArtworkUrl = newArtUrl;
                    currentArtworkData = fallbackArtworkData;
                    loadArtworkWithCache(currentArtworkUrl);
                }
            } else {
                currentArtworkUrl = "";
                currentArtworkData = fallbackArtworkData;
            }

            requestPlayerStateUpdate();
        } else if (ACTION_UPDATE_STATE.equals(action)) {
            isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, false);
            isMusicCurrentlyPlaying = isPlaying;
            if (isPlaying) {
                acquireWakeLock();
                registerNoisyReceiver();
            } else {
                releaseWakeLock();
                unregisterNoisyReceiver();
            }
            lastPositionUpdateMs = SystemClock.elapsedRealtime();
            requestPlayerStateUpdate();
        } else if (ACTION_UPDATE_POSITION.equals(action)) {
            currentPositionMs = Math.max(0L, intent.getLongExtra(EXTRA_POSITION_MS, currentPositionMs));
            currentDurationMs = Math.max(0L, intent.getLongExtra(EXTRA_DURATION_MS, currentDurationMs));
            if (intent.hasExtra(EXTRA_IS_PLAYING)) {
                isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, isPlaying);
                isMusicCurrentlyPlaying = isPlaying;
                if (isPlaying) {
                    acquireWakeLock();
                        registerNoisyReceiver();
                } else {
                    releaseWakeLock();
                        unregisterNoisyReceiver();
                }
            }
            lastPositionUpdateMs = SystemClock.elapsedRealtime();
            requestPlayerStateUpdate();
        } else if (ACTION_STOP.equals(action)) {
            isPlaying = false;
            isMusicCurrentlyPlaying = false;
            releaseWakeLock();
            unregisterNoisyReceiver();
            stopSelf();
            return START_NOT_STICKY;
        } else if (ACTION_PLAY.equals(action)) {
            sendActionToApp(ACTION_PLAY);
            isPlaying = true;
            isMusicCurrentlyPlaying = true;
            acquireWakeLock();
            registerNoisyReceiver();
            requestPlayerStateUpdate();
        } else if (ACTION_PAUSE.equals(action)) {
            sendActionToApp(ACTION_PAUSE);
            isPlaying = false;
            isMusicCurrentlyPlaying = false;
            releaseWakeLock();
            unregisterNoisyReceiver();
            requestPlayerStateUpdate();
        } else if (ACTION_PREV.equals(action)) {
            sendActionToApp(ACTION_PREV);
        } else if (ACTION_NEXT.equals(action)) {
            sendActionToApp(ACTION_NEXT);
        }

        return START_STICKY;
    }

    private void loadArtworkWithCache(final String urlStr) {
        if (urlStr == null || urlStr.trim().isEmpty()) {
            currentArtworkData = fallbackArtworkData;
            requestPlayerStateUpdate();
            return;
        }

        byte[] cached = artworkDataCache.get(urlStr);
        if (cached != null) {
            currentArtworkData = cached;
            requestPlayerStateUpdate();
            return;
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
                    byte[] data = bitmapToByteArray(bmp);
                    if (data != null) {
                        artworkDataCache.put(requestedUrl, data);
                        if (requestedUrl.equals(currentArtworkUrl)) {
                            currentArtworkData = data;
                            requestPlayerStateUpdate();
                        }
                    }
                }
            } catch (Exception e) {
                if (currentArtworkData == null) {
                    currentArtworkData = fallbackArtworkData;
                    requestPlayerStateUpdate();
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

    @Override
    public void onDestroy() {
        releaseWakeLock();
        unregisterNoisyReceiver();
        if (mediaSession != null) {
            removeSession(mediaSession);
            mediaSession.release();
            mediaSession = null;
        }
        if (customPlayer != null) {
            customPlayer.release();
            customPlayer = null;
        }
        artworkDataCache.evictAll();
        isMusicCurrentlyPlaying = false;
        super.onDestroy();
    }
}
