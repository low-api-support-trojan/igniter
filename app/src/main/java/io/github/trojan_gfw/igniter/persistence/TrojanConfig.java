package io.github.trojan_gfw.igniter.persistence;

import android.content.Context;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.text.TextUtils;

import org.json.JSONArray;
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
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.trojan_gfw.igniter.LogHelper;
import io.github.trojan_gfw.igniter.R;

public class TrojanConfig implements Parcelable {

    // Class Scoped Static Definitions

    // Tags
    public static final String SINGLE_CONFIG_TAG = "TrojanConfig";
    public static final String CONFIG_LIST_TAG = "TrojanConfigList";

    private static TrojanConfig instance;
    private static JSONObject defaultJSON;


    // Object Scoped members
    File filename;
    private String localAddr;
    private int localPort;
    private String remoteAddr;
    private int remotePort;
    private String password;
    private boolean verifyCert;
    private String caCertPath;
    private String cipherList;
    private String tls13CipherList;

    private JSONObject json;


    // Global Config
    public static void init(Context context) {
        Storage storage = Storage.getSharedInstance(context);
        try {
            Resources res = context.getResources();
            InputStream inputStream = res.openRawResource(R.raw.config);

            byte[] b = new byte[inputStream.available()];
            inputStream.read(b);
            defaultJSON = new JSONObject(new String(b));
        } catch (Exception e) {
            e.printStackTrace();
        }
        TrojanConfig trojanConfig = new TrojanConfig().setCaCertPath(storage.getCaCertPath());
        setInstance(trojanConfig);
    }

    public static TrojanConfig getInstance() {
        return instance;
    }

    public static void setInstance(TrojanConfig trojanConfig) {
        instance = trojanConfig;
    }


    // Local Config
    private void construct() {
        // defaults
        this.localAddr = "127.0.0.1";
        this.localPort = 1080;
        this.remotePort = 443;
        this.verifyCert = true;
        this.cipherList = "ECDHE-ECDSA-AES128-GCM-SHA256:"
                + "ECDHE-RSA-AES128-GCM-SHA256:"
                + "ECDHE-ECDSA-CHACHA20-POLY1305:"
                + "ECDHE-RSA-CHACHA20-POLY1305:"
                + "ECDHE-ECDSA-AES256-GCM-SHA384:"
                + "ECDHE-RSA-AES256-GCM-SHA384:"
                + "ECDHE-ECDSA-AES256-SHA:"
                + "ECDHE-ECDSA-AES128-SHA:"
                + "ECDHE-RSA-AES128-SHA:"
                + "ECDHE-RSA-AES256-SHA:"
                + "DHE-RSA-AES128-SHA:"
                + "DHE-RSA-AES256-SHA:"
                + "AES128-SHA:"
                + "AES256-SHA:"
                + "DES-CBC3-SHA";
        this.tls13CipherList = "TLS_AES_128_GCM_SHA256:"
                + "TLS_CHACHA20_POLY1305_SHA256:"
                + "TLS_AES_256_GCM_SHA384";
    }

    public TrojanConfig() {
        construct();
    }

