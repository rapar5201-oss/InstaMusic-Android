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
import androidx.media3.session.LibraryResult;
import androidx.media3.session.MediaLibraryService;
import androidx.media3.session.MediaSession;
import androidx.media3.session.SessionCommands;
import com.google.common.collect.ImmutableList;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@OptIn(markerClass = UnstableApi.class)
public class MusicPlaybackService extends MediaLibraryService {
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
    public static final String ACTION_PLAY_TRACK = "com.instamusic.app.PLAY_TRACK";

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

    private MediaLibrarySession mediaSession;
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
            if (currentArtworkUrl != null && !currentArtworkUrl.trim().isEmpty() && currentArtworkUrl.startsWith("http")) {
                try {
                    metaBuilder.setArtworkUri(android.net.Uri.parse(currentArtworkUrl));
                } catch (Exception ignored) {}
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

        MediaLibrarySession.Callback librarySessionCallback = new MediaLibrarySession.Callback() {
            @Override
            public MediaSession.ConnectionResult onConnect(MediaSession session, MediaSession.ControllerInfo controller) {
                MediaSession.ConnectionResult defaultResult = MediaLibrarySession.Callback.super.onConnect(session, controller);
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
                playerCommands.add(Player.COMMAND_SET_MEDIA_ITEM);
                playerCommands.add(Player.COMMAND_CHANGE_MEDIA_ITEMS);
                return MediaSession.ConnectionResult.accept(sessionCommands.build(), playerCommands.build());
            }

            @Override
            public ListenableFuture<LibraryResult<MediaItem>> onGetLibraryRoot(
                    MediaLibrarySession session,
                    MediaSession.ControllerInfo browser,
                    @Nullable LibraryParams params) {
                MediaMetadata rootMeta = new MediaMetadata.Builder()
                        .setTitle("InstaMusic")
                        .setIsBrowsable(true)
                        .setIsPlayable(false)
                        .setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
                        .build();

                MediaItem rootItem = new MediaItem.Builder()
                        .setMediaId("ROOT")
                        .setMediaMetadata(rootMeta)
                        .build();

                return Futures.immediateFuture(LibraryResult.ofItem(rootItem, params));
            }

            @Override
            public ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> onGetChildren(
                    MediaLibrarySession session,
                    MediaSession.ControllerInfo browser,
                    String parentId,
                    int page,
                    int pageSize,
                    @Nullable LibraryParams params) {
                List<MediaItem> items = getMediaItemsForParent(parentId);
                return Futures.immediateFuture(LibraryResult.ofItemList(ImmutableList.copyOf(items), params));
            }

            @Override
            public ListenableFuture<LibraryResult<MediaItem>> onGetItem(
                    MediaLibrarySession session,
                    MediaSession.ControllerInfo browser,
                    String mediaId) {
                MediaItem item = findMediaItemById(mediaId);
                if (item != null) {
                    return Futures.immediateFuture(LibraryResult.ofItem(item, null));
                }
                return Futures.immediateFuture(LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE));
            }

            @Override
            public ListenableFuture<LibraryResult<Void>> onSubscribe(
                    MediaLibrarySession session,
                    MediaSession.ControllerInfo browser,
                    String parentId,
                    @Nullable LibraryParams params) {
                return Futures.immediateFuture(LibraryResult.ofVoid());
            }

            @Override
            public ListenableFuture<List<MediaItem>> onAddMediaItems(
                    MediaSession mediaSession,
                    MediaSession.ControllerInfo controller,
                    List<MediaItem> mediaItems) {
                if (mediaItems != null && !mediaItems.isEmpty()) {
                    handleTrackSelectedFromCar(mediaItems.get(0));
                }
                return Futures.immediateFuture(mediaItems);
            }

            @Override
            public ListenableFuture<MediaSession.MediaItemsWithStartPosition> onSetMediaItems(
                    MediaSession mediaSession,
                    MediaSession.ControllerInfo controller,
                    List<MediaItem> mediaItems,
                    int startIndex,
                    long startPositionMs) {
                if (mediaItems != null && !mediaItems.isEmpty()) {
                    int idx = (startIndex >= 0 && startIndex < mediaItems.size()) ? startIndex : 0;
                    handleTrackSelectedFromCar(mediaItems.get(idx));
                }
                return MediaLibrarySession.Callback.super.onSetMediaItems(mediaSession, controller, mediaItems, startIndex, startPositionMs);
            }

            @Override
            public ListenableFuture<LibraryResult<Void>> onSearch(
                    MediaLibrarySession session,
                    MediaSession.ControllerInfo browser,
                    String query,
                    @Nullable LibraryParams params) {
                int count = searchTracks(query).size();
                session.notifySearchResultChanged(browser, query, count, params);
                return Futures.immediateFuture(LibraryResult.ofVoid(params));
            }

            @Override
            public ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> onGetSearchResult(
                    MediaLibrarySession session,
                    MediaSession.ControllerInfo browser,
                    String query,
                    int page,
                    int pageSize,
                    @Nullable LibraryParams params) {
                List<MediaItem> all = searchTracks(query);
                int fromIndex = Math.max(0, page * pageSize);
                int toIndex = Math.min(all.size(), fromIndex + pageSize);
                List<MediaItem> sub = (fromIndex < all.size()) ? all.subList(fromIndex, toIndex) : new ArrayList<>();
                return Futures.immediateFuture(LibraryResult.ofItemList(sub, params));
            }

            @Override
            public ListenableFuture<MediaSession.MediaItemsWithStartPosition> onPlaybackResumption(
                    MediaSession mediaSession,
                    MediaSession.ControllerInfo controller) {
                List<MediaItem> tracks = getOldHitTracks();
                if (tracks.isEmpty()) tracks = getFeaturedTracks();
                return Futures.immediateFuture(new MediaSession.MediaItemsWithStartPosition(tracks, 0, 0L));
            }
        };

