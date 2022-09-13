package io.github.trojan_gfw.igniter.initializer;

import android.content.Context;

import io.github.trojan_gfw.igniter.IgniterApplication;
import io.github.trojan_gfw.igniter.common.os.MultiProcessSP;
import io.github.trojan_gfw.igniter.persistence.TrojanConfig;

/**
 * Initializer that runs in Tools Process.
 */
public class ToolInitializer extends Initializer {

    @Override
    public void init(Context context) {
        MultiProcessSP.init(context);
        TrojanConfig.init(IgniterApplication.getApplication().storage);
    }

    @Override
    public boolean runsInWorkerThread() {
        return false;
    }
}
