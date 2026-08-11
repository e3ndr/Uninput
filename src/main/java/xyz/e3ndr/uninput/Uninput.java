package xyz.e3ndr.uninput;

import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.TrayIcon.MessageType;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;

import lombok.Lombok;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.uninput.config.Border;
import xyz.e3ndr.uninput.config.BoundingBox;
import xyz.e3ndr.uninput.config.BoundingBox.TouchResult;
import xyz.e3ndr.uninput.config.Config;
import xyz.e3ndr.uninput.config.Config.BorderConfig;
import xyz.e3ndr.uninput.events.UEvent;
import xyz.e3ndr.uninput.events.UKeyboardPressEvent;
import xyz.e3ndr.uninput.events.UKeyboardReleaseEvent;
import xyz.e3ndr.uninput.events.UMouseMoveEvent;
import xyz.e3ndr.uninput.events.UMousePressEvent;
import xyz.e3ndr.uninput.events.UMouseReleaseEvent;
import xyz.e3ndr.uninput.events.UMouseWheelEvent;
import xyz.e3ndr.uninput.events.USpawnEvent;
import xyz.e3ndr.uninput.hooks.BoundsHook;
import xyz.e3ndr.uninput.hooks.CaptureWindow;
import xyz.e3ndr.uninput.hooks.Inputter;
import xyz.e3ndr.uninput.hooks.MouseHook;

@SuppressWarnings("resource")
public class Uninput {
    public static final String hostname;
    public static final BoundingBox box;

    public static final int centerX;
    public static final int centerY;

    public static final Robot robot;

    private static FastLogger logger = new FastLogger();

    public static boolean isMouseOnThisMachinesScreen = true;
    public static String externalTarget = null;

    private static BoundsHook boundsHook = new BoundsHook();
    private static MouseHook mouseHook = new MouseHook();
    private static NetworkTransport network = new NetworkTransport();
    private static CaptureWindow captureWindow = new CaptureWindow();

    public static Config config;

    static {
        String hst = "?";
        try {
            hst = InetAddress.getLocalHost().getHostName();
            hst = hst.replace(".mshome.net", "");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        hostname = hst;

        box = new BoundingBox();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        centerX = screenSize.width / 2;
        centerY = screenSize.height / 2;

        try {
            robot = new Robot();
        } catch (Exception e) {
            throw Lombok.sneakyThrow(e);
        }
    }

    public static void init(Config config) throws Exception {
        Uninput.config = config;

        logger.debug("Full display area: %s", box.getFullSize());

        logger.info("Registering listeners.");
        GlobalScreen.registerNativeHook();

        logger.info("This machine's hostname: %s", hostname);

        network.init();
        captureWindow.init();
        mouseHook.init();
        boundsHook.init();

        logger.info("Done! Server is open and listening on port %d.", config.port);

        Tray.sendNotification("Uninput Started", String.format("Uninput has started listening on %s:%d", hostname, config.port), MessageType.INFO);
    }

    public static void selfEvent(UEvent event) {
        if (event instanceof UKeyboardPressEvent) {
            UKeyboardPressEvent e = (UKeyboardPressEvent) event;

            if (e.getVk() == KeyEvent.VK_END) {
                logger.info("User panicked! (Pressed VK_END)");
                restoreControl();
                return;
            } else if (e.getVk() == KeyEvent.VK_HOME) {
                logger.info("User super panicked! Killing process. (Pressed VK_HOME)");
                System.exit(1);
                return;
            }
        }

        if (externalTarget == null) return;

        logger.trace("Sent: %s", event);
        boolean result = network.send(externalTarget, event);

        if (!result) {
            Tray.sendNotification("Uninput Lost Connection", String.format("Lost connection to %s, stopping input.", externalTarget), MessageType.ERROR);
            logger.info("Lost connection to %s, stopping input.", externalTarget);
            restoreControl();
        }
    }

    public static void remoteEvent(UEvent e) {
        switch (e.getType()) {
            case SPAWN: {
                USpawnEvent event = (USpawnEvent) e;

                if (!isMouseOnThisMachinesScreen) {
                    logger.info("Control given back by another machine.");
                    restoreControl();
                }

//                Point point = box.getSpawnLocation(event.getDisplay(), event.getBorder(), event.getDistance());
                logger.info("Spawning cursor at %d,%d", centerX, centerY);

                Inputter.start();
                return;
            }

            case KEYBOARD_PRESS: {
                UKeyboardPressEvent event = (UKeyboardPressEvent) e;

                robot.keyPress(event.getVk());
                return;
            }

            case KEYBOARD_RELEASE: {
                UKeyboardReleaseEvent event = (UKeyboardReleaseEvent) e;

                robot.keyRelease(event.getVk());
                return;
            }

            case MOUSE_MOVE: {
                UMouseMoveEvent event = (UMouseMoveEvent) e;

                Inputter.move(event.getXDelta(), event.getYDelta());
                return;
            }

            case MOUSE_PRESS: {
                UMousePressEvent event = (UMousePressEvent) e;
                int button = InputEvent.getMaskForButton(event.getButton());

                robot.mousePress(button);
                return;
            }

            case MOUSE_RELEASE: {
                UMouseReleaseEvent event = (UMouseReleaseEvent) e;
                int button = InputEvent.getMaskForButton(event.getButton());

                robot.mouseRelease(button);
                return;
            }

            case MOUSE_WHEEL: {
                UMouseWheelEvent event = (UMouseWheelEvent) e;

                robot.mouseWheel(event.getWheelDelta());
                return;
            }
        }
    }

    public static void close() throws IOException {
        try {
            boundsHook.close();
        } catch (Exception e) {
            logger.severe(e);
        }
        try {
            mouseHook.close();
        } catch (Exception e) {
            logger.severe(e);
        }

        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            throw new IOException(e);
        }
    }

    public static void restoreControl() {
        logger.info("Switching control back to this machine and restoring cursor back to it's original position (+ 10px).");
        isMouseOnThisMachinesScreen = true;
        externalTarget = null;
        captureWindow.disable();
//        Inputter.unlockMouse();
    }

    public static void borderTouched(TouchResult result, BorderConfig borderConfig) {
        Border touched = borderConfig.getBorder();
        String target = borderConfig.getTargetDisplay().split("=")[0];
        String displayName = borderConfig.getTargetDisplay().split("=")[1];

        if (!network.isConnected(target)) {
            logger.warn("Border %s was touched, but the target %s is not connected. Ignoring.", touched, target);
            return;
        }

        isMouseOnThisMachinesScreen = false;
        externalTarget = target;

        logger.info("Touched border %s! Switching control to %s.", touched, externalTarget);
        logger.debug("The mouse will have a distance of %.2f%%.", result.distance * 100);

        selfEvent(new USpawnEvent(touched, result.distance, displayName));

        captureWindow.enable();
//        Inputter.lockMouse(touched);
    }

}
