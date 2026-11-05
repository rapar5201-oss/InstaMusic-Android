package com.instamusic.app;

import android.app.Activity;
import android.graphics.Bitmap;
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
    private WebView appView, splashView;
    private LinearLayout errorLayout;
    private boolean pageReady = false, splashDone = false, splashHidden = false;
    private long lastBackPressed = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public class SplashBridge {
        @JavascriptInterface public void onSplashComplete() {
            handler.post(() -> { splashDone = true; revealWhenReady(); });
        }
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Window w=getWindow(); w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.setStatusBarColor(0xFF080808); w.setNavigationBarColor(0xFF080808);
        setContentView(R.layout.activity_main);
        appView=findViewById(R.id.webview); splashView=findViewById(R.id.splash_webview);
        errorLayout=findViewById(R.id.error_layout);
        Button retry=findViewById(R.id.btn_retry); retry.setOnClickListener(v -> reloadApp());
        setupSplash(); setupApp();
        if (state != null && appView.restoreState(state) != null) { } else appView.loadUrl(APP_URL);
        // Keep the animated split-screen visible while the remote site finishes loading.
        handler.postDelayed(() -> { splashDone=true; revealWhenReady(); }, 12000);
    }

    private void setupSplash() {
        WebSettings s=splashView.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true);
        splashView.setBackgroundColor(0xFF080808); splashView.addJavascriptInterface(new SplashBridge(), "AndroidBridge");
        splashView.loadUrl("file:///android_asset/splash.html");
    }
    private void setupApp() {
        WebSettings s=appView.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true); s.setMediaPlaybackRequiresUserGesture(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE); s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true); s.setSupportZoom(false); s.setBuiltInZoomControls(false);
        s.setCacheMode(WebSettings.LOAD_DEFAULT); s.setUserAgentString(s.getUserAgentString()+" InstaMusicApp/1.0 (Android; Mobile)");
        CookieManager.getInstance().setAcceptCookie(true); CookieManager.getInstance().setAcceptThirdPartyCookies(appView,true);
        appView.setBackgroundColor(0xFF05080D);
        appView.setWebViewClient(new WebViewClient() {
            @Override public void onPageStarted(WebView v,String url,Bitmap icon) { errorLayout.setVisibility(View.GONE); }
            @Override public void onPageFinished(WebView v,String url) { pageReady=true; errorLayout.setVisibility(View.GONE); revealWhenReady(); }
            @Override public void onReceivedError(WebView v,WebResourceRequest req,WebResourceError err) {
                if (req.isForMainFrame()) { pageReady=true; errorLayout.setVisibility(View.VISIBLE); appView.setVisibility(View.GONE); revealWhenReady(); }
            }
            @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest req) {
                String u=req.getUrl().toString(); return u.startsWith("intent:")||u.startsWith("market:")||u.startsWith("mailto:")||u.startsWith("tel:");
            }
        });
        appView.setWebChromeClient(new WebChromeClient());
    }
    private void revealWhenReady() {
        if (!splashDone || (!pageReady && errorLayout.getVisibility()!=View.VISIBLE) || splashHidden) return;
        splashHidden=true;
        if (errorLayout.getVisibility()!=View.VISIBLE) appView.setVisibility(View.VISIBLE);
        splashView.animate().alpha(0f).setDuration(380).withEndAction(() -> splashView.setVisibility(View.GONE)).start();
    }
    private void reloadApp() { errorLayout.setVisibility(View.GONE); appView.setVisibility(View.VISIBLE); pageReady=false; appView.loadUrl(APP_URL); }
    @Override public void onBackPressed() {
        if (appView!=null && appView.canGoBack()) { appView.goBack(); return; }
        long now=System.currentTimeMillis();
        if (now-lastBackPressed<BACK_EXIT_MS) super.onBackPressed();
        else { lastBackPressed=now; Toast.makeText(this,"Press back again to exit",Toast.LENGTH_SHORT).show(); }
    }
    @Override protected void onResume() { super.onResume(); if(appView!=null) appView.onResume(); }
    @Override protected void onPause() { if(appView!=null) appView.onPause(); super.onPause(); }
    @Override protected void onSaveInstanceState(Bundle out) { if(appView!=null) appView.saveState(out); super.onSaveInstanceState(out); }
    @Override protected void onDestroy() { handler.removeCallbacksAndMessages(null); if(appView!=null) appView.destroy(); if(splashView!=null) splashView.destroy(); super.onDestroy(); }
}
