package com.instamusic.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpdateManager {
    private static final String TAG = "UpdateManager";
    public static final String CHANNEL_ID = "instamusic_updates";
    public static final int NOTIFICATION_ID = 202;
    public static final String EXTRA_CHECK_UPDATE = "extra_check_update";

    private static final String VERSION_URL = "https://github.com/rapar5201-oss/InstaMusic-Android/releases/download/latest-apk/version.json";
    private static final String FALLBACK_RELEASE_API = "https://api.github.com/repos/rapar5201-oss/InstaMusic-Android/releases/tags/latest-apk";
    private static final String DEFAULT_APK_URL = "https://github.com/rapar5201-oss/InstaMusic-Android/releases/download/latest-apk/app-release.apk";

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static boolean isDownloading = false;

    public static void initNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null && nm.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "App Updates",
                    NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Notifies you when a new version of InstaMusic is available");
                channel.enableLights(true);
                channel.enableVibration(true);
                nm.createNotificationChannel(channel);
            }
        }
    }

    public static void checkForUpdates(Activity activity, boolean userInitiated) {
        if (activity == null || activity.isFinishing()) return;
        initNotificationChannel(activity.getApplicationContext());

        executor.execute(() -> {
            try {
                UpdateInfo info = fetchUpdateInfo();
                if (info == null) {
                    if (userInitiated) {
                        mainHandler.post(() -> Toast.makeText(activity, "Unable to check for updates right now", Toast.LENGTH_SHORT).show());
                    }
                    return;
                }

                PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
                int currentCode = pInfo.versionCode;
                Log.d(TAG, "Current versionCode: " + currentCode + ", Remote versionCode: " + info.versionCode);

                if (info.versionCode > currentCode) {
                    // Post system notification
                    showUpdateNotification(activity.getApplicationContext(), info);

                    // Show in-app update dialog
                    mainHandler.post(() -> {
                        if (!activity.isFinishing()) {
                            showUpdateDialog(activity, info);
                        }
                    });
                } else if (userInitiated) {
                    mainHandler.post(() -> Toast.makeText(activity, "InstaMusic is up to date (v" + pInfo.versionName + ")", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error checking for updates", e);
                if (userInitiated) {
                    mainHandler.post(() -> Toast.makeText(activity, "Update check failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private static class UpdateInfo {
        int versionCode;
        String versionName;
        String apkUrl;
        String releaseNotes;
    }

    private static UpdateInfo fetchUpdateInfo() {
        // Step 1: Try reading version.json
        try {
            String jsonStr = httpGet(VERSION_URL);
            if (jsonStr != null && !jsonStr.trim().isEmpty()) {
                JSONObject obj = new JSONObject(jsonStr);
                UpdateInfo info = new UpdateInfo();
                info.versionCode = obj.optInt("versionCode", 0);
                info.versionName = obj.optString("versionName", "1.0.0");
                info.apkUrl = obj.optString("apkUrl", DEFAULT_APK_URL);
                info.releaseNotes = obj.optString("releaseNotes", "New update available with bug fixes and features.");
                if (info.versionCode > 0) return info;
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to load version.json, trying GitHub releases API", e);
        }

        // Step 2: Fallback to GitHub Releases API
        try {
            String apiStr = httpGet(FALLBACK_RELEASE_API);
            if (apiStr != null && !apiStr.trim().isEmpty()) {
                JSONObject release = new JSONObject(apiStr);
                UpdateInfo info = new UpdateInfo();
                info.releaseNotes = release.optString("body", "New version of InstaMusic is available.");
                info.apkUrl = DEFAULT_APK_URL;

                if (release.has("assets")) {
                    org.json.JSONArray assets = release.getJSONArray("assets");
                    for (int i = 0; i < assets.length(); i++) {
                        JSONObject asset = assets.getJSONObject(i);
                        String name = asset.optString("name", "");
                        if (name.contains("release") || name.endsWith(".apk")) {
                            info.apkUrl = asset.optString("browser_download_url", DEFAULT_APK_URL);
                            break;
                        }
                    }
                }

                String tagName = release.optString("tag_name", "");
                String relName = release.optString("name", "");
                info.versionName = !relName.isEmpty() ? relName : (!tagName.isEmpty() ? tagName : "Latest");
                info.versionCode = 999;
                return info;
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed fallback to GitHub releases API", e);
        }

        return null;
    }

    private static String httpGet(String urlStr) throws Exception {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            conn.setRequestProperty("User-Agent", "InstaMusic-Android");
            conn.setInstanceFollowRedirects(true);

            int status = conn.getResponseCode();
            int redirects = 0;
            while ((status == HttpURLConnection.HTTP_MOVED_TEMP
                    || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status == HttpURLConnection.HTTP_SEE_OTHER
                    || status == 307 || status == 308) && redirects < 5) {
                String loc = conn.getHeaderField("Location");
                if (loc == null) break;
                conn.disconnect();
                url = new URL(loc);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                conn.setRequestProperty("User-Agent", "InstaMusic-Android");
                status = conn.getResponseCode();
                redirects++;
            }

            if (status != HttpURLConnection.HTTP_OK) return null;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                return sb.toString();
            }
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    public static void showUpdateNotification(Context context, UpdateInfo info) {
        try {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm == null) return;

            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra(EXTRA_CHECK_UPDATE, true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }
            PendingIntent pi = PendingIntent.getActivity(context, 202, intent, flags);

            Notification.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder = new Notification.Builder(context, CHANNEL_ID);
            } else {
                builder = new Notification.Builder(context);
            }

            builder.setContentTitle("InstaMusic Update Available")
                   .setContentText("Version " + info.versionName + " is ready to install. Tap to update.")
                   .setSmallIcon(R.drawable.ic_music_note)
                   .setAutoCancel(true)
                   .setContentIntent(pi);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setPriority(Notification.PRIORITY_HIGH);
            }

            nm.notify(NOTIFICATION_ID, builder.build());
        } catch (Exception e) {
            Log.e(TAG, "Failed to show update notification", e);
        }
    }

    public static void showUpdateDialog(Activity activity, UpdateInfo info) {
        if (activity == null || activity.isFinishing()) return;

        new AlertDialog.Builder(activity)
            .setTitle("New Update Available")
            .setMessage("A new version of InstaMusic (" + info.versionName + ") is available!\n\n"
                + info.releaseNotes + "\n\nWould you like to download and install it now?")
            .setPositiveButton("Update Now", (dialog, which) -> {
                dialog.dismiss();
                downloadAndInstallApk(activity, info.apkUrl, info.versionName);
            })
            .setNeutralButton("Open in Browser", (dialog, which) -> {
                dialog.dismiss();
                try {
                    Intent bIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(info.apkUrl));
                    activity.startActivity(bIntent);
                } catch (Exception ignored) {}
            })
            .setNegativeButton("Later", (dialog, which) -> dialog.dismiss())
            .show();
    }

    private static void downloadAndInstallApk(Activity activity, String apkUrl, String versionName) {
        if (isDownloading) {
            Toast.makeText(activity, "Update is already downloading...", Toast.LENGTH_SHORT).show();
            return;
        }
        isDownloading = true;

        AlertDialog progressDialog = new AlertDialog.Builder(activity)
            .setTitle("Downloading InstaMusic v" + versionName)
            .setMessage("Connecting to download server...")
            .setCancelable(false)
            .create();
        progressDialog.show();

        executor.execute(() -> {
            HttpURLConnection conn = null;
            InputStream in = null;
            FileOutputStream out = null;
            File apkFile = null;
            try {
                URL url = new URL(apkUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);
                conn.setRequestProperty("User-Agent", "InstaMusic-Android");
                conn.setInstanceFollowRedirects(true);

                int status = conn.getResponseCode();
                int redirects = 0;
                while ((status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_SEE_OTHER
                        || status == 307 || status == 308) && redirects < 5) {
                    String loc = conn.getHeaderField("Location");
                    if (loc == null) break;
                    conn.disconnect();
                    url = new URL(loc);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(30000);
                    conn.setRequestProperty("User-Agent", "InstaMusic-Android");
                    status = conn.getResponseCode();
                    redirects++;
                }

                if (status != HttpURLConnection.HTTP_OK) {
                    throw new Exception("HTTP server returned " + status);
                }

                long fileLength = conn.getContentLength();
                in = new BufferedInputStream(conn.getInputStream());

                File cacheDir = activity.getExternalCacheDir() != null ? activity.getExternalCacheDir() : activity.getCacheDir();
                File updateDir = new File(cacheDir, "updates");
                if (!updateDir.exists()) updateDir.mkdirs();
                apkFile = new File(updateDir, "InstaMusic-update.apk");
                if (apkFile.exists()) apkFile.delete();

                out = new FileOutputStream(apkFile);
                byte[] data = new byte[8192];
                long total = 0;
                int count;
                long lastUpdate = 0;

                while ((count = in.read(data)) != -1) {
                    total += count;
                    out.write(data, 0, count);

                    long now = System.currentTimeMillis();
                    if (now - lastUpdate > 300) {
                        lastUpdate = now;
                        final int progress = (fileLength > 0) ? (int) ((total * 100) / fileLength) : -1;
                        final long mbDownloaded = total / (1024 * 1024);
                        mainHandler.post(() -> {
                            if (!activity.isFinishing() && progressDialog.isShowing()) {
                                if (progress >= 0) {
                                    progressDialog.setMessage("Downloaded " + progress + "% (" + mbDownloaded + " MB)");
                                } else {
                                    progressDialog.setMessage("Downloaded " + mbDownloaded + " MB...");
                                }
                            }
                        });
                    }
                }
                out.flush();

                final File downloadedFile = apkFile;
                mainHandler.post(() -> {
                    isDownloading = false;
                    try { if (progressDialog.isShowing()) progressDialog.dismiss(); } catch (Exception ignored) {}
                    installApk(activity, downloadedFile, apkUrl);
                });

            } catch (Exception e) {
                Log.e(TAG, "Download failed", e);
                isDownloading = false;
                mainHandler.post(() -> {
                    try { if (progressDialog.isShowing()) progressDialog.dismiss(); } catch (Exception ignored) {}
                    Toast.makeText(activity, "Download error: " + e.getMessage() + ". Opening in browser...", Toast.LENGTH_LONG).show();
                    try {
                        Intent bIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl));
                        activity.startActivity(bIntent);
                    } catch (Exception ignored) {}
                });
            } finally {
                try { if (out != null) out.close(); } catch (Exception ignored) {}
                try { if (in != null) in.close(); } catch (Exception ignored) {}
                if (conn != null) conn.disconnect();
            }
        });
    }

    public static void installApk(Activity activity, File apkFile, String fallbackUrl) {
        if (activity == null || activity.isFinishing()) return;
        if (apkFile == null || !apkFile.exists() || apkFile.length() == 0) {
            Toast.makeText(activity, "Downloaded file not found. Opening browser...", Toast.LENGTH_SHORT).show();
            try {
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)));
            } catch (Exception ignored) {}
            return;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!activity.getPackageManager().canRequestPackageInstalls()) {
                    Toast.makeText(activity, "Please allow InstaMusic to install update packages", Toast.LENGTH_LONG).show();
                    Intent permIntent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                    permIntent.setData(Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivity(permIntent);
                }
            }

            Uri apkUri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", apkFile);

            Intent installIntent = new Intent(Intent.ACTION_VIEW);
            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(installIntent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch installer", e);
            Toast.makeText(activity, "Installer error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            try {
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)));
            } catch (Exception ignored) {}
        }
    }
}
