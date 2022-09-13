package io.github.trojan_gfw.igniter.persistence;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class TrojanPreferences {
    public static final String TROJAN_PREFERENCE_NAME = "TROJAN_PREFERENCE";
    public static final String KEY_ENABLE_IPV6 = "enable_ipv6";
    boolean enableIPV6 = false;
    Context context;
    SharedPreferences sharedPreferences;

    public TrojanPreferences(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(TROJAN_PREFERENCE_NAME, MODE_PRIVATE);
        enableIPV6 = sharedPreferences.getBoolean(KEY_ENABLE_IPV6, false);
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
}
