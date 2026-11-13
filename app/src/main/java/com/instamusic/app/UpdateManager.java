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
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpdateManager {
    private static final String TAG = "UpdateManager";
    public static final String CHANNEL_ID = "instamusic_updates";
    public static final int NOTIFICATION_ID_UPDATE = 202;
    public static final int NOTIFICATION_ID_PROGRESS = 203;
    public static final String EXTRA_CHECK_UPDATE = "extra_check_update";

    private static final String VERSION_URL = "https://github.com/rapar5201-oss/InstaMusic-Android/releases/download/latest-apk/version.json";
    private static final String FALLBACK_RELEASE_API = "https://api.github.com/repos/rapar5201-oss/InstaMusic-Android/releases/tags/latest-apk";
    private static final String DEFAULT_APK_URL = "https://github.com/rapar5201-oss/InstaMusic-Android/releases/download/latest-apk/app-release.apk";

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static volatile boolean isDownloading = false;
    private static volatile boolean cancelRequested = false;

    private static File pendingInstallFile = null;
    private static String pendingFallbackUrl = null;

    public static void initNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null && nm.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "App Updates",
                    NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Notifies you when a new version of InstaMusic is available and tracks download progress");
                channel.enableLights(true);
                channel.enableVibration(true);
                nm.createNotificationChannel(channel);
            }
        }
    }

    public static void checkForUpdates(Activity activity, boolean userInitiated) {
        if (activity == null || activity.isFinishing()) return;
        initNotificationChannel(activity.getApplicationContext());

        if (userInitiated) {
            Toast.makeText(activity, "Checking for updates...", Toast.LENGTH_SHORT).show();
        }

        executor.execute(() -> {
            try {
                UpdateInfo info = fetchUpdateInfo();
                if (info == null) {
                    if (userInitiated) {
                        mainHandler.post(() -> Toast.makeText(activity, "Unable to check for updates right now. Please check your network.", Toast.LENGTH_SHORT).show());
                    }
                    return;
                }

                PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
                int currentCode = pInfo.versionCode;
                Log.d(TAG, "Current versionCode: " + currentCode + ", Remote versionCode: " + info.versionCode);

                if (info.versionCode > currentCode) {
                    // Post system notification for update available
                    showUpdateAvailableNotification(activity.getApplicationContext(), info);

                    // Show in-app update dialog
                    mainHandler.post(() -> {
                        if (!activity.isFinishing()) {
                            showUpdatePromptDialog(activity, info, pInfo.versionName);
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

    public static class UpdateInfo {
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

    public static void showUpdateAvailableNotification(Context context, UpdateInfo info) {
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
            PendingIntent pi = PendingIntent.getActivity(context, NOTIFICATION_ID_UPDATE, intent, flags);

            Notification.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder = new Notification.Builder(context, CHANNEL_ID);
            } else {
                builder = new Notification.Builder(context);
            }

            builder.setContentTitle("InstaMusic Update Available")
                   .setContentText("Version " + info.versionName + " is ready. Tap to download & install.")
                   .setSmallIcon(R.drawable.ic_music_note)
                   .setAutoCancel(true)
                   .setContentIntent(pi);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setPriority(Notification.PRIORITY_HIGH);
            }

            nm.notify(NOTIFICATION_ID_UPDATE, builder.build());
        } catch (Exception e) {
            Log.e(TAG, "Failed to show update notification", e);
        }
    }

    public static void showUpdatePromptDialog(Activity activity, UpdateInfo info, String currentVersion) {
        if (activity == null || activity.isFinishing()) return;

        new AlertDialog.Builder(activity)
            .setTitle("New Update Available")
            .setMessage("A new version of InstaMusic is available!\n\n"
                + "• Installed version: v" + currentVersion + "\n"
                + "• New version: v" + info.versionName + "\n\n"
                + "Release Notes:\n" + info.releaseNotes + "\n\n"
                + "Would you like to download and install this update now?")
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

    public static void downloadAndInstallApk(Activity activity, String apkUrl, String versionName) {
        if (activity == null || activity.isFinishing()) return;
        if (isDownloading) {
            Toast.makeText(activity, "Update download is already in progress...", Toast.LENGTH_SHORT).show();
            return;
        }

        isDownloading = true;
        cancelRequested = false;

        LayoutInflater inflater = LayoutInflater.from(activity);
        View dialogView = inflater.inflate(R.layout.dialog_update_progress, null);

        TextView titleView = dialogView.findViewById(R.id.update_dialog_title);
        TextView statusText = dialogView.findViewById(R.id.update_status_text);
        ProgressBar progressBar = dialogView.findViewById(R.id.update_progress_bar);
        TextView progressPercent = dialogView.findViewById(R.id.update_progress_percent);
        TextView progressBytes = dialogView.findViewById(R.id.update_progress_bytes);
        Button btnCancel = dialogView.findViewById(R.id.btn_update_cancel);
        Button btnAction = dialogView.findViewById(R.id.btn_update_action);

        titleView.setText("Downloading InstaMusic v" + versionName);
        statusText.setText("Connecting to download server...");
        progressBar.setIndeterminate(true);
        progressPercent.setText("0%");
        progressBytes.setText("Connecting...");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            progressBar.setProgressTintList(ColorStateList.valueOf(0xFFFF5100));
            progressBar.setIndeterminateTintList(ColorStateList.valueOf(0xFFFF5100));
        }

        AlertDialog dialog = new AlertDialog.Builder(activity)
            .setView(dialogView)
            .setCancelable(false)
            .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        btnCancel.setOnClickListener(v -> {
            if (isDownloading) {
                cancelRequested = true;
                statusText.setText("Cancelling download...");
                btnCancel.setEnabled(false);
            } else {
                dialog.dismiss();
            }
        });

        dialog.show();

        // Initial ongoing progress notification
        updateDownloadNotification(activity.getApplicationContext(), versionName, -1, "Connecting...", false, null);

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

                if (cancelRequested) {
                    handleDownloadCancelled(activity, dialog, null);
                    return;
                }

                if (status != HttpURLConnection.HTTP_OK) {
                    throw new Exception("Server response HTTP " + status);
                }

                long fileLength = conn.getContentLength();
                in = new BufferedInputStream(conn.getInputStream());

                File cacheDir = activity.getExternalCacheDir() != null ? activity.getExternalCacheDir() : activity.getCacheDir();
                File updateDir = new File(cacheDir, "updates");
                if (!updateDir.exists()) updateDir.mkdirs();
                apkFile = new File(updateDir, "InstaMusic-v" + versionName + ".apk");
                if (apkFile.exists()) apkFile.delete();

                out = new FileOutputStream(apkFile);
                byte[] buffer = new byte[8192];
                long totalDownloaded = 0;
                int readBytes;
                long lastUiUpdate = 0;

                while ((readBytes = in.read(buffer)) != -1) {
                    if (cancelRequested) {
                        handleDownloadCancelled(activity, dialog, apkFile);
                        return;
                    }

                    totalDownloaded += readBytes;
                    out.write(buffer, 0, readBytes);

                    long now = System.currentTimeMillis();
                    if (now - lastUiUpdate >= 120) {
                        lastUiUpdate = now;
                        final long curTotal = totalDownloaded;
                        final long targetTotal = fileLength;
                        final int progress = (targetTotal > 0) ? (int) ((curTotal * 100) / targetTotal) : -1;
                        final String mbString = formatBytes(curTotal) + (targetTotal > 0 ? " / " + formatBytes(targetTotal) : "");

                        mainHandler.post(() -> {
                            if (!activity.isFinishing() && dialog.isShowing()) {
                                if (progress >= 0) {
                                    progressBar.setIndeterminate(false);
                                    progressBar.setProgress(progress);
                                    statusText.setText("Downloading update: " + progress + "%");
                                    progressPercent.setText(progress + "%");
                                } else {
                                    progressBar.setIndeterminate(true);
                                    statusText.setText("Downloading update...");
                                    progressPercent.setText("");
                                }
                                progressBytes.setText(mbString);
                            }
                        });

                        updateDownloadNotification(activity.getApplicationContext(), versionName, progress, mbString, false, null);
                    }
                }

                out.flush();

                if (cancelRequested) {
                    handleDownloadCancelled(activity, dialog, apkFile);
                    return;
                }

                final File downloadedApk = apkFile;
                final long finalSize = downloadedApk != null ? downloadedApk.length() : 0;
                Log.d(TAG, "Update APK downloaded successfully, size: " + finalSize + " bytes");

                mainHandler.post(() -> {
                    isDownloading = false;
                    cancelRequested = false;

                    if (!activity.isFinishing() && dialog.isShowing()) {
                        progressBar.setIndeterminate(false);
                        progressBar.setProgress(100);
                        statusText.setText("Download complete! Ready to install.");
                        progressPercent.setText("100%");
                        progressBytes.setText(formatBytes(finalSize));

                        btnCancel.setText("Close");
                        btnCancel.setEnabled(true);
                        btnCancel.setOnClickListener(v -> dialog.dismiss());

                        btnAction.setVisibility(View.VISIBLE);
                        btnAction.setText("Install Now");
                        btnAction.setOnClickListener(v -> installApk(activity, downloadedApk, apkUrl));
                    }

                    // Complete notification with install action
                    updateDownloadNotification(activity.getApplicationContext(), versionName, 100, "Download complete. Tap to install.", true, downloadedApk);

                    // Automatically trigger installer
                    installApk(activity, downloadedApk, apkUrl);
                });

            } catch (Exception e) {
                Log.e(TAG, "Download error", e);
                isDownloading = false;
                final String errorMsg = (e.getMessage() != null && !e.getMessage().isEmpty()) ? e.getMessage() : "Unknown network error";

                mainHandler.post(() -> {
                    if (!activity.isFinishing() && dialog.isShowing()) {
                        progressBar.setIndeterminate(false);
                        progressBar.setProgress(0);
                        statusText.setText("Download failed: " + errorMsg);
                        progressBytes.setText("Failed");

                        btnCancel.setText("Close");
                        btnCancel.setEnabled(true);
                        btnCancel.setOnClickListener(v -> dialog.dismiss());

                        btnAction.setVisibility(View.VISIBLE);
                        btnAction.setText("Retry");
                        btnAction.setOnClickListener(v -> {
                            dialog.dismiss();
                            downloadAndInstallApk(activity, apkUrl, versionName);
                        });
                    }

                    Toast.makeText(activity, "Download failed: " + errorMsg, Toast.LENGTH_LONG).show();
                    cancelDownloadNotification(activity.getApplicationContext());
                });

            } finally {
                try { if (out != null) out.close(); } catch (Exception ignored) {}
                try { if (in != null) in.close(); } catch (Exception ignored) {}
                if (conn != null) conn.disconnect();
            }
        });
    }

    private static void handleDownloadCancelled(Activity activity, AlertDialog dialog, File partialFile) {
        isDownloading = false;
        cancelRequested = false;
        if (partialFile != null && partialFile.exists()) {
            try { partialFile.delete(); } catch (Exception ignored) {}
        }

        cancelDownloadNotification(activity.getApplicationContext());

        mainHandler.post(() -> {
            try {
                if (!activity.isFinishing() && dialog != null && dialog.isShowing()) {
                    TextView statusText = dialog.findViewById(R.id.update_status_text);
                    if (statusText != null) statusText.setText("Download cancelled.");
                    mainHandler.postDelayed(() -> {
                        try { if (dialog.isShowing()) dialog.dismiss(); } catch (Exception ignored) {}
                    }, 800);
                }
            } catch (Exception ignored) {}
            Toast.makeText(activity, "Update download cancelled", Toast.LENGTH_SHORT).show();
        });
    }

    private static String formatBytes(long bytes) {
        if (bytes <= 0) return "0.0 MB";
        double mb = bytes / (1024.0 * 1024.0);
        return String.format(Locale.US, "%.1f MB", mb);
    }

    public static void updateDownloadNotification(Context context, String versionName, int progress, String details, boolean isComplete, File downloadedFile) {
        try {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm == null) return;

            Notification.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder = new Notification.Builder(context, CHANNEL_ID);
            } else {
                builder = new Notification.Builder(context);
            }

            builder.setSmallIcon(R.drawable.ic_music_note);

            if (isComplete && downloadedFile != null && downloadedFile.exists()) {
                Intent installIntent = createInstallIntent(context, downloadedFile);
                int flags = PendingIntent.FLAG_UPDATE_CURRENT;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    flags |= PendingIntent.FLAG_IMMUTABLE;
                }
                PendingIntent pi = PendingIntent.getActivity(context, NOTIFICATION_ID_PROGRESS, installIntent, flags);

                builder.setContentTitle("InstaMusic Update Ready")
                       .setContentText("Version " + versionName + " downloaded. Tap to install.")
                       .setProgress(0, 0, false)
                       .setOngoing(false)
                       .setAutoCancel(true)
                       .setContentIntent(pi);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder.setPriority(Notification.PRIORITY_HIGH);
                }
            } else {
                builder.setContentTitle("Downloading InstaMusic v" + versionName)
                       .setContentText(details)
                       .setOngoing(true);

                if (progress >= 0) {
                    builder.setProgress(100, progress, false);
                } else {
                    builder.setProgress(100, 0, true);
                }
            }

            nm.notify(NOTIFICATION_ID_PROGRESS, builder.build());
        } catch (Exception e) {
            Log.e(TAG, "Failed to update download notification", e);
        }
    }

    public static void cancelDownloadNotification(Context context) {
        try {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.cancel(NOTIFICATION_ID_PROGRESS);
            }
        } catch (Exception ignored) {}
    }

    public static Intent createInstallIntent(Context context, File apkFile) {
        Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", apkFile);
        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return installIntent;
    }

    public static void installApk(Activity activity, File apkFile, String fallbackUrl) {
        if (activity == null || activity.isFinishing()) return;
        if (apkFile == null || !apkFile.exists() || apkFile.length() == 0) {
            Toast.makeText(activity, "Update package not found. Opening browser...", Toast.LENGTH_SHORT).show();
            try {
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)));
            } catch (Exception ignored) {}
            return;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!activity.getPackageManager().canRequestPackageInstalls()) {
                    pendingInstallFile = apkFile;
                    pendingFallbackUrl = fallbackUrl;

                    new AlertDialog.Builder(activity)
                        .setTitle("Install Permission Required")
                        .setMessage("Android requires permission to install app updates for InstaMusic.\n\nPlease tap 'Settings', turn on 'Allow from this source', then return here to complete installation.")
                        .setPositiveButton("Settings", (dialog, which) -> {
                            try {
                                Intent permIntent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                                permIntent.setData(Uri.parse("package:" + activity.getPackageName()));
                                activity.startActivity(permIntent);
                            } catch (Exception ex) {
                                Toast.makeText(activity, "Error opening settings: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            pendingInstallFile = null;
                            dialog.dismiss();
                        })
                        .show();
                    return;
                }
            }

            Intent installIntent = createInstallIntent(activity, apkFile);
            activity.startActivity(installIntent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch installer", e);
            Toast.makeText(activity, "Installer error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            try {
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)));
            } catch (Exception ignored) {}
        }
    }

    public static void checkPendingInstallOnResume(Activity activity) {
        if (activity == null || activity.isFinishing()) return;
        if (pendingInstallFile != null && pendingInstallFile.exists()) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || activity.getPackageManager().canRequestPackageInstalls()) {
                File fileToInstall = pendingInstallFile;
                String fallback = pendingFallbackUrl;
                pendingInstallFile = null;
                pendingFallbackUrl = null;
                installApk(activity, fileToInstall, fallback);
            }
        }
    }
}
