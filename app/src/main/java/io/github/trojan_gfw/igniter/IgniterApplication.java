package io.github.trojan_gfw.igniter;

import android.app.Application;
import android.content.Intent;

import androidx.core.content.ContextCompat;

import io.github.trojan_gfw.igniter.persistence.ClashConfig;
import io.github.trojan_gfw.igniter.persistence.ServerList;
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
    public ServerList servers;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        init();
    }

    public void init() {
        trojanPreferences = new TrojanPreferences(this);

        storage = new Storage(this);
        storage.check();
        // Make sure the CA file exists;
        trojanConfig = TrojanConfig.getInstance(storage);
        clashConfig = new ClashConfig(storage.getClashConfigPath());
        servers = new ServerList(this);
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
