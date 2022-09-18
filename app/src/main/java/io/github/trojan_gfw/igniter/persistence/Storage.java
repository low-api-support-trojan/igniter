package io.github.trojan_gfw.igniter.persistence;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Environment;

import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import io.github.trojan_gfw.igniter.IgniterApplication;
import io.github.trojan_gfw.igniter.LogHelper;
import io.github.trojan_gfw.igniter.R;

public class Storage {
    Context context;
    public static final int CACHE = 0;
    public static final int FILES = 1;
    public static final int EXTERNAL = 2;

    public File[] dirs = new File[3];

    public Storage(Context context) {
        this.context = context;
        dirs[CACHE] = context.getCacheDir();
        dirs[FILES] = context.getFilesDir();
        dirs[EXTERNAL] = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        dirs[EXTERNAL] = new File(dirs[EXTERNAL], context.getString(R.string.app_name));
    }

    public String getPath(int type, String filename) {
        switch (type) {
            case CACHE:
            case FILES:
            case EXTERNAL:
                return new File(dirs[type], filename).getPath();
            default:
                return null;
        }
    }

    public static void print(String filename, String tag) {
        String result = read(filename);
        LogHelper.v(tag, result);
    }

    public static String read(String filename) {
        try {
            File file = new File(filename);
            FileInputStream fis = new FileInputStream(file);
            int length = (int) file.length();
            byte[] content = new byte[length];
            int readBytes = fis.read(content);
            assert readBytes == length;
            String result = new String(content);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void write(String filename, byte[] bytes) {
        try {
            File file = new File(filename);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(bytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JSONObject readJSON(String filename) {
        try {
            String jsonStr = read(filename);
            return new JSONObject(jsonStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getCountryMmdbPath() {
        return getPath(FILES, context.getString(R.string.country_mmdb_config));
    }

    public String getClashConfigPath() {
        return getPath(FILES, context.getString(R.string.clash_config));
    }

    public String getTrojanConfigPath() {
        return getPath(FILES, context.getString(R.string.trojan_config));
    }

    public String getTrojanConfigListPath() {
        return getPath(FILES, context.getString(R.string.trojan_list_config));
    }

    public String getExemptedAppListPath() {
        return getPath(EXTERNAL, context.getString(R.string.exempted_app_list_config));
    }

    public String getCaCertPath() {
        return getPath(CACHE, context.getString(R.string.ca_cert_config));
    }

    public boolean isExternalWritable() {
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(context, permissions[i]) !=
                    PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void reset() {
        String[] paths = {
                getCaCertPath(),
                getCountryMmdbPath(),
                getClashConfigPath()
        };
        int[] ids = {
                R.raw.cacert,
                R.raw.country,
                R.raw.clash_config
        };

        for (int i = 0; i < ids.length; i++) {
            File file = new File(paths[i]);
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
                byte[] output = readRawBytes(ids[i]);
                write(paths[i], output);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public byte[] readRawBytes(int id) {

        try {
            Resources res = context.getResources();
            InputStream inputStream = res.openRawResource(id);
            byte[] b = new byte[inputStream.available()];
            inputStream.read(b);
            return b;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String readRawText(int id) {
        return new String(readRawBytes(id));
    }

    public JSONObject readRawJSON(int id) {

        try {
            String rawText = readRawText(id);
            return new JSONObject(rawText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteConfigs() {
        String[] paths = {
                getCaCertPath(),
                getCountryMmdbPath(),
                getClashConfigPath()
        };

        for(String filename: paths) {
            File file = new File(filename);
            file.delete();
        }
    }
}
