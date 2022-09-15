package io.github.trojan_gfw.igniter.tile;

import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import androidx.core.content.ContextCompat;

import io.github.trojan_gfw.igniter.BuildConfig;
import io.github.trojan_gfw.igniter.IgniterApplication;
import io.github.trojan_gfw.igniter.MainActivity;
import io.github.trojan_gfw.igniter.ProxyService;
import io.github.trojan_gfw.igniter.R;
import io.github.trojan_gfw.igniter.persistence.Storage;
import io.github.trojan_gfw.igniter.persistence.TrojanConfig;

/**
 * Helper class for starting or stopping {@link ProxyService}. Before starting {@link ProxyService},
 * make sure the TrojanConfig is valid (with the help of {@link #isTrojanConfigValid()} and whether
 * user has consented VPN Service (with the help of {@link #isVPNServiceConsented(Context)}.
 * <br/>
 * It's recommended to start launcher activity when the config is invalid or user hasn't consented
 * VPN service.
 */
public abstract class ProxyHelper {

    public static boolean isVPNServiceConsented(Context context) {
        return VpnService.prepare(context.getApplicationContext()) == null;
    }

    public static void startProxyService(Context context) {
        IgniterApplication app = IgniterApplication.getApplication();
        Intent intent = new Intent(context, ProxyService.class);
        ContextCompat.startForegroundService(context, intent);
    }

    public static void startLauncherActivity(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public static void stopProxyService(Context context) {
        Intent intent = new Intent(context.getString(R.string.stop_service));
        intent.setPackage(context.getPackageName());
        context.sendBroadcast(intent);
    }
}
