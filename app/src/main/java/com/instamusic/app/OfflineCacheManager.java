package com.instamusic.app;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OfflineCacheManager {
    private static final String TAG = "OfflineCacheManager";
    private static final String CACHE_DIR_NAME = "offline_web_cache";
    private static final String ASSET_BASE_PATH = "offline_web";
    private static final String TARGET_HOST = "instamusic.lovable.app";

    private static final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
    private static volatile boolean updateScheduled = false;

    public static String getNetworkQualityJson(Context context) {
        if (context == null) {
            return "{\"online\":false,\"percent\":0,\"bandwidthKbps\":0,\"label\":\"Offline\",\"type\":\"none\"}";
        }
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) {
                return "{\"online\":false,\"percent\":0,\"bandwidthKbps\":0,\"label\":\"Offline\",\"type\":\"none\"}";
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network active = cm.getActiveNetwork();
                if (active == null) {
                    return "{\"online\":false,\"percent\":0,\"bandwidthKbps\":0,\"label\":\"Offline\",\"type\":\"none\"}";
                }
                NetworkCapabilities cap = cm.getNetworkCapabilities(active);
                if (cap == null || !cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    return "{\"online\":false,\"percent\":0,\"bandwidthKbps\":0,\"label\":\"Offline\",\"type\":\"none\"}";
                }

                boolean isWifi = cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
                boolean isCellular = cap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
                int bandwidthKbps = cap.getLinkDownstreamBandwidthKbps();

                int percent;
                String label;

                if (bandwidthKbps <= 0) {
                    percent = isWifi ? 85 : 65;
                    label = isWifi ? "Good internet" : "Mobile data";
                } else if (bandwidthKbps >= 15000) {
                    percent = Math.min(100, 92 + (bandwidthKbps / 10000));
                    label = "Fast internet";
                } else if (bandwidthKbps >= 5000) {
                    percent = 75 + (int) ((bandwidthKbps - 5000) * 16.0 / 10000.0);
                    label = "Good internet";
                } else if (bandwidthKbps >= 2000) {
                    percent = 50 + (int) ((bandwidthKbps - 2000) * 24.0 / 3000.0);
                    label = "Good internet";
                } else if (bandwidthKbps >= 600) {
                    percent = 32 + (int) ((bandwidthKbps - 600) * 18.0 / 1400.0);
                    label = "Stable internet";
                } else {
                    percent = Math.max(10, (int) (bandwidthKbps * 30.0 / 600.0));
                    label = "Slow connection";
                }

                String transport = isWifi ? "Wi-Fi" : (isCellular ? "Cellular" : "Ethernet");
                return "{\"online\":true,\"percent\":" + percent + ",\"bandwidthKbps\":" + bandwidthKbps + ",\"label\":\"" + label + "\",\"type\":\"" + transport + "\"}";
            } else {
                NetworkInfo info = cm.getActiveNetworkInfo();
                if (info == null || !info.isConnected()) {
                    return "{\"online\":false,\"percent\":0,\"bandwidthKbps\":0,\"label\":\"Offline\",\"type\":\"none\"}";
                }
                boolean isWifi = info.getType() == ConnectivityManager.TYPE_WIFI;
                int percent = isWifi ? 85 : 65;
                String label = isWifi ? "Good internet" : "Mobile data";
                return "{\"online\":true,\"percent\":" + percent + ",\"bandwidthKbps\":5000,\"label\":\"" + label + "\",\"type\":\"" + (isWifi ? "Wi-Fi" : "Cellular") + "\"}";
            }
        } catch (Exception e) {
            return "{\"online\":true,\"percent\":75,\"bandwidthKbps\":4000,\"label\":\"Good internet\",\"type\":\"Network\"}";
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        if (context == null) return false;
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network active = cm.getActiveNetwork();
                if (active == null) return false;
                NetworkCapabilities cap = cm.getNetworkCapabilities(active);
                return cap != null && (
                    cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    cap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    cap.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                );
            } else {
                NetworkInfo info = cm.getActiveNetworkInfo();
                return info != null && info.isConnected();
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean hasCachedShell(Context context) {
        if (context == null) return false;
        File diskHtml = new File(getCacheDir(context), "index.html");
        if (diskHtml.exists() && diskHtml.length() > 0) return true;
        try (InputStream is = context.getAssets().open(ASSET_BASE_PATH + "/index.html")) {
            return is != null;
        } catch (Exception e) {
            return false;
        }
    }

    public static WebResourceResponse shouldIntercept(Context context, WebResourceRequest request) {
        if (context == null || request == null || request.getUrl() == null) return null;
        Uri uri = request.getUrl();
        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) return null;

        String host = uri.getHost();
        if (host == null) return null;

        boolean isTargetHost = host.equalsIgnoreCase(TARGET_HOST);

        if (isTargetHost) {
            String path = uri.getPath();
            if (path == null || path.isEmpty()) path = "/";

            boolean isHtmlRoute = path.equals("/") ||
                                  path.equals("/home") ||
                                  path.equals("/library") ||
                                  path.equals("/favorites") ||
                                  path.equals("/search") ||
                                  path.equals("/profile") ||
                                  path.equals("/create") ||
                                  path.equals("/welcome") ||
                                  path.equals("/now-playing") ||
                                  path.startsWith("/artist/") ||
                                  path.startsWith("/song/") ||
                                  path.startsWith("/playlist/") ||
                                  path.startsWith("/mix/") ||
                                  path.startsWith("/creator/") ||
                                  path.startsWith("/genre/") ||
                                  (!path.contains(".") && !path.startsWith("/api/"));

            if (isHtmlRoute) {
                // Always serve local index.html immediately for instant startup and offline support
                InputStream is = openCachedOrAsset(context, "index.html");
                if (is != null) {
                    if (isNetworkAvailable(context)) {
                        refreshHtmlInBackground(context, uri.toString());
                    }
                    return createResponse("text/html", is);
                }
            }

            // Static assets: /assets/*, /__l5e/*, /favicon.png
            if (path.startsWith("/assets/") || path.startsWith("/__l5e/") || path.equals("/favicon.png")) {
                String relativePath = path.startsWith("/") ? path.substring(1) : path;
                String mimeType = guessMimeType(path);

                // Check bundled assets and local disk cache first
                InputStream is = openCachedOrAsset(context, relativePath);
                if (is != null) {
                    return createResponse(mimeType, is);
                }

                // If not found locally and network is available, fetch and cache
                if (isNetworkAvailable(context)) {
                    return fetchAndCacheAsset(context, uri.toString(), relativePath, mimeType);
                }
            }
        }

        // Google Fonts handling when offline: return harmless empty CSS/font response so it doesn't fail
        if (host.contains("fonts.googleapis.com") || host.contains("fonts.gstatic.com")) {
            if (!isNetworkAvailable(context)) {
                String emptyCss = "/* offline fonts fallback */";
                ByteArrayInputStream bais = new ByteArrayInputStream(emptyCss.getBytes(StandardCharsets.UTF_8));
                return createResponse("text/css", bais);
            }
        }

        return null;
    }

    private static InputStream openCachedOrAsset(Context context, String relativePath) {
        try {
            File diskFile = new File(getCacheDir(context), relativePath);
            if (diskFile.exists() && diskFile.length() > 0) {
                return new FileInputStream(diskFile);
            }
        } catch (Exception ignored) {}

        try {
            AssetManager am = context.getAssets();
            return am.open(ASSET_BASE_PATH + "/" + relativePath);
        } catch (Exception ignored) {}

        return null;
    }

    private static WebResourceResponse createResponse(String mimeType, InputStream is) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Cache-Control", "public, max-age=86400");
        return new WebResourceResponse(mimeType, "UTF-8", 200, "OK", headers, is);
    }

    private static String guessMimeType(String path) {
        if (path == null) return "application/octet-stream";
        String lower = path.toLowerCase();
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html";
        if (lower.endsWith(".js") || lower.endsWith(".mjs")) return "application/javascript";
        if (lower.endsWith(".css")) return "text/css";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".ico")) return "image/x-icon";
        if (lower.endsWith(".woff2")) return "font/woff2";
        if (lower.endsWith(".woff")) return "font/woff";
        if (lower.endsWith(".ttf")) return "font/ttf";
        if (lower.endsWith(".json")) return "application/json";
        return "application/octet-stream";
    }

    private static File getCacheDir(Context context) {
        File dir = new File(context.getFilesDir(), CACHE_DIR_NAME);
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    private static WebResourceResponse fetchAndCacheAsset(Context context, String urlStr, String relativePath, String mimeType) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(10000);
            conn.connect();

            int code = conn.getResponseCode();
            if (code == 200) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try (InputStream in = new BufferedInputStream(conn.getInputStream())) {
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = in.read(buf)) != -1) {
                        bos.write(buf, 0, len);
                    }
                }
                byte[] data = bos.toByteArray();

                backgroundExecutor.execute(() -> {
                    try {
                        File targetFile = new File(getCacheDir(context), relativePath);
                        File parent = targetFile.getParentFile();
                        if (parent != null && !parent.exists()) parent.mkdirs();
                        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                            fos.write(data);
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Failed to cache asset: " + relativePath, e);
                    }
                });

                return createResponse(mimeType, new ByteArrayInputStream(data));
            }
        } catch (Exception e) {
            Log.d(TAG, "Network fetch failed for " + urlStr + ", checking offline asset");
        }

        InputStream fallback = openCachedOrAsset(context, relativePath);
        if (fallback != null) {
            return createResponse(mimeType, fallback);
        }

        return null;
    }

    private static void refreshHtmlInBackground(Context context, String urlStr) {
        if (updateScheduled) return;
        updateScheduled = true;
        backgroundExecutor.execute(() -> {
            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(6000);
                conn.setReadTimeout(8000);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 InstaMusicApp/1.0 (Android; Mobile)");
                conn.connect();

                if (conn.getResponseCode() == 200) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    try (InputStream in = new BufferedInputStream(conn.getInputStream())) {
                        byte[] buf = new byte[8192];
                        int len;
                        while ((len = in.read(buf)) != -1) {
                            bos.write(buf, 0, len);
                        }
                    }
                    byte[] data = bos.toByteArray();
                    if (data.length > 500) {
                        File diskHtml = new File(getCacheDir(context), "index.html");
                        try (FileOutputStream fos = new FileOutputStream(diskHtml)) {
                            fos.write(data);
                        }
                        Log.d(TAG, "Successfully refreshed cached index.html (" + data.length + " bytes)");
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "Could not refresh index.html in background: " + e.getMessage());
            } finally {
                updateScheduled = false;
            }
        });
    }
}
