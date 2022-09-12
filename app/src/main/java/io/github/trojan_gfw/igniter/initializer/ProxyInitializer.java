package io.github.trojan_gfw.igniter.initializer;

import android.content.Context;

import io.github.trojan_gfw.igniter.LogHelper;
import io.github.trojan_gfw.igniter.persistence.Storage;
import io.github.trojan_gfw.igniter.persistence.TrojanConfig;

public class ProxyInitializer extends Initializer {
    private static final String TAG = "ProxyInitializer";

    @Override
    public void init(Context context) {
        Storage storage = Storage.getSharedInstance(context);
        TrojanConfig cacheConfig = TrojanConfig.read(storage.getTrojanConfigPath());
        if (cacheConfig == null) {
            LogHelper.e(TAG, "read null trojan config");
        } else {
            cacheConfig.setCaCertPath(storage.getCaCertPath());
            TrojanConfig.setInstance(cacheConfig);
        }
        if (!TrojanConfig.getInstance().isValidRunningConfig()) {
            LogHelper.e(TAG, "Invalid trojan config!");
        }
    }

    @Override
    public boolean runsInWorkerThread() {
        return false;
    }
}
