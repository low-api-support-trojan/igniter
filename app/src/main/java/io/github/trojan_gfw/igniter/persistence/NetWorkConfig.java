package io.github.trojan_gfw.igniter.persistence;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;

import freeport.Freeport;
import io.github.trojan_gfw.igniter.LogHelper;
import tun2socks.Tun2socks;
import tun2socks.Tun2socksStartOptions;

public class NetWorkConfig {
    public static final String LOCAL_HOST = "127.0.0.1";
    private static final int VPN_MTU = 1500;
    private static final String PRIVATE_VLAN4_CLIENT = "172.19.0.1";
    private static final String PRIVATE_VLAN6_CLIENT = "fdfe:dcba:9876::1";
    private static final String TUN2SOCKS5_SERVER_HOST = "127.0.0.1";
    private static final String FAKE_IP_RANGE = "198.18.0.1/16";

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
}
