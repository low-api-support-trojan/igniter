package io.github.trojan_gfw.igniter.persistence;

import android.content.Context;
import android.content.SharedPreferences;

public class TrojanPreferences {

    // Preferences Names
    public static final String TROJAN_PREFERENCE_NAME = "TROJAN_PREFERENCE";

    // Private keys
    public static final String KEY_ENABLE_IPV6 = "enable_ipv6";

    // Multi Process shared keys
    public static final String KEY_EVER_STARTED = "ever_started";
    public static final String KEY_ENABLE_CLASH = "enable_clash";
    public static final String KEY_ENABLE_LAN = "enable_lan";

    boolean enableIPV6 = false;
    boolean enableClash = false;
    boolean everStarted = false;
    boolean enableLan = false;

    Context context;
    SharedPreferences sharedPreferences;

    public TrojanPreferences(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(TROJAN_PREFERENCE_NAME, Context.MODE_PRIVATE);
        enableIPV6 = sharedPreferences.getBoolean(KEY_ENABLE_IPV6, false);
        everStarted = sharedPreferences.getBoolean(KEY_EVER_STARTED, false);
        enableClash = sharedPreferences.getBoolean(KEY_ENABLE_CLASH, false);
        enableLan = sharedPreferences.getBoolean(KEY_ENABLE_LAN, false);
    }

    public void setEnableIPV6(boolean enableIPV6) {
        this.enableIPV6 = enableIPV6;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_ENABLE_IPV6, enableIPV6);
        editor.commit();
    }

    public boolean getEnableIPV6() {
        return enableIPV6;
    }

    private void setString(String key, String value) {
        sharedPreferences.edit().putString(key, value).commit();
    }

    private String getString(String key, String value) {
        return sharedPreferences.getString(key, value);
    }

    private void setBoolean(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).commit();
    }

    private boolean getBoolean(String key, boolean fallback) {
        return sharedPreferences.getBoolean(key, fallback);
    }

    public boolean isEverStarted() {
        return everStarted;
    }

    public void setEverStarted(boolean everStarted) {
        this.everStarted = everStarted;
        setBoolean(KEY_EVER_STARTED, everStarted);
    }

    public boolean getEnableClash() {
        return enableClash;
    }
    public void setEnableClash(boolean enableClash) {
        this.enableClash = enableClash;
        setBoolean(KEY_ENABLE_CLASH, enableClash);
    }

    public boolean isEnableLan() {
        return enableLan;
    }

    public void setEnableLan(boolean enableLan) {
        this.enableLan = enableLan;
        setBoolean(KEY_ENABLE_LAN, enableLan);
    }
}
