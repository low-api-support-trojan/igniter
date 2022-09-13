package io.github.trojan_gfw.igniter;

import android.app.Application;

import io.github.trojan_gfw.igniter.initializer.InitializerHelper;
import io.github.trojan_gfw.igniter.persistence.Storage;

public class IgniterApplication extends Application {

    public static IgniterApplication instance;

    public static IgniterApplication getApplication() {
        return instance;
    }

    // Sharable Singletons
    public Storage storage;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        storage = new Storage(this);
        InitializerHelper.runInit(this);
    }
}
