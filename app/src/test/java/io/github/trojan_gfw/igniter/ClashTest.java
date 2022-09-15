package io.github.trojan_gfw.igniter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import io.github.trojan_gfw.igniter.persistence.ClashConfig;
import io.github.trojan_gfw.igniter.persistence.Storage;

public class ClashTest {
    @Test
    public void shouldParseYaml() throws IOException {
        String filename = "./src/test/java/io/github/trojan_gfw/igniter/data/scratch.yml";
        File f = new File(filename);
        String text = Storage.read(f.getAbsolutePath());
        ClashConfig cc = new ClashConfig(f.getAbsolutePath());
        File f1 = new File(filename + ".tmp");
        cc.save(f1.getAbsolutePath());
        String text1 = Storage.read(f1.getAbsolutePath());
        ClashConfig cc1 = new ClashConfig(f1.getAbsolutePath());
        assertEquals(cc1.data, cc.data);
        int port = cc1.getPort();
        int trojanPort = cc1.getTrojanPort();
        assertTrue(port == 1180);
        assertTrue(trojanPort == 1080);
        cc1.setPort(1181);
        cc1.setTrojanPort(1081);
        port = cc1.getPort();
        trojanPort = cc1.getTrojanPort();
        assertTrue(port == 1181);
        assertTrue(trojanPort == 1081);
        f1.delete();
    }
}