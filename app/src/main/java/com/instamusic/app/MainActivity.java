package com.instamusic.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
    public static volatile boolean isAppInForeground = false;
    private static final String APP_URL = "https://instamusic.lovable.app";
    private static final long BACK_EXIT_MS = 2000;
    private static final int PERMISSION_REQ_NOTIFICATIONS = 1001;

    private static java.lang.ref.WeakReference<MainActivity> sInstanceRef;

    public static MainActivity getInstance() {
        return sInstanceRef != null ? sInstanceRef.get() : null;
    }

    private WebView appView, splashView;
    private LinearLayout errorLayout;
    private boolean pageReady = false, splashDone = false, splashHidden = false;
    private long lastBackPressed = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private String pendingPlayTitle = null;
    private String pendingPlayArtist = null;

    public void playTrackDirectly(String title, String artist) {
        triggerPlayTrack(title, artist);
    }

    public class SplashBridge {
        @JavascriptInterface
        public void onSplashComplete() {
            handler.post(() -> { splashDone = true; revealWhenReady(); });
        }

        @JavascriptInterface
        public void checkForUpdates() {
            handler.post(() -> UpdateManager.checkForUpdates(MainActivity.this, true));
        }
    }

    public class MediaBridge {
        @JavascriptInterface
        public void updateMetadata(String title, String artist, String album, String artworkUrl, boolean isPlaying) {
            updateMetadataWithUpcoming(title, artist, album, artworkUrl, isPlaying, null, null);
        }

        @JavascriptInterface
        public void updateMetadataWithUpcoming(String title, String artist, String album, String artworkUrl, boolean isPlaying, String upcomingTitle, String upcomingArtist) {
            handler.post(() -> {
                MusicPlaybackService.isMusicCurrentlyPlaying = isPlaying;
                checkAndRequestNotificationPermission();
                Intent intent = new Intent(MainActivity.this, MusicPlaybackService.class);
                intent.setAction(MusicPlaybackService.ACTION_UPDATE_METADATA);
                intent.putExtra(MusicPlaybackService.EXTRA_TITLE, title);
                intent.putExtra(MusicPlaybackService.EXTRA_ARTIST, artist);
                intent.putExtra(MusicPlaybackService.EXTRA_ALBUM, album);
                intent.putExtra(MusicPlaybackService.EXTRA_ARTWORK_URL, artworkUrl);
                intent.putExtra(MusicPlaybackService.EXTRA_IS_PLAYING, isPlaying);
                if (upcomingTitle != null && !upcomingTitle.trim().isEmpty()) {
                    intent.putExtra(MusicPlaybackService.EXTRA_UPCOMING_TITLE, upcomingTitle);
                }
                if (upcomingArtist != null && !upcomingArtist.trim().isEmpty()) {
                    intent.putExtra(MusicPlaybackService.EXTRA_UPCOMING_ARTIST, upcomingArtist);
                }
                startPlaybackService(intent);
                if (isPlaying && title != null && !title.trim().isEmpty() && !title.equalsIgnoreCase("INSTA MUSIC")) {
                    MusicHistoryDbHelper.getInstance(MainActivity.this).recordTrackPlayed(title, artist, album, artworkUrl);
                }
            });
        }

        @JavascriptInterface
        public void updatePlaybackState(boolean isPlaying) {
            updatePlaybackInfo(0, 0, isPlaying);
        }

        @JavascriptInterface
        public void updatePlaybackInfo(double positionSeconds, double durationSeconds, boolean isPlaying) {
            handler.post(() -> {
                MusicPlaybackService.isMusicCurrentlyPlaying = isPlaying;
                checkAndRequestNotificationPermission();
                Intent intent = new Intent(MainActivity.this, MusicPlaybackService.class);
                intent.setAction(MusicPlaybackService.ACTION_UPDATE_POSITION);
                intent.putExtra(MusicPlaybackService.EXTRA_POSITION_MS, Math.max(0L, Math.round(positionSeconds * 1000.0)));
                intent.putExtra(MusicPlaybackService.EXTRA_DURATION_MS, Math.max(0L, Math.round(durationSeconds * 1000.0)));
                intent.putExtra(MusicPlaybackService.EXTRA_IS_PLAYING, isPlaying);
                startPlaybackService(intent);
            });
        }

        @JavascriptInterface
        public void checkForUpdates() {
            handler.post(() -> UpdateManager.checkForUpdates(MainActivity.this, true));
        }

        @JavascriptInterface
        public void setRemindersEnabled(boolean enabled) {
            try {
                SharedPreferences prefs = getSharedPreferences(MusicReminderScheduler.PREFS_NAME, Context.MODE_PRIVATE);
                prefs.edit().putBoolean("reminders_enabled", enabled).apply();
                if (!enabled) {
                    MusicReminderScheduler.cancelReminders(MainActivity.this);
                } else {
                    MusicReminderScheduler.schedulePeriodicReminders(MainActivity.this);
                }
            } catch (Exception e) {
                android.util.Log.e("MainActivity", "Error in setRemindersEnabled: " + e.getMessage());
            }
        }

                @JavascriptInterface
        public void setReminderInterval(String intervalStr) {
            handler.post(() -> {
                try {
                    long minutes = 30;
                    if (intervalStr != null) {
                        String s = intervalStr.toLowerCase();
                        if (s.contains("10")) minutes = 10;
                        else if (s.contains("30")) minutes = 30;
                        else if (s.contains("1 hour") || s.contains("60")) minutes = 60;
                        else if (s.contains("off")) {
                            setRemindersEnabled(false);
                            return;
                        }
                    }
                    MusicReminderScheduler.setReminderIntervalMinutes(MainActivity.this, minutes);
                } catch (Exception ignored) {}
            });
        }
        @JavascriptInterface
        public boolean getRemindersEnabled() {
            try {
                SharedPreferences prefs = getSharedPreferences(MusicReminderScheduler.PREFS_NAME, Context.MODE_PRIVATE);
                return prefs.getBoolean("reminders_enabled", true);
            } catch (Exception e) {
                return true;
            }
        }

        @JavascriptInterface
        public void triggerTestReminder() {
            handler.post(() -> MusicReminderScheduler.triggerReminder(MainActivity.this));
        }

        @JavascriptInterface
        public void share(String title, String text, String url) {
            handler.post(() -> {
                try {
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.setType("text/plain");
                    StringBuilder sb = new StringBuilder();
                    if (title != null && !title.trim().isEmpty()) {
                        sb.append(title.trim());
                    }
                    if (text != null && !text.trim().isEmpty() && !text.trim().equals(title != null ? title.trim() : "")) {
                        if (sb.length() > 0) sb.append("
");
                        sb.append(text.trim());
                    }
                    if (url != null && !url.trim().isEmpty() && !sb.toString().contains(url.trim())) {
                        if (sb.length() > 0) sb.append("
");
                        sb.append(url.trim());
                    }
                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, title != null && !title.isEmpty() ? title : "InstaMusic");
                    sendIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
                    Intent chooser = Intent.createChooser(sendIntent, "Share music via");
                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(chooser);
                } catch (Exception e) {
                    android.util.Log.e("MainActivity", "Error in share: " + e.getMessage());
                }
            });
        }

        @JavascriptInterface
        public void vibrate(long milliseconds) {
            handler.post(() -> {
                try {
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (v != null && v.hasVibrator()) {
                        long duration = milliseconds > 0 ? milliseconds : 25;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            v.vibrate(duration);
                        }
                    }
                } catch (Exception e) {
                    android.util.Log.w("MainActivity", "Error in vibrate: " + e.getMessage());
                }
            });
        }

        @JavascriptInterface
        public void showToast(String message) {
            handler.post(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
        }

        @JavascriptInterface
        public boolean isOnline() {
            return OfflineCacheManager.isNetworkAvailable(MainActivity.this);
        }

        @JavascriptInterface
        public String getNetworkStatus() {
            return OfflineCacheManager.getNetworkQualityJson(MainActivity.this);
        }
    }

    private final BroadcastReceiver mediaActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) return;
            String action = intent.getAction();
            if (MusicPlaybackService.ACTION_PLAY.equals(action)) {
                triggerMediaAction("play");
            } else if (MusicPlaybackService.ACTION_PAUSE.equals(action)) {
                triggerMediaAction("pause");
            } else if (MusicPlaybackService.ACTION_NEXT.equals(action)) {
                triggerMediaAction("nexttrack");
            } else if (MusicPlaybackService.ACTION_PREV.equals(action)) {
                triggerMediaAction("previoustrack");
            } else if (MusicPlaybackService.ACTION_SEEK.equals(action)) {
                long posMs = intent.getLongExtra(MusicPlaybackService.EXTRA_SEEK_POSITION_MS, 0L);
                triggerSeekAction(posMs / 1000.0);
            } else if (MusicPlaybackService.ACTION_PLAY_TRACK.equals(action)) {
                String title = intent.getStringExtra(MusicPlaybackService.EXTRA_TITLE);
                String artist = intent.getStringExtra(MusicPlaybackService.EXTRA_ARTIST);
                triggerPlayTrack(title, artist);
            }
        }
    };

    private void triggerPlayTrack(String title, String artist) {
        if (appView == null) return;
        appView.post(() -> {
            String safeTitle = (title != null) ? title.replace("'", "\\'") : "";
            String safeArtist = (artist != null) ? artist.replace("'", "\\'") : "";
            String js = "(function(){"
                + "if (window.__triggerPlayTrackByName) {"
                + "  window.__triggerPlayTrackByName('" + safeTitle + "', '" + safeArtist + "');"
                + "} else if (window.__triggerMediaAction) {"
                + "  window.__triggerMediaAction('play');"
                + "}"
                + "})()";
            appView.evaluateJavascript(js, null);
        });
    }

    private void triggerSeekAction(double seconds) {
        if (appView == null) return;
        appView.post(() -> {
            String js = "(function(){"
                + "if (window.__triggerMediaSeek) { window.__triggerMediaSeek(" + seconds + "); return; }"
                + "var a = document.querySelector('audio');"
                + "if (a && Number.isFinite(" + seconds + ")) a.currentTime = " + seconds + ";"
                + "})()";
            appView.evaluateJavascript(js, null);
        });
    }

    private void startPlaybackService(Intent intent) {
        try {
            boolean isPlaying = intent.getBooleanExtra(MusicPlaybackService.EXTRA_IS_PLAYING, false);
            if (isPlaying && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        } catch (Exception e) {
            try {
                startService(intent);
            } catch (Exception ignored) {}
        }
    }

    private void triggerMediaAction(String action) {
        if (appView == null) return;
        appView.post(() -> {
            String js = "(function(){"
                + "if (window.__triggerMediaAction) { window.__triggerMediaAction('" + action + "'); return; }"
                + "var a = document.querySelector('audio');"
                + "if (a) {"
                + "  if ('" + action + "' === 'play') a.play();"
                + "  else if ('" + action + "' === 'pause') a.pause();"
                + "}"
                + "})()";
            appView.evaluateJavascript(js, null);
        });
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        sInstanceRef = new java.lang.ref.WeakReference<>(this);
        OfflineCacheManager.clearStaleCache(this);
        MusicReminderScheduler.schedulePeriodicReminders(this);
        handleReminderIntent(getIntent());
        handlePlayTrackIntent(getIntent());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Window w = getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.setStatusBarColor(0xFF080808);
        w.setNavigationBarColor(0xFF080808);
        setContentView(R.layout.activity_main);

        appView = findViewById(R.id.webview);
        splashView = findViewById(R.id.splash_webview);
        errorLayout = findViewById(R.id.error_layout);
        Button retry = findViewById(R.id.btn_retry);
        retry.setOnClickListener(v -> reloadApp());

        Button btnCheckUpdates = findViewById(R.id.btn_check_updates);
        if (btnCheckUpdates != null) {
            btnCheckUpdates.setOnClickListener(v -> UpdateManager.checkForUpdates(MainActivity.this, true));
        }

        // Ask for notification permission on app open
        checkAndRequestNotificationPermission();

        // Register media action receiver for notification buttons
        IntentFilter filter = new IntentFilter();
        filter.addAction(MusicPlaybackService.ACTION_PLAY);
        filter.addAction(MusicPlaybackService.ACTION_PAUSE);
        filter.addAction(MusicPlaybackService.ACTION_NEXT);
        filter.addAction(MusicPlaybackService.ACTION_PREV);
        filter.addAction(MusicPlaybackService.ACTION_SEEK);
        filter.addAction(MusicPlaybackService.ACTION_PLAY_TRACK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mediaActionReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(mediaActionReceiver, filter);
        }

        // Initialize update channel and schedule in-app update check
        UpdateManager.initNotificationChannel(this);
        boolean fromNotification = getIntent() != null && getIntent().getBooleanExtra(UpdateManager.EXTRA_CHECK_UPDATE, false);
        if (fromNotification) {
            UpdateManager.checkForUpdates(this, true);
        } else {
            handler.postDelayed(() -> UpdateManager.checkForUpdates(MainActivity.this, false), 3500);
        }

        setupSplash();
        setupApp();

        if (state != null && appView.restoreState(state) != null) { }
        else appView.loadUrl(APP_URL);

        handler.postDelayed(() -> { splashDone = true; revealWhenReady(); }, 12000);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null && intent.getBooleanExtra(UpdateManager.EXTRA_CHECK_UPDATE, false)) {
            UpdateManager.checkForUpdates(this, true);
        }
        handleReminderIntent(intent);
        handlePlayTrackIntent(intent);
    }

    private void handlePlayTrackIntent(Intent intent) {
        if (intent == null) return;
        if (MusicPlaybackService.ACTION_PLAY_TRACK.equals(intent.getAction()) || intent.hasExtra(MusicPlaybackService.EXTRA_TITLE)) {
            String title = intent.getStringExtra(MusicPlaybackService.EXTRA_TITLE);
            String artist = intent.getStringExtra(MusicPlaybackService.EXTRA_ARTIST);
            if (title != null && !title.trim().isEmpty()) {
                pendingPlayTitle = title;
                pendingPlayArtist = artist;
                if (pageReady && appView != null) {
                    triggerPlayTrack(title, artist);
                    pendingPlayTitle = null;
                    pendingPlayArtist = null;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Check for Updates");
        menu.add(0, 2, 1, "Refresh Page");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            UpdateManager.checkForUpdates(this, true);
            return true;
        } else if (item.getItemId() == 2) {
            reloadApp();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQ_NOTIFICATIONS);
            }
        }
    }

    private void setupSplash() {
        WebSettings s = splashView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        splashView.setBackgroundColor(0xFF080808);
        splashView.addJavascriptInterface(new SplashBridge(), "AndroidBridge");
        splashView.loadUrl("file:///android_asset/splash.html");
    }

    private void setupApp() {
        WebSettings s = appView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setTextZoom(100);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setUserAgentString(s.getUserAgentString() + " InstaMusicApp/1.0 (Android; Mobile)");

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(appView, true);
        appView.setBackgroundColor(0xFF05080D);

        appView.addJavascriptInterface(new MediaBridge(), "MediaBridge");
        appView.addJavascriptInterface(new MediaBridge(), "Android");

        appView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest req) {
                WebResourceResponse resp = OfflineCacheManager.shouldIntercept(MainActivity.this, req);
                if (resp != null) return resp;
                return super.shouldInterceptRequest(v, req);
            }

            @Override
            public void onPageStarted(WebView v, String url, Bitmap icon) {
                errorLayout.setVisibility(View.GONE);
                injectMediaBridgeScript(v);
            }

            @Override
            public void onPageFinished(WebView v, String url) {
                pageReady = true;
                errorLayout.setVisibility(View.GONE);
                injectMediaBridgeScript(v);
                revealWhenReady();
            }

            @Override
            public void onReceivedError(WebView v, WebResourceRequest req, WebResourceError err) {
                if (req.isForMainFrame()) {
                    if (OfflineCacheManager.hasCachedShell(MainActivity.this)) {
                        v.post(() -> {
                            errorLayout.setVisibility(View.GONE);
                            appView.setVisibility(View.VISIBLE);
                            revealWhenReady();
                        });
                        return;
                    }
                    pageReady = true;
                    errorLayout.setVisibility(View.VISIBLE);
                    appView.setVisibility(View.GONE);
                    revealWhenReady();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest req) {
                String u = req.getUrl().toString();
                return u.startsWith("intent:") || u.startsWith("market:") || u.startsWith("mailto:") || u.startsWith("tel:");
            }
        });

        appView.setWebChromeClient(new WebChromeClient());
    }

    private void injectMediaBridgeScript(WebView view) {
        if (view == null) return;
        try (java.io.InputStream is = getAssets().open("media_bridge.js");
             java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream()) {
            byte[] data = new byte[4096];
            int nRead;
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            String js = buffer.toString("UTF-8");
            view.evaluateJavascript(js, null);
        } catch (Exception ignored) {}
    }

    private void revealWhenReady() {
        if (!splashDone || (!pageReady && errorLayout.getVisibility() != View.VISIBLE) || splashHidden) return;
        splashHidden = true;
        if (errorLayout.getVisibility() != View.VISIBLE) appView.setVisibility(View.VISIBLE);
        splashView.animate().alpha(0f).setDuration(380).withEndAction(() -> splashView.setVisibility(View.GONE)).start();
        if (pendingPlayTitle != null) {
            triggerPlayTrack(pendingPlayTitle, pendingPlayArtist);
            pendingPlayTitle = null;
            pendingPlayArtist = null;
        }
    }

    private void reloadApp() {
        errorLayout.setVisibility(View.GONE);
        appView.setVisibility(View.VISIBLE);
        pageReady = false;
        appView.loadUrl(APP_URL);
    }

    @Override
    public void onBackPressed() {
        if (MusicPlaybackService.isMusicCurrentlyPlaying) {
            // Keep the WebView audio engine alive in background
            moveTaskToBack(true);
            Toast.makeText(this, "Music continues in the background", Toast.LENGTH_SHORT).show();
            return;
        }

        if (appView != null && appView.canGoBack()) {
            appView.goBack();
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastBackPressed < BACK_EXIT_MS) {
            super.onBackPressed();
        } else {
            lastBackPressed = now;
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleReminderIntent(Intent intent) {
        if (intent == null) return;
        if (intent.getBooleanExtra("from_reminder", false)) {
            String trackTitle = intent.getStringExtra("resume_title");
            if (trackTitle != null && !trackTitle.trim().isEmpty()) {
                Toast.makeText(this, "Resuming " + trackTitle, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Welcome back to InstaMusic", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isAppInForeground = true;
        if (appView != null) appView.onResume();
        UpdateManager.checkPendingInstallOnResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isAppInForeground = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        if (appView != null) appView.saveState(out);
        super.onSaveInstanceState(out);
    }

    @Override
    protected void onDestroy() {
        if (sInstanceRef != null && sInstanceRef.get() == this) {
            sInstanceRef = null;
        }
        try {
            unregisterReceiver(mediaActionReceiver);
        } catch (Exception ignored) {}
        handler.removeCallbacksAndMessages(null);
        if (appView != null) appView.destroy();
        if (splashView != null) splashView.destroy();
        super.onDestroy();
    }
}
