package io.github.trojan_gfw.igniter;

import android.app.Application;

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
        storage = new Storage(this);

        clashConfig = new ClashConfig(storage.getClashConfigPath());

        TrojanConfig.init(storage);
        trojanConfig = TrojanConfig.getInstance();
        trojanPreferences = new TrojanPreferences(this);
    }
}
