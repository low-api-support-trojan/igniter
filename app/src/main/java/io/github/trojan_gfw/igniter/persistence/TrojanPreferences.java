package io.github.trojan_gfw.igniter.persistence;

import android.content.Context;
import android.content.SharedPreferences;

public class TrojanPreferences {

    // Preferences Names
    public static final String TROJAN_PREFERENCE_NAME = "TROJAN_PREFERENCE";
    public static final String TROJAN_MULTI_PROCESS_PREFERENCE_NAME = "TROJAN_MULTTI_PROCESS_PREFERENCE";

    // Private keys
    public static final String KEY_ENABLE_IPV6 = "enable_ipv6";

    // Multi Process shared keys
    public static final String KEY_EVER_STARTED = "first_start";
    public static final String KEY_ENABLE_CLASH = "enable_clash";

    boolean enableIPV6 = false;
    boolean enableClash = false;
    boolean everStarted = false;
    Context context;
    SharedPreferences sharedPreferences;
    SharedPreferences multiProcessSharedPreferences;

    public TrojanPreferences(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(TROJAN_PREFERENCE_NAME, Context.MODE_PRIVATE);
        multiProcessSharedPreferences = context.getSharedPreferences(TROJAN_MULTI_PROCESS_PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
        enableIPV6 = sharedPreferences.getBoolean(KEY_ENABLE_IPV6, false);
        everStarted = multiProcessSharedPreferences.getBoolean(KEY_EVER_STARTED, false);
        enableClash = multiProcessSharedPreferences.getBoolean(KEY_ENABLE_CLASH, false);
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
        multiProcessSharedPreferences.edit().putString(key, value).commit();
    }

    private String getString(String key, String value) {
        return multiProcessSharedPreferences.getString(key, value);
    }

    private void setBoolean(String key, boolean value) {
        multiProcessSharedPreferences.edit().putBoolean(key, value).commit();
    }

    private boolean getBoolean(String key, boolean fallback) {
        return multiProcessSharedPreferences.getBoolean(key, fallback);
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
}
