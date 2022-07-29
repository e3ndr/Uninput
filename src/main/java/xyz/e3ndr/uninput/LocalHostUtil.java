package xyz.e3ndr.uninput;

import java.net.InetAddress;

import lombok.SneakyThrows;

public class LocalHostUtil {

    @SneakyThrows
    public static String getMachine() {
        return InetAddress.getLocalHost().getHostName();
    }

}
