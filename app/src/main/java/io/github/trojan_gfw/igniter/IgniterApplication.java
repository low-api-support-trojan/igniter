package io.github.trojan_gfw.igniter;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Process;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.List;

import io.github.trojan_gfw.igniter.persistence.Storage;
import io.github.trojan_gfw.igniter.persistence.TrojanConfig;
import io.github.trojan_gfw.igniter.persistence.TrojanPreferences;

public class IgniterApplication extends Application {

    public static String PROCESS_ID_TOOL = ":tool";
    public static String PROCESS_ID_PROXY = ":proxy";

    public static IgniterApplication instance;

    public static IgniterApplication getApplication() {
        return instance;
    }

    // Sharable Singletons
    public Storage storage;
    public TrojanConfig trojanConfig;
    public TrojanPreferences trojanPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        storage = new Storage(this);
        TrojanConfig.init(storage);
        trojanConfig = TrojanConfig.getInstance();
        trojanPreferences = new TrojanPreferences(this);
        runInit(this);
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

    public void runInit(Context context) {
        final String processName = IgniterApplication.getApplication().getProcessName(Process.myPid());
    }
}