    // Parcel processing

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(localAddr);
        dest.writeInt(localPort);
        dest.writeString(remoteAddr);
        dest.writeInt(remotePort);
        dest.writeString(password);
        dest.writeByte((byte) (verifyCert ? 1 : 0));
        dest.writeString(caCertPath);
        dest.writeString(cipherList);
        dest.writeString(tls13CipherList);
    }

    protected TrojanConfig readFromParcel(Parcel in) {
        localAddr = in.readString();
        localPort = in.readInt();
        remoteAddr = in.readString();
        remotePort = in.readInt();
        password = in.readString();
        verifyCert = in.readByte() != 0;
        caCertPath = in.readString();
        cipherList = in.readString();
        tls13CipherList = in.readString();
        return this;
    }

    public static final Creator<TrojanConfig> CREATOR = new Creator<TrojanConfig>() {
        @Override
        public TrojanConfig createFromParcel(Parcel in) {
            return new TrojanConfig().readFromParcel(in);
        }

        @Override
        public TrojanConfig[] newArray(int size) {
            return new TrojanConfig[size];
        }
    };

    // JSON Processing
    public JSONObject toJSON() throws JSONException {
        return new JSONObject()
                .put("local_addr", this.localAddr)
                .put("local_port", this.localPort)
                .put("remote_addr", this.remoteAddr)
                .put("remote_port", this.remotePort)
                .put("password", new JSONArray().put(password))
                .put("log_level", 2) // WARN
                .put("ssl", new JSONObject()
                        .put("verify", this.verifyCert)
                        .put("cert", this.caCertPath)
                        .put("cipher", this.cipherList)
                        .put("cipher_tls13", this.tls13CipherList)
                        .put("alpn", new JSONArray().put("h2").put("http/1.1")));
    }

    public String toJSONString() throws JSONException {
        return toJSON().toString();
    }

    public TrojanConfig fromJSON(JSONObject json) throws JSONException {
        this.json = json;
        this.setLocalAddr(json.getString("local_addr"))
                .setLocalPort(json.getInt("local_port"))
                .setRemoteAddr(json.getString("remote_addr"))
                .setRemotePort(json.getInt("remote_port"))
                .setPassword(json.getJSONArray("password").getString(0))
                .setVerifyCert(json.getJSONObject("ssl").getBoolean("verify"));
        return this;
    }

    public TrojanConfig fromJSONString(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        return this.fromJSON(jsonObject);
    }

    public void copyFrom(TrojanConfig that) {
        this
                .setLocalAddr(that.localAddr)
                .setLocalPort(that.localPort)
                .setRemoteAddr(that.remoteAddr)
                .setRemotePort(that.remotePort)
                .setPassword(that.password)
                .setVerifyCert(that.verifyCert)
                .setCaCertPath(that.caCertPath)
                .setCipherList(that.cipherList)
                .setTls13CipherList(that.tls13CipherList);

    }

    public boolean isValidRunningConfig() {
        return !TextUtils.isEmpty(this.caCertPath)
                && !TextUtils.isEmpty(this.remoteAddr)
                && !TextUtils.isEmpty(this.password);
    }

    public String getLocalAddr() {
        return localAddr;
    }

    public TrojanConfig setLocalAddr(String localAddr) {
        this.localAddr = localAddr;
        return this;
    }

    public int getLocalPort() {
        return localPort;
    }

    public TrojanConfig setLocalPort(int localPort) {
        this.localPort = localPort;
        return this;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public TrojanConfig setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
        return this;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public TrojanConfig setRemotePort(int remotePort) {
        this.remotePort = remotePort;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public TrojanConfig setPassword(String password) {
        this.password = password;
        return this;
    }

    public boolean getVerifyCert() {
        return verifyCert;
    }

    public TrojanConfig setVerifyCert(boolean verifyCert) {
        this.verifyCert = verifyCert;
        return this;
    }

    public String getCaCertPath() {
        return caCertPath;
    }

    public TrojanConfig setCaCertPath(String caCertPath) {
        this.caCertPath = caCertPath;
        return this;
    }

    public String getCipherList() {
        return cipherList;
    }

    public TrojanConfig setCipherList(String cipherList) {
        this.cipherList = cipherList;
        return this;
    }

    public String getTls13CipherList() {
        return tls13CipherList;
    }

    public TrojanConfig setTls13CipherList(String tls13CipherList) {
        this.tls13CipherList = tls13CipherList;
        return this;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof TrojanConfig)) {
            return false;
        }
        TrojanConfig that = (TrojanConfig) obj;
        return (paramEquals(remoteAddr, that.remoteAddr) && paramEquals(remotePort, that.remotePort)
                && paramEquals(localAddr, that.localAddr) && paramEquals(localPort, that.localPort))
                && paramEquals(password, that.password) && paramEquals(verifyCert, that.verifyCert)
                && paramEquals(caCertPath, that.caCertPath)
                && paramEquals(cipherList, that.cipherList) && paramEquals(tls13CipherList, that.tls13CipherList);
    }

    private static boolean paramEquals(Object a, Object b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }

    @Nullable
    public static TrojanConfig read(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            TrojanConfig trojanConfig = new TrojanConfig();
            trojanConfig.fromJSONString(sb.toString());
            return trojanConfig;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void write(TrojanConfig trojanConfig, String filename) {
        try {
            String config = trojanConfig.toJSONString();
            File file = new File(filename);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(config.getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void show(String filename, String tag) {
        try {
            File file = new File(filename);
            FileInputStream fis = new FileInputStream(file);
                byte[] content = new byte[(int) file.length()];
                fis.read(content);
                LogHelper.v(tag, new String(content));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> void update(String trojanConfigPath, String key, T v) {
        File file = new File(trojanConfigPath);
        if (file.exists()) {
            try {
                String str;
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] content = new byte[(int) file.length()];
                    fis.read(content);
                    str = new String(content);
                }
                JSONObject json = new JSONObject(str);
                json.put(key, v);
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(json.toString().getBytes());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static boolean writeList(List<TrojanConfig> configList, String filename) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (TrojanConfig config : configList) {
                JSONObject jsonObject = config.toJSON();
                jsonArray.put(jsonObject);
            }
            String configStr = jsonArray.toString();
            File file = new File(filename);
            if (file.exists()) {
                file.delete();
            }
            OutputStream fos = new FileOutputStream(file);
            fos.write(configStr.getBytes());
            fos.flush();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @NonNull
    public static List<TrojanConfig> readList(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            return Collections.emptyList();
        }
        try (InputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            String json = new String(data);
            JSONArray jsonArr = new JSONArray(json);
            int len = jsonArr.length();
            List<TrojanConfig> list = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                TrojanConfig tc = new TrojanConfig().fromJSON(jsonArr.getJSONObject(i));
                list.add(tc);
            }
            return list;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }


    public static String toURIString(TrojanConfig trojanConfig) {
        URI trojanUri;
        try {
            trojanUri = new URI("trojan",
                    trojanConfig.getPassword(),
                    trojanConfig.getRemoteAddr(),
                    trojanConfig.getRemotePort(),
                    null, null, null);
        } catch (java.net.URISyntaxException e) {
            e.printStackTrace();
            return null;
        }

        return trojanUri.toString();
    }

    public static TrojanConfig fromURIString(String URIString) {
        URI trojanUri;
        try {
            trojanUri = new URI(URIString);
        } catch (java.net.URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
        String scheme = trojanUri.getScheme();
        if (scheme == null) {
            return null;
        }
        if (!scheme.equals("trojan"))
            return null;
        String host = trojanUri.getHost();
        int port = trojanUri.getPort();
        String userInfo = trojanUri.getUserInfo();

        TrojanConfig retConfig = new TrojanConfig();
        retConfig.setRemoteAddr(host);
        retConfig.setRemotePort(port);
        retConfig.setPassword(userInfo);
        return retConfig;
    }

}
