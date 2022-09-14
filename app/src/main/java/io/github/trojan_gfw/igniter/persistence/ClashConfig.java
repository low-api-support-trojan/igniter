package io.github.trojan_gfw.igniter.persistence;

import org.json.JSONException;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class ClashConfig {
    public static  String TAG = "ClashConfig";
    // KEYS
    private static String KEY_SOCKS_PORT = "socks-port";
    private static String KEY_PROXIES = "proxies";
    private static String KEY_NAME = "name";
    private static String KEY_PORT = "port";
    private static String KEY_TROJAN_NAME = "trojan";

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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public <T> void update(String key, T value) {
        update(this.data, key, value);
    }

    public <T> void update(Map<String, Object> data, String key, T value) {
        data.put(key, value);
    }

    public void save() throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(filename);
        yaml.dump(data, writer);
    }

    public void updatePort(int port, int proxyPort) {
        data.put(KEY_SOCKS_PORT, port);
        List<String> proxies = (List<String>) data.get(KEY_PROXIES);
        try {
            for (int i = 0; i < proxies.toArray().length; i++) {
                String jsonStr = proxies.get(i);
                JSONObject json = new JSONObject(jsonStr);
                String name = json.getString(KEY_NAME);
                if (name.equals(KEY_TROJAN_NAME)) {
                    json.put(KEY_PORT, proxyPort);
                    jsonStr = json.toString();
                    proxies.set(i, jsonStr);
                }
            }
            data.put(KEY_PROXIES, proxies);
            save();
        } catch (FileNotFoundException | JSONException e) {
            e.printStackTrace();
        }
    }
}
