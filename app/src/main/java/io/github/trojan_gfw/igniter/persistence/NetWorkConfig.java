package io.github.trojan_gfw.igniter.persistence;

import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Set;

import clash.Clash;
import io.github.trojan_gfw.igniter.IgniterApplication;
import io.github.trojan_gfw.igniter.JNIHelper;
import io.github.trojan_gfw.igniter.R;
import tun2socks.Tun2socks;
import tun2socks.Tun2socksStartOptions;

public class NetWorkConfig {
    public static final String LOCAL_HOST = "127.0.0.1";
    private static final int VPN_MTU = 1500;
    private static final String PRIVATE_VLAN4_CLIENT = "172.19.0.1";
    private static final String PRIVATE_VLAN6_CLIENT = "fdfe:dcba:9876::1";
    private static final String TUN2SOCKS5_SERVER_HOST = "127.0.0.1";
    private static final String FAKE_IP_RANGE = "198.18.0.1/16";

    private static final String[] DNS_SERVERS = {
            "8.8.8.8",
            "8.8.4.4",
            "1.1.1.1",
            "1.0.0.1"
    };

    private static final String[] IPV6_DNS_SERVERS = {
            "2001:4860:4860::8888",
            "2001:4860:4860::8844"
    };

    public static boolean isPortTaken(final String ip, final int port, final int timeout) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), timeout);
            socket.close();
            return true;
        }
        catch(ConnectException ce){
            ce.printStackTrace();
            return false;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static void tunnelProxy(int fd, int port, boolean enableIPV6, boolean enableClash) {
        Tun2socksStartOptions tun2socksStartOptions = new Tun2socksStartOptions();
        tun2socksStartOptions.setTunFd(fd);
        tun2socksStartOptions.setSocks5Server(TUN2SOCKS5_SERVER_HOST + ":" + port);
        tun2socksStartOptions.setEnableIPv6(enableIPV6);
        tun2socksStartOptions.setMTU(VPN_MTU);

        Tun2socks.setLoglevel("info");
        if (enableClash) {
            tun2socksStartOptions.setFakeIPRange(FAKE_IP_RANGE);
        } else {
            // Disable go-tun2socks fake ip
            tun2socksStartOptions.setFakeIPRange("");
        }
        Tun2socks.start(tun2socksStartOptions);
    }

    public static String startService(IgniterApplication app, int fd) {
        JNIHelper.trojan(app.storage.getTrojanConfigPath());

        boolean enableClash = app.trojanPreferences.enableClash;
        boolean enableIPV6 = app.trojanPreferences.enableIPV6;
        boolean enableLan= app.trojanPreferences.enableLan;
        long trojanPort = app.trojanConfig.getLocalPort();
        long clashSocksPort = 0;
        long tun2socksPort;
        if (enableClash) {
            clashSocksPort = app.clashConfig.getPort();
            ClashConfig.startClash(app.getFilesDir().toString(),
                    (int)clashSocksPort, (int)trojanPort,
                    enableLan);
            tun2socksPort = clashSocksPort;
        } else {
            tun2socksPort = trojanPort;
        }
        tunnelProxy(fd, (int)tun2socksPort, enableIPV6, enableClash);
        String str = String.format(app.getString(R.string.network_ports), trojanPort, tun2socksPort);
        if (enableClash) {
            str += String.format(app.getString(R.string.clash_port), clashSocksPort);
        }
        return str;
    }

    public static ParcelFileDescriptor establish(IgniterApplication app, VpnService.Builder b, String sessionName, Set<String> packages) {
        boolean enableClash = app.trojanPreferences.enableClash;
        boolean enableIPV6 = app.trojanPreferences.enableIPV6;
        for (String packageName : packages) {
            try {
                b.addDisallowedApplication(packageName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        b.setSession(sessionName);
        b.setMtu(VPN_MTU);
        b.addAddress(PRIVATE_VLAN4_CLIENT, 30);
        if (enableClash) {
            for (String route : app.getResources().getStringArray(R.array.bypass_private_route)) {
                String[] parts = route.split("/", 2);
                b.addRoute(parts[0], Integer.parseInt(parts[1]));
            }
            // fake ip range for go-tun2socks
            // should match clash configuration
            b.addRoute("198.18.0.0", 16);
        } else {
            b.addRoute("0.0.0.0", 0);
        }
        for (String server : DNS_SERVERS) {
            b.addDnsServer(server);
        }
        if (enableIPV6) {
            b.addAddress(PRIVATE_VLAN6_CLIENT, 126);
            b.addRoute("::", 0);

            for (String server : IPV6_DNS_SERVERS) {
                b.addDnsServer(server);
            }
        }
        return b.establish();
    }

    public static void stop(IgniterApplication app) {
        boolean enableClash = app.trojanPreferences.enableClash;
        JNIHelper.stop();
        if (enableClash) {
            Clash.stop();
        }
        Tun2socks.stop();
    }

    public static void setPort(IgniterApplication app, int port) {
        if (app.trojanPreferences.getEnableClash()) {
            app.clashConfig.setPort(port);
            app.clashConfig.setTrojanPort(port + 1);
            app.trojanConfig.setLocalPort(port + 1);
        } else {
            app.trojanConfig.setLocalPort(port);
        }
    }
}
