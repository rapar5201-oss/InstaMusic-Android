package com.instamusic.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String APP_URL = "https://instamusic.lovable.app";
    private static final long BACK_EXIT_MS = 2000;
    private static final int PERMISSION_REQ_NOTIFICATIONS = 1001;

    private WebView appView, splashView;
    private LinearLayout errorLayout;
    private boolean pageReady = false, splashDone = false, splashHidden = false;
    private long lastBackPressed = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());

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
            handler.post(() -> {
                checkAndRequestNotificationPermission();
                Intent intent = new Intent(MainActivity.this, MusicPlaybackService.class);
                intent.setAction(MusicPlaybackService.ACTION_UPDATE_METADATA);
                intent.putExtra(MusicPlaybackService.EXTRA_TITLE, title);
                intent.putExtra(MusicPlaybackService.EXTRA_ARTIST, artist);
                intent.putExtra(MusicPlaybackService.EXTRA_ALBUM, album);
                intent.putExtra(MusicPlaybackService.EXTRA_ARTWORK_URL, artworkUrl);
                intent.putExtra(MusicPlaybackService.EXTRA_IS_PLAYING, isPlaying);
                startPlaybackService(intent);
            });
        }

        @JavascriptInterface
        public void updatePlaybackState(boolean isPlaying) {
            updatePlaybackInfo(0, 0, isPlaying);
        }

        @JavascriptInterface
        public void updatePlaybackInfo(double positionSeconds, double durationSeconds, boolean isPlaying) {
            handler.post(() -> {
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
            }
        }
    };

    private void startPlaybackService(Intent intent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

        // Ask for notification permission on app open
        checkAndRequestNotificationPermission();

        // Register media action receiver for notification buttons
        IntentFilter filter = new IntentFilter();
        filter.addAction(MusicPlaybackService.ACTION_PLAY);
        filter.addAction(MusicPlaybackService.ACTION_PAUSE);
        filter.addAction(MusicPlaybackService.ACTION_NEXT);
        filter.addAction(MusicPlaybackService.ACTION_PREV);
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
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setUserAgentString(s.getUserAgentString() + " InstaMusicApp/1.0 (Android; Mobile)");

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(appView, true);
        appView.setBackgroundColor(0xFF05080D);

        appView.addJavascriptInterface(new MediaBridge(), "MediaBridge");

        appView.setWebViewClient(new WebViewClient() {
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
    }

    private void reloadApp() {
        errorLayout.setVisibility(View.GONE);
        appView.setVisibility(View.VISIBLE);
        pageReady = false;
        appView.loadUrl(APP_URL);
    }

    @Override
    public void onBackPressed() {
        if (appView != null && appView.canGoBack()) { appView.goBack(); return; }
        long now = System.currentTimeMillis();
        if (now - lastBackPressed < BACK_EXIT_MS) super.onBackPressed();
        else {
            lastBackPressed = now;
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (appView != null) appView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        if (appView != null) appView.saveState(out);
        super.onSaveInstanceState(out);
    }

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(mediaActionReceiver);
        } catch (Exception ignored) {}
        handler.removeCallbacksAndMessages(null);
        if (appView != null) appView.destroy();
        if (splashView != null) splashView.destroy();
        super.onDestroy();
    }
}
