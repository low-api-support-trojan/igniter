package io.github.trojan_gfw.igniter;

import android.app.Application;
import android.content.Intent;

import androidx.core.content.ContextCompat;

import io.github.trojan_gfw.igniter.persistence.ClashConfig;
import io.github.trojan_gfw.igniter.persistence.Storage;
import io.github.trojan_gfw.igniter.persistence.TrojanConfig;
import io.github.trojan_gfw.igniter.persistence.TrojanPreferences;

public class IgniterApplication extends Application {
    public static IgniterApplication instance;

    public static IgniterApplication getApplication() {
        return instance;
    }

    // Sharable Singletons
    public Storage storage;
    public ClashConfig clashConfig;
    public TrojanConfig trojanConfig;
    public TrojanPreferences trojanPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        trojanPreferences = new TrojanPreferences(this);

        storage = new Storage(this);
        if (!trojanPreferences.isEverStarted()) {
            storage.reset();
            trojanPreferences.setEverStarted(true);
        }
        TrojanConfig.init(storage);
        trojanConfig = TrojanConfig.getInstance();
        clashConfig = new ClashConfig(storage.getClashConfigPath());
    }

    public void startProxyService() {
        Intent intent = new Intent(this, ProxyService.class);
        ContextCompat.startForegroundService(this, intent);
    }

    public void stopProxyService() {
        Intent intent = new Intent(this.getString(R.string.stop_service));
        intent.setPackage(getPackageName());
        this.sendBroadcast(intent);
    }

    public void startLauncherActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.startActivity(intent);
    }
}
