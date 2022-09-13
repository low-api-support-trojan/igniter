package io.github.trojan_gfw.igniter.initializer;

import android.content.Context;

import io.github.trojan_gfw.igniter.IgniterApplication;
import io.github.trojan_gfw.igniter.common.os.MultiProcessSP;
import io.github.trojan_gfw.igniter.persistence.TrojanConfig;

/**
 * Initializer that runs in Main Process (Default process).
 */
public class MainInitializer extends Initializer {

    @Override
    public void init(Context context) {
        MultiProcessSP.init(context);
        MultiProcessSP.setIsFirstStart(false);
        TrojanConfig.init(IgniterApplication.getApplication().storage);
    }

    @Override
    public boolean runsInWorkerThread() {
        return false;
    }
}
