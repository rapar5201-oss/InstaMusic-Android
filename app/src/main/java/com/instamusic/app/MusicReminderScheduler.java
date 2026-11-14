package com.instamusic.app;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MusicReminderScheduler {
    private static final String TAG = "MusicReminderScheduler";

    public static final String CHANNEL_ID = "instamusic_reminders";
    public static final String ACTION_CHECK_REMINDER = "com.instamusic.app.ACTION_CHECK_REMINDER";
    public static final String PREFS_NAME = "instamusic_reminder_prefs";
    public static final String KEY_LAST_REMINDER_TIME = "last_reminder_time";
    public static final int NOTIFICATION_ID = 202;

    // Interval: 30 minutes
    public static final long REMINDER_INTERVAL_MS = 30 * 60 * 1000L;
    // Minimum cooldown between notifications: 20 minutes
    public static final long MIN_COOLDOWN_MS = 15 * 60 * 1000L;

    private static final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Reminders & Recommendations",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Reminders when you have been away from your favorite music");
            channel.setShowBadge(true);
            channel.enableVibration(true);
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }

    public static void schedulePeriodicReminders(Context context) {
        if (context == null) return;
        createNotificationChannel(context);

        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            Intent intent = new Intent(context, MusicReminderReceiver.class);
            intent.setAction(ACTION_CHECK_REMINDER);

            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1001, intent, flags);

            long triggerAtMillis = System.currentTimeMillis() + REMINDER_INTERVAL_MS;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } else {
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, REMINDER_INTERVAL_MS, pendingIntent);
            }

            Log.d(TAG, "Scheduled next reminder check in " + (REMINDER_INTERVAL_MS / 60000) + " minutes");
        } catch (Exception e) {
            Log.e(TAG, "Failed to schedule reminders: " + e.getMessage(), e);
        }
    }

    public static void triggerReminder(final Context context) {
        if (context == null) return;

        // 1. Re-arm next alarm check to ensure continuous scheduling
        schedulePeriodicReminders(context);

        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // 2. Check if music is currently playing in foreground service
                    if (MusicPlaybackService.isMusicCurrentlyPlaying) {
                        Log.d(TAG, "Music is currently playing; skipping reminder.");
                        return;
                    }

                    // 3. Check if app is in active foreground
                    if (MainActivity.isAppInForeground) {
                        Log.d(TAG, "App is currently in foreground; skipping reminder.");
                        return;
                    }

                    // 4. Check notification permission
                    if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                        Log.d(TAG, "Notifications are disabled by user; skipping.");
                        return;
                    }

                    // 5. Check cooldown
                    SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    long lastTime = prefs.getLong(KEY_LAST_REMINDER_TIME, 0L);
                    long now = System.currentTimeMillis();
                    if (now - lastTime < MIN_COOLDOWN_MS) {
                        Log.d(TAG, "Cooldown active; skipping reminder.");
                        return;
                    }

                    // 6. Fetch favorite or recent track from database
                    MusicHistoryDbHelper db = MusicHistoryDbHelper.getInstance(context);
                    MusicHistoryDbHelper.TrackInfo track = db.getFavoriteOrRecentTrack();

                    String titleText;
                    String bodyText;

                    int variant = (int) (Math.random() * 5);
                    if (track != null && track.title != null && !track.title.isEmpty()) {
                        if (variant == 0) {
                            titleText = "Your song is missing you 🎵";
                            bodyText = "Pick up where you left off with \"" + track.title + "\" by " + track.artist + ". Tap to play now.";
                        } else if (variant == 1) {
                            titleText = "Your waiting tracks are ready 🎶";
                            bodyText = "Your queue and saved library have songs waiting for you. Tap to resume listening.";
                        } else if (variant == 2) {
                            titleText = "New tracks waiting on InstaMusic 🎧";
                            bodyText = "Fresh mixes and trending charts are live. Tap to discover what's new today.";
                        } else if (variant == 3) {
                            titleText = "Time for your favorite vibe? ✨";
                            bodyText = "Ready for another play of \"" + track.title + "\" by " + track.artist + "? Tap to turn it up.";
                        } else {
                            titleText = "Take a music break ⚡";
                            bodyText = "Tune back into \"" + track.title + "\" or jump into your daily mixes.";
                        }
                    } else {
                        if (variant % 2 == 0) {
                            titleText = "Your waiting tracks are ready 🎶";
                            bodyText = "Your queue and saved library have songs waiting for you. Tap to start listening.";
                        } else {
                            titleText = "New tracks waiting on InstaMusic 🎧";
                            bodyText = "Fresh beats and trending playlists are live. Tap to hear what's playing today.";
                        }
                    }

                    // 7. Load artwork for notification large icon
                    Bitmap largeIcon = null;
                    if (track != null && track.artworkUrl != null && !track.artworkUrl.trim().isEmpty()) {
                        largeIcon = downloadArtwork(track.artworkUrl);
                    }
                    if (largeIcon == null) {
                        largeIcon = createFallbackIcon();
                    }

                    // 8. Intent to open app directly
                    Intent openIntent = new Intent(context, MainActivity.class);
                    openIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    openIntent.putExtra("from_reminder", true);
                    if (track != null && track.title != null) {
                        openIntent.putExtra("resume_title", track.title);
                        openIntent.putExtra("resume_artist", track.artist);
                    }

                    int flags = PendingIntent.FLAG_UPDATE_CURRENT;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        flags |= PendingIntent.FLAG_IMMUTABLE;
                    }
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 2001, openIntent, flags);

                    // 9. Build notification
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_music_note)
                            .setColor(0xFFFF5722)
                            .setContentTitle(titleText)
                            .setContentText(bodyText)
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(bodyText)
                                    .setSummaryText("InstaMusic Reminder"))
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setAutoCancel(true)
                            .setContentIntent(pendingIntent);

                    if (largeIcon != null) {
                        builder.setLargeIcon(largeIcon);
                    }

                    NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (nm != null) {
                        createNotificationChannel(context);
                        nm.notify(NOTIFICATION_ID, builder.build());
                        prefs.edit().putLong(KEY_LAST_REMINDER_TIME, now).apply();
                        Log.d(TAG, "Successfully posted reminder notification: " + titleText);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error triggering reminder: " + e.getMessage(), e);
                }
            }
        });
    }

    private static Bitmap downloadArtwork(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(5000);
            conn.setDoInput(true);
            conn.connect();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream is = conn.getInputStream();
                Bitmap bmp = BitmapFactory.decodeStream(is);
                is.close();
                return bmp;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static Bitmap createFallbackIcon() {
        try {
            Bitmap bmp = Bitmap.createBitmap(192, 192, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            // Dark obsidian circle
            paint.setColor(0xFF14171E);
            canvas.drawCircle(96, 96, 96, paint);

            // Subtle vibrant orange ring
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(7f);
            paint.setColor(0xFFFF5722);
            canvas.drawCircle(96, 96, 78, paint);

            // Center glow dot
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFFFF5722);
            canvas.drawCircle(96, 96, 24, paint);

            return bmp;
        } catch (Exception e) {
            return null;
        }
    }
}
