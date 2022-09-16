package io.github.trojan_gfw.igniter;

import static junit.framework.TestCase.assertTrue;

import android.content.Context;

import androidx.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import io.github.trojan_gfw.igniter.persistence.Storage;

@RunWith(AndroidJUnit4.class)
public class StorageTest {
     Context instrumentationContext;

    @Before
    public void setup() {
        instrumentationContext =  androidx.test.core.app.ApplicationProvider.getApplicationContext();
    }
    @Test
    public void shouldInit() throws IOException {

        Storage storage = new Storage(instrumentationContext);
        storage.reset();

        String[] paths = {
                storage.getCaCertPath(),
                storage.getCountryMmdbPath(),
                storage.getClashConfigPath()
        };

        int[] ids = {
                R.raw.cacert,
                R.raw.country,
                R.raw.clash_config
        };

        for(String filename: paths) {
            assertTrue(!new File(filename).exists());
        }
        storage.init();
        for(int i = 0; i < paths.length; i++) {
            String filename = paths[i];
            String rawString = storage.readRawText(ids[i]);
            String content = Storage.read(filename);
            assertTrue(content.equals(rawString));
        }
    }
}
