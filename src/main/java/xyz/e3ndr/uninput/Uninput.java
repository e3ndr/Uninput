package xyz.e3ndr.uninput;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.TrayIcon.MessageType;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;

import lombok.Getter;
import lombok.Lombok;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.uninput.BoundingBox.TouchResult;
import xyz.e3ndr.uninput.Config.BorderConfig;
import xyz.e3ndr.uninput.events.UEvent;
import xyz.e3ndr.uninput.events.UKeyboardPressEvent;
import xyz.e3ndr.uninput.events.UKeyboardReleaseEvent;
import xyz.e3ndr.uninput.events.UMouseMoveEvent;
import xyz.e3ndr.uninput.events.UMousePressEvent;
import xyz.e3ndr.uninput.events.UMouseReleaseEvent;
import xyz.e3ndr.uninput.events.UMouseWheelEvent;
import xyz.e3ndr.uninput.events.USpawnEvent;
import xyz.e3ndr.uninput.hooks.BoundsHook;
import xyz.e3ndr.uninput.hooks.MouseHook;

public class Uninput implements Closeable {
    public static final String hostname;
    public static final BoundingBox box;

    public static final int targetX;
    public static final int targetY;

    public static final Robot robot;

    private FastLogger logger = new FastLogger();

    private @Getter boolean isMouseOnThisMachinesScreen = true;
    private @Getter String externalTarget = null;

    private BoundsHook boundsHook;
    private MouseHook mouseHook;

    private NetworkTransport network;

    private CaptureWindow window = new CaptureWindow(this);

    @Getter
    private Config config;

    static {
        String hst = "?";
        try {
            hst = InetAddress.getLocalHost().getHostName();
            hst = hst.replace(".mshome.net", "");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        hostname = hst;

        box = new BoundingBox(null);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        targetX = screenSize.width / 2;
        targetY = screenSize.height / 2;

        try {
            robot = new Robot();
        } catch (Exception e) {
            throw Lombok.sneakyThrow(e);
        }
    }

    public Uninput(Config config) throws Exception {
        this.config = config;

        this.logger.debug("Full display area: %s", box.getFullSize());

        this.logger.info("Registering listeners.");
        GlobalScreen.registerNativeHook();

        this.boundsHook = new BoundsHook(this);
        this.mouseHook = new MouseHook(this);

        this.logger.info("This machine's hostname: %s", hostname);

        this.network = new NetworkTransport(this);

        this.logger.info("Done! Server is open and listening on port %d.", config.getPort());

        Tray.sendNotification("Uninput Started", String.format("Uninput has started listening on %s:%d", hostname, config.getPort()), MessageType.INFO);
    }

    public void selfEvent(UEvent event) {
        if (event instanceof UKeyboardPressEvent) {
            UKeyboardPressEvent e = (UKeyboardPressEvent) event;

            if (e.getVk() == KeyEvent.VK_END) {
                this.logger.info("User panicked! (Pressed VK_END)");
                this.restoreControl();
                return;
            } else if (e.getVk() == KeyEvent.VK_HOME) {
                this.logger.info("User super panicked! Killing process. (Pressed VK_HOME)");
                System.exit(1);
                return;
            }
        }

        this.logger.trace("Sent: %s", event);
        boolean result = this.network.send(this.externalTarget, event);

        if (!result) {
            Tray.sendNotification("Uninput Lost Connection", String.format("Lost connection to %s, stopping input.", this.externalTarget), MessageType.ERROR);
            this.logger.info("Lost connection to %s, stopping input.", this.externalTarget);
            this.restoreControl();
        }
    }

    public void remoteEvent(UEvent e) {
        switch (e.getType()) {
            case SPAWN: {
                USpawnEvent event = (USpawnEvent) e;

                if (!this.isMouseOnThisMachinesScreen) {
                    this.logger.info("Control given back by another machine.");
                    this.restoreControl();
                }

                Point point = box.getSpawnLocation(event.getDisplay(), event.getBorder(), event.getDistance());
                this.logger.info("Spawning cursor at %d,%d", point.x, point.y);

                Inputter.start(point);
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

    @Override
    public void close() throws IOException {
        try {
            this.boundsHook.close();
        } catch (Exception e) {
            this.logger.severe(e);
        }
        try {
            this.mouseHook.close();
        } catch (Exception e) {
            this.logger.severe(e);
        }

        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            throw new IOException(e);
        }
    }

    public void restoreControl() {
        this.logger.info("Switching control back to this machine and restoring cursor back to it's original position (+ 10px).");
        this.isMouseOnThisMachinesScreen = true;
        this.externalTarget = null;
        this.window.disable();
        Inputter.unlockMouse();
    }

    public void borderTouched(TouchResult result, BorderConfig borderConfig) {
        Border touched = borderConfig.getBorder();
        String target = borderConfig.getTargetDisplay().split("=")[0];
        String displayName = borderConfig.getTargetDisplay().split("=")[1];

        this.isMouseOnThisMachinesScreen = false;
        this.externalTarget = target;

        this.logger.info("Touched border %s! Switching control to %s.", touched, this.externalTarget);
        this.logger.debug("The mouse will have a distance of %.2f%%.", result.distance * 100);

        this.selfEvent(new USpawnEvent(touched, result.distance, displayName));

        this.window.enable();
        Inputter.lockMouse(touched);
    }

}
