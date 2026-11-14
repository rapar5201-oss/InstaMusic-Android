package com.instamusic.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MusicHistoryDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "MusicHistoryDbHelper";
    private static final String DATABASE_NAME = "instamusic_history.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_TRACKS = "track_history";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_ARTIST = "artist";
    public static final String COLUMN_ALBUM = "album";
    public static final String COLUMN_ARTWORK_URL = "artwork_url";
    public static final String COLUMN_LAST_PLAYED = "last_played_time";
    public static final String COLUMN_PLAY_COUNT = "play_count";

    private static MusicHistoryDbHelper instance;

    public static synchronized MusicHistoryDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new MusicHistoryDbHelper(context.getApplicationContext());
        }
        return instance;
    }

    public static class TrackInfo {
        public long id;
        public String title;
        public String artist;
        public String album;
        public String artworkUrl;
        public long lastPlayedTime;
        public int playCount;

        public TrackInfo(long id, String title, String artist, String album, String artworkUrl, long lastPlayedTime, int playCount) {
            this.id = id;
            this.title = title;
            this.artist = artist != null ? artist : "InstaMusic";
            this.album = album != null ? album : "";
            this.artworkUrl = artworkUrl != null ? artworkUrl : "";
            this.lastPlayedTime = lastPlayedTime;
            this.playCount = playCount;
        }
    }

    private MusicHistoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_TRACKS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT NOT NULL, " +
                COLUMN_ARTIST + " TEXT, " +
                COLUMN_ALBUM + " TEXT, " +
                COLUMN_ARTWORK_URL + " TEXT, " +
                COLUMN_LAST_PLAYED + " INTEGER NOT NULL, " +
                COLUMN_PLAY_COUNT + " INTEGER NOT NULL DEFAULT 1" +
                ");";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACKS);
        onCreate(db);
    }

    public synchronized void recordTrackPlayed(String title, String artist, String album, String artworkUrl) {
        if (title == null || title.trim().isEmpty() || title.equalsIgnoreCase("INSTA MUSIC")) {
            return;
        }
        String cleanTitle = title.trim();
        String cleanArtist = (artist != null && !artist.trim().isEmpty()) ? artist.trim() : "InstaMusic";
        long now = System.currentTimeMillis();

        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getWritableDatabase();
            cursor = db.query(
                    TABLE_TRACKS,
                    new String[]{COLUMN_ID, COLUMN_PLAY_COUNT},
                    COLUMN_TITLE + " = ? AND " + COLUMN_ARTIST + " = ?",
                    new String[]{cleanTitle, cleanArtist},
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                int currentCount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PLAY_COUNT));

                ContentValues values = new ContentValues();
                values.put(COLUMN_LAST_PLAYED, now);
                values.put(COLUMN_PLAY_COUNT, currentCount + 1);
                if (artworkUrl != null && !artworkUrl.trim().isEmpty()) {
                    values.put(COLUMN_ARTWORK_URL, artworkUrl.trim());
                }
                if (album != null && !album.trim().isEmpty()) {
                    values.put(COLUMN_ALBUM, album.trim());
                }

                db.update(TABLE_TRACKS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
            } else {
                ContentValues values = new ContentValues();
                values.put(COLUMN_TITLE, cleanTitle);
                values.put(COLUMN_ARTIST, cleanArtist);
                values.put(COLUMN_ALBUM, album != null ? album.trim() : "");
                values.put(COLUMN_ARTWORK_URL, artworkUrl != null ? artworkUrl.trim() : "");
                values.put(COLUMN_LAST_PLAYED, now);
                values.put(COLUMN_PLAY_COUNT, 1);

                db.insert(TABLE_TRACKS, null, values);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error recording track played: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public synchronized TrackInfo getLastPlayedTrack() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            cursor = db.query(
                    TABLE_TRACKS,
                    null,
                    null,
                    null,
                    null, null,
                    COLUMN_LAST_PLAYED + " DESC",
                    "1"
            );
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToTrack(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching last played track: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public synchronized TrackInfo getFavoriteOrRecentTrack() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            // Fetch top 5 recent or most played tracks to pick an enticing reminder
            cursor = db.query(
                    TABLE_TRACKS,
                    null,
                    null,
                    null,
                    null, null,
                    COLUMN_PLAY_COUNT + " DESC, " + COLUMN_LAST_PLAYED + " DESC",
                    "5"
            );
            if (cursor != null && cursor.getCount() > 0) {
                int count = cursor.getCount();
                int randomIndex = (int) (Math.random() * count);
                cursor.moveToPosition(randomIndex);
                return cursorToTrack(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching favorite track: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public synchronized int getTrackCount() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TRACKS, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            return 0;
        } finally {
            if (cursor != null) cursor.close();
        }
        return 0;
    }

    private TrackInfo cursorToTrack(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
        String artist = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ARTIST));
        String album = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ALBUM));
        String artworkUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ARTWORK_URL));
        long lastPlayed = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LAST_PLAYED));
        int playCount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PLAY_COUNT));
        return new TrackInfo(id, title, artist, album, artworkUrl, lastPlayed, playCount);
    }
}
