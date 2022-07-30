package xyz.e3ndr.uninput;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;

public class Tray {
    private static final SystemTray tray = SystemTray.getSystemTray();
    private static TrayIcon trayIcon;

    static {
        Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
        // Image image =
        // Toolkit.getDefaultToolkit().createImage(getClass().getResource("icon.png"));

        trayIcon = new TrayIcon(image, "Uninput");
        trayIcon.setImageAutoSize(true);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public static void sendNotification(String title, String body, MessageType type) {
        trayIcon.displayMessage(title, body, type);
    }

}
