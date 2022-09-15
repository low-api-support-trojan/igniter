package io.github.trojan_gfw.igniter.persistence;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import clash.Clash;
import clash.ClashStartOptions;

public class ClashConfig {
    public static String TAG = "ClashConfig";
    // KEYS
    private static String KEY_SOCKS_PORT = "socks-port";
    private static String KEY_PROXIES = "proxies";
    private static String KEY_NAME = "name";
    private static String KEY_PORT = "port";
    private static String KEY_TROJAN_NAME = "trojan";

    private static int DEFAULT_PORT = 1080;
    private static int DEFAULT_TROJAN_PORT = 1081;

    private String filename;
    private FileInputStream fileInputStream;
    Map<String, Object> data;
    Yaml yaml;

    public ClashConfig(String filename) {
        try {
            this.filename = filename;
            fileInputStream = new FileInputStream(filename);
            yaml = new Yaml();
            data = (Map<String, Object>) yaml.load(fileInputStream);
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public <T> void update(String key, T value) {
        update(data, key, value);
    }

    public <T> void update(Map<String, Object> data, String key, T value) {
        data.put(key, value);
    }

    public void save(String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            file.createNewFile();
        }
        PrintWriter writer = new PrintWriter(filename);
        yaml.dump(data, writer);
        writer.close();
    }

    public void setPort(int port) {
        try {
            data.put(KEY_SOCKS_PORT, port);
            save(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setTrojanPort(int port) {
        List<Map<String, Object>> proxies = (List<Map<String, Object>>) data.get(KEY_PROXIES);
        try {
            for (int i = 0; i < proxies.size(); i++) {
                Map<String, Object> map = proxies.get(i);
                if (map.get(KEY_NAME).equals(KEY_TROJAN_NAME)) {
                    map.put(KEY_PORT, port);
                    proxies.set(i, map);
                    break;
                }
            }
            data.put(KEY_PROXIES, proxies);
            save(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getPort() {
        return (int) data.get(KEY_SOCKS_PORT);
    }

    public int getTrojanPort() {
        List<Map<String, Object>> proxies = (List<Map<String, Object>>) data.get(KEY_PROXIES);
        for (int i = 0; i < proxies.size(); i++) {
            Map<String, Object> map = proxies.get(i);
            if (map.get(KEY_NAME).equals(KEY_TROJAN_NAME)) {
                return (int) map.get(KEY_PORT);
            }
        }
        return DEFAULT_TROJAN_PORT;
    }

    public static void startClash(String path, int port, int proxy) {
        ClashStartOptions clashStartOptions = new ClashStartOptions();
        clashStartOptions.setHomeDir(path);
        clashStartOptions.setTrojanProxyServer("127.0.0.1:" + proxy);
        // Clash specific syntax for any address
        clashStartOptions.setSocksListener("*:" + port);
        clashStartOptions.setTrojanProxyServerUdpEnabled(true);
        Clash.start(clashStartOptions);
    }
}
