package io.github.trojan_gfw.igniter;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import androidx.annotation.Nullable;

import java.util.List;

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

    @Nullable
    public String getProcessName(int pid) {
        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
        if (runningAppProcesses == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo info : runningAppProcesses) {
            if (info.pid == pid) {
                return info.processName;
            }
        }
        return null;
    }
}