        mediaSession = new MediaLibrarySession.Builder(this, customPlayer, librarySessionCallback)
            .setSessionActivity(pOpenApp)
            .setId("InstaMusicSession")
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
    public MediaLibrarySession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }

    private void requestPlayerStateUpdate() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (customPlayer != null) customPlayer.refreshState();
        } else {
            mainHandler.post(() -> {
                if (customPlayer != null) customPlayer.refreshState();
            });
        }
    }

    private final Runnable releaseWakeLockRunnable = this::releaseWakeLockNow;

    private void acquireWakeLock() {
        mainHandler.removeCallbacks(releaseWakeLockRunnable);
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
        // Keep CPU awake for 25s grace period during track-to-track transitions & autoplay
        mainHandler.removeCallbacks(releaseWakeLockRunnable);
        mainHandler.postDelayed(releaseWakeLockRunnable, 25000L);
    }

    private void releaseWakeLockNow() {
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

    private List<MediaItem> searchTracks(String query) {
        List<MediaItem> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return getOldHitTracks();
        }
        String q = query.toLowerCase().trim();
        List<MediaItem> pool = new ArrayList<>();
        pool.addAll(getOldHitTracks());
        pool.addAll(getFeaturedTracks());

        for (MediaItem item : pool) {
            if (item.mediaMetadata != null) {
                String t = item.mediaMetadata.title != null ? item.mediaMetadata.title.toString().toLowerCase() : "";
                String a = item.mediaMetadata.artist != null ? item.mediaMetadata.artist.toString().toLowerCase() : "";
                String al = item.mediaMetadata.albumTitle != null ? item.mediaMetadata.albumTitle.toString().toLowerCase() : "";
                if (t.contains(q) || a.contains(q) || al.contains(q)) {
                    results.add(item);
                }
            }
        }
        return results.isEmpty() ? getOldHitTracks() : results;
    }

    private List<MediaItem> getMediaItemsForParent(String parentId) {
        List<MediaItem> items = new ArrayList<>();
        if (parentId == null || parentId.isEmpty() || "ROOT".equalsIgnoreCase(parentId) || "/".equals(parentId) || parentId.startsWith("ROOT")) {
            items.add(createCategoryItem("CAT_RECENT", "Recently Played", "Your recent listening history", MediaMetadata.FOLDER_TYPE_TITLES));
            items.add(createCategoryItem("CAT_OLD_HITS", "Old Hits & Evergreen Classics", "Timeless 70s, 80s, 90s & Golden Era", MediaMetadata.FOLDER_TYPE_PLAYLISTS));
            items.add(createCategoryItem("CAT_TOP_HITS", "Top Hits & Trending", "Most popular tracks on InstaMusic", MediaMetadata.FOLDER_TYPE_PLAYLISTS));
            items.add(createCategoryItem("CAT_MOOD", "AI Mood Mixes", "Chill, Energy, Focus and Party vibes", MediaMetadata.FOLDER_TYPE_PLAYLISTS));
            items.add(createCategoryItem("CAT_FAVORITES", "Favorites & Library", "Saved songs and top tracks", MediaMetadata.FOLDER_TYPE_TITLES));
        } else if ("CAT_OLD_HITS".equalsIgnoreCase(parentId)) {
            items.addAll(getOldHitTracks());
        } else if ("CAT_RECENT".equalsIgnoreCase(parentId)) {
            List<MusicHistoryDbHelper.TrackInfo> recent = MusicHistoryDbHelper.getInstance(this).getRecentTracks(20);
            if (recent != null && !recent.isEmpty()) {
                for (MusicHistoryDbHelper.TrackInfo track : recent) {
                    items.add(createPlayableItem("recent_" + track.id, track.title, track.artist, track.album, track.artworkUrl));
                }
            } else {
                items.addAll(getFeaturedTracks());
            }
        } else if ("CAT_TOP_HITS".equalsIgnoreCase(parentId)) {
            items.addAll(getFeaturedTracks());
        } else if ("CAT_MOOD".equalsIgnoreCase(parentId)) {
            items.add(createPlayableItem("mood_1", "Tamil Vibe Station", "Anirudh, AR Rahman, Yuvan", "AI Mood Mix", "https://c.saavncdn.com/222/Leo-Tamil-2023-20231019104033-500x500.jpg"));
            items.add(createPlayableItem("mood_2", "Late Night Chill", "InstaMusic AI", "Chillout", "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=500&h=500&fit=crop"));
            items.add(createPlayableItem("mood_3", "High Energy Workout", "InstaMusic AI", "Workout", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&h=500&fit=crop"));
            items.add(createPlayableItem("mood_4", "Focus & Deep Work", "InstaMusic AI", "Ambient", "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&h=500&fit=crop"));
        } else if ("CAT_FAVORITES".equalsIgnoreCase(parentId)) {
            MusicHistoryDbHelper.TrackInfo fav = MusicHistoryDbHelper.getInstance(this).getFavoriteOrRecentTrack();
            if (fav != null) {
                items.add(createPlayableItem("fav_" + fav.id, fav.title, fav.artist, fav.album, fav.artworkUrl));
            }
            items.addAll(getFeaturedTracks());
        }
        return items;
    }

    private List<MediaItem> getOldHitTracks() {
        List<MediaItem> list = new ArrayList<>();
        list.add(createPlayableItem("old_1", "Pal Pal Dil Ke Paas", "Kishore Kumar", "Blackmail", "https://c.saavncdn.com/112/Blackmail-Hindi-1973-20221124115124-500x500.jpg"));
        list.add(createPlayableItem("old_2", "Tujhe Dekha To", "Lata Mangeshkar, Kumar Sanu", "Dilwale Dulhania Le Jayenge", "https://c.saavncdn.com/326/Dilwale-Dulhania-Le-Jayenge-Hindi-1995-20200924151745-500x500.jpg"));
        list.add(createPlayableItem("old_3", "Jotheyali Jothe Jotheyali", "S.P. Balasubrahmanyam, S. Janaki", "Geetha - Ilaiyaraaja", "https://c.saavncdn.com/978/Geetha-Kannada-1981-20200806085532-500x500.jpg"));
        list.add(createPlayableItem("old_4", "O Mere Dil Ke Chain", "Kishore Kumar, R.D. Burman", "Mere Jeevan Saathi", "https://c.saavncdn.com/393/Mere-Jeevan-Saathi-Hindi-1972-20221124115124-500x500.jpg"));
        list.add(createPlayableItem("old_5", "Chura Liya Hai Tumne Jo Dil Ko", "Asha Bhosle, Mohammed Rafi, R.D. Burman", "Yaadon Ki Baaraat", "https://c.saavncdn.com/123/Yaadon-Ki-Baaraat-Hindi-1973-20221124115124-500x500.jpg"));
        list.add(createPlayableItem("old_6", "Chinna Chinna Aasai", "Minmini, A.R. Rahman", "Roja", "https://c.saavncdn.com/001/Roja-Tamil-1992-20191024072834-500x500.jpg"));
        list.add(createPlayableItem("old_7", "Deepangal Pesum", "S.P.B. Charan, Sandhya, Ilaiyaraaja", "Devathai", "https://c.saavncdn.com/492/Devathai-Tamil-1997-20200806112234-500x500.jpg"));
        list.add(createPlayableItem("old_8", "Bohemian Rhapsody", "Queen", "A Night at the Opera", "https://c.saavncdn.com/514/A-Night-At-The-Opera-English-1975-20191206121434-500x500.jpg"));
        list.add(createPlayableItem("old_9", "Billie Jean", "Michael Jackson", "Thriller", "https://c.saavncdn.com/832/Thriller-English-1982-20200806141434-500x500.jpg"));
        return list;
    }

    private List<MediaItem> getFeaturedTracks() {
        List<MediaItem> list = new ArrayList<>();
        list.add(createPlayableItem("feat_1", "Hukum - Thalaivar Alappara", "Anirudh Ravichander", "Jailer", "https://c.saavncdn.com/264/Jailer-Tamil-2023-20230728194411-500x500.jpg"));
        list.add(createPlayableItem("feat_2", "Arabic Kuthu - Beast Mode", "Anirudh Ravichander, Jonita Gandhi", "Beast", "https://c.saavncdn.com/352/Arabic-Kuthu-From-Beast-Tamil-2022-20220214174005-500x500.jpg"));
        list.add(createPlayableItem("feat_3", "Naa Ready", "Thalapathy Vijay, Anirudh", "Leo", "https://c.saavncdn.com/222/Leo-Tamil-2023-20231019104033-500x500.jpg"));
        list.add(createPlayableItem("feat_4", "Kaavaalaa", "Shilpa Rao, Anirudh Ravichander", "Jailer", "https://c.saavncdn.com/264/Jailer-Tamil-2023-20230728194411-500x500.jpg"));
        list.add(createPlayableItem("feat_5", "Enjoy Enjaami", "Dhee ft. Arivu, Santhosh Narayanan", "Enjoy Enjaami", "https://c.saavncdn.com/022/Enjoy-Enjaami-Tamil-2021-20210305141011-500x500.jpg"));
        list.add(createPlayableItem("feat_6", "Vathi Coming", "Anirudh Ravichander, Gana Balachandar", "Master", "https://c.saavncdn.com/835/Master-Tamil-2020-20200316091404-500x500.jpg"));
        return list;
    }

    private MediaItem createCategoryItem(String id, String title, String subtitle, int folderType) {
        MediaMetadata metadata = new MediaMetadata.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setIsBrowsable(true)
                .setIsPlayable(false)
                .setFolderType(folderType)
                .build();
        return new MediaItem.Builder()
                .setMediaId(id)
                .setMediaMetadata(metadata)
                .build();
    }

    private MediaItem createPlayableItem(String id, String title, String artist, String album, String artworkUrl) {
        MediaMetadata.Builder meta = new MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setIsBrowsable(false)
                .setIsPlayable(true)
                .setFolderType(MediaMetadata.FOLDER_TYPE_NONE);

        if (artworkUrl != null && !artworkUrl.trim().isEmpty() && artworkUrl.startsWith("http")) {
            try {
                meta.setArtworkUri(android.net.Uri.parse(artworkUrl));
            } catch (Exception ignored) {}
        }

        return new MediaItem.Builder()
                .setMediaId(id)
                .setMediaMetadata(meta.build())
                .build();
    }

    private MediaItem findMediaItemById(String mediaId) {
        if (mediaId == null) return null;
        if ("ROOT".equals(mediaId)) {
            return new MediaItem.Builder()
                    .setMediaId("ROOT")
                    .setMediaMetadata(new MediaMetadata.Builder().setTitle("InstaMusic").setIsBrowsable(true).setIsPlayable(false).setFolderType(MediaMetadata.FOLDER_TYPE_MIXED).build())
                    .build();
        }
        List<MediaItem> all = new ArrayList<>();
        all.addAll(getMediaItemsForParent("ROOT"));
        all.addAll(getMediaItemsForParent("CAT_RECENT"));
        all.addAll(getMediaItemsForParent("CAT_TOP_HITS"));
        all.addAll(getMediaItemsForParent("CAT_MOOD"));
        all.addAll(getMediaItemsForParent("CAT_FAVORITES"));
        for (MediaItem item : all) {
            if (mediaId.equals(item.mediaId)) {
                return item;
            }
        }
        return null;
    }

    private void handleTrackSelectedFromCar(MediaItem target) {
        if (target == null || target.mediaMetadata == null) return;
        CharSequence titleSeq = target.mediaMetadata.title;
        CharSequence artistSeq = target.mediaMetadata.artist;
        final String title = titleSeq != null ? titleSeq.toString() : "";
        final String artist = artistSeq != null ? artistSeq.toString() : "";
        final String album = target.mediaMetadata.albumTitle != null ? target.mediaMetadata.albumTitle.toString() : "";
        final String artUrl = target.mediaMetadata.artworkUri != null ? target.mediaMetadata.artworkUri.toString() : "";

        if (!title.isEmpty()) {
            currentTitle = title;
            currentArtist = !artist.isEmpty() ? artist : "InstaMusic";
            currentAlbum = album;
            currentArtworkUrl = artUrl;
            isPlaying = true;
            isMusicCurrentlyPlaying = true;
            acquireWakeLock();
            registerNoisyReceiver();
            requestPlayerStateUpdate();

            MainActivity activity = MainActivity.getInstance();
            if (activity != null) {
                activity.playTrackDirectly(title, artist);
            } else {
                try {
                    Intent launchIntent = new Intent(this, MainActivity.class);
                    launchIntent.setAction(ACTION_PLAY_TRACK);
                    launchIntent.putExtra(EXTRA_TITLE, title);
                    launchIntent.putExtra(EXTRA_ARTIST, artist);
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(launchIntent);
                } catch (Exception ignored) {}
            }

            Intent playIntent = new Intent(ACTION_PLAY_TRACK);
            playIntent.putExtra(EXTRA_TITLE, title);
            playIntent.putExtra(EXTRA_ARTIST, artist);
            playIntent.setPackage(getPackageName());
            sendBroadcast(playIntent);
        }
    }

    private byte[] createFallbackArtworkBytes() {
        try {
            int size = 512;
            Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            // Deep studio obsidian background
            android.graphics.LinearGradient bgGrad = new android.graphics.LinearGradient(
                0f, 0f, (float) size, (float) size,
                new int[]{0xFF0B0E14, 0xFF121824, 0xFF1C1328},
                null, android.graphics.Shader.TileMode.CLAMP
            );
            paint.setShader(bgGrad);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(0, 0, size, size, paint);
            paint.setShader(null);

            float cx = size / 2f;
            float cy = size / 2f;

            // Outer vinyl concentric grooves
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(0x20FFFFFF);
            paint.setStrokeWidth(2f);
            canvas.drawCircle(cx, cy, 220f, paint);
            paint.setColor(0x16FFFFFF);
            canvas.drawCircle(cx, cy, 195f, paint);
            canvas.drawCircle(cx, cy, 170f, paint);
            canvas.drawCircle(cx, cy, 145f, paint);

            // Glowing Brand Ring (InstaMusic Orange to Magenta gradient)
            android.graphics.SweepGradient glowGrad = new android.graphics.SweepGradient(
                cx, cy,
                new int[]{0xFFFF5722, 0xFFFF8A00, 0xFFE11D48, 0xFF7C3AED, 0xFFFF5722},
                null
            );
            paint.setShader(glowGrad);
            paint.setStrokeWidth(22f);
            canvas.drawCircle(cx, cy, 115f, paint);
            paint.setShader(null);

            // Inner dark center label
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFF0F172A);
            canvas.drawCircle(cx, cy, 104f, paint);

            // Brand Coral Center Dot
            paint.setColor(0xFFFF5722);
            canvas.drawCircle(cx, cy, 36f, paint);

            // Center spindle
            paint.setColor(0xFFFFFFFF);
            canvas.drawCircle(cx, cy, 12f, paint);

            // Title banner text
            paint.setColor(0xEEFFFFFF);
            paint.setTextSize(28f);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setFakeBoldText(true);
            canvas.drawText("INSTA MUSIC", cx, size - 44f, paint);

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
                    String targetUrl = requestedUrl;
                    if (targetUrl.contains("150x150")) {
                        targetUrl = targetUrl.replace("150x150", "500x500");
                    } else if (targetUrl.contains("50x50")) {
                        targetUrl = targetUrl.replace("50x50", "500x500");
                    }
                    for (int redirectCount = 0; redirectCount < 5; redirectCount++) {
                        URL url = new URL(targetUrl);
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestProperty("User-Agent", "InstaMusic/1.0 (Android)");
                        conn.setInstanceFollowRedirects(true);
                        conn.setConnectTimeout(8000);
                        conn.setReadTimeout(8000);
                        conn.setDoInput(true);
                        int status = conn.getResponseCode();
                        if (status == HttpURLConnection.HTTP_MOVED_PERM
                                || status == HttpURLConnection.HTTP_MOVED_TEMP
                                || status == HttpURLConnection.HTTP_SEE_OTHER
                                || status == 307 || status == 308) {
                            String newLocation = conn.getHeaderField("Location");
                            if (newLocation != null && !newLocation.trim().isEmpty()) {
                                if (newLocation.startsWith("/")) {
                                    newLocation = url.getProtocol() + "://" + url.getHost() + newLocation;
                                }
                                targetUrl = newLocation;
                                conn.disconnect();
                                continue;
                            }
                        }
                        if (status >= 200 && status < 300) {
                            is = conn.getInputStream();
                            bmp = BitmapFactory.decodeStream(is);
                        }
                        break;
                    }
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
        mainHandler.removeCallbacks(releaseWakeLockRunnable);
        releaseWakeLockNow();
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
