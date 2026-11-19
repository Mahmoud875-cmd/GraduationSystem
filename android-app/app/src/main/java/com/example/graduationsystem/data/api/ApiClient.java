
package com.example.graduationsystem.data.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.graduationsystem.util.Session;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Builds the Retrofit client for the Laravel API.
 */
public class ApiClient {

    public static final String DEFAULT_BASE_URL =
        "http://10.223.238.4:8000/api/";

    private static final String PREFS = "gs_server";
    private static final String KEY_URL = "base_url";

    private static volatile ApiService service;
    private static String baseUrl = DEFAULT_BASE_URL;

    /** Loads a previously saved server URL (if any) into the active base URL. */
    public static void loadFromPrefs(Context context) {
        if (context == null) return;
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String saved = prefs.getString(KEY_URL, null);
        if (saved != null && !saved.isEmpty()) {
            setBaseUrl(saved);
            return;
        }
        // No explicit URL yet: try to auto-discover the PC on the same Wi-Fi so the
        // app works out-of-the-box for LAN testing without editing anything.
        String lan = guessLanBaseUrl();
        if (lan != null) {
            setBaseUrl(lan);
        }
    }

    private static final String TAG = "ApiClient";

    /**
     * Best-effort LAN discovery of the Laravel dev server. If the phone can reach
     * &lt;phone-wifi-subnet&gt;.x:8000/api/login it returns that base URL, else null.
     */
    public static String guessLanBaseUrl() {
        try {
            String v4 = getLocalIPv4();
            if (v4 == null) return null;
            int dot = v4.lastIndexOf('.');
            if (dot < 0) return null;
            String prefix = v4.substring(0, dot + 1); // e.g. "10.69.19."
            // Try the phone's own gateway-ish candidates + the host itself.
            int[] lastOctets = {4, 1, 2, 3, 100};
            java.net.InetAddress addr;
            for (int last : lastOctets) {
                String ip = prefix + last;
                addr = java.net.InetAddress.getByName(ip);
                if (probe(addr)) {
                    String url = "http://" + ip + ":8000/api/";
                    Log.i(TAG, "Auto-detected LAN server at " + url);
                    return url;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /** Returns the device's non-loopback IPv4 (Wi-Fi) address, or null. */
    private static String getLocalIPv4() {
        try {
            for (NetworkInterface intf : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!intf.isUp() || intf.isLoopback()) continue;
                for (InetAddress a : Collections.list(intf.getInetAddresses())) {
                    if (a instanceof Inet4Address) {
                        String s = a.getHostAddress();
                        if (s != null && !s.startsWith("127.")) return s;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /** Opens a short TCP connect to host:8000 to see if the dev server is there. */
    private static boolean probe(java.net.InetAddress host) {
        try (java.net.Socket s = new java.net.Socket()) {
            s.connect(new java.net.InetSocketAddress(host, 8000), 250);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void setBaseUrl(String url) {
        if (url != null && !url.isEmpty()) {
            if (!url.endsWith("/")) {
                url = url + "/";
            }

            baseUrl = url;
            service = null;
        }
    }

    /** Persists the server URL so it survives restarts and applies without rebuilding. */
    public static void saveBaseUrl(Context context, String url) {
        if (context == null) return;
        context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY_URL, url).apply();
        setBaseUrl(url);
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    public static ApiService get(Context context) {
        if (service == null) {
            synchronized (ApiClient.class) {
                if (service == null) {
                    service = build(context.getApplicationContext());
                }
            }
        }

        return service;
    }

    private static ApiService build(Context appContext) {
        Session session = Session.get(appContext);

        HttpLoggingInterceptor logging =
            new HttpLoggingInterceptor();
        logging.setLevel(
            HttpLoggingInterceptor.Level.BODY
        );

        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .addInterceptor(chain -> {
                Request original = chain.request();

                Request.Builder builder = original.newBuilder()
                    .header("Accept", "application/json");

                String token = session.getToken();

                if (token != null && !token.isEmpty()) {
                    builder.header(
                        "Authorization",
                        "Bearer " + token
                    );
                }

                return chain.proceed(builder.build());
            })
            .build();

        return new Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(ApiService.class);
    }
}
