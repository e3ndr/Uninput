package xyz.e3ndr.uninput;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;

import co.casterlabs.rakurai.json.Rson;
import lombok.Getter;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.uninput.BoundingBox.TouchResult;
import xyz.e3ndr.uninput.Config.BorderConfig;
import xyz.e3ndr.uninput.events.UEvent;
import xyz.e3ndr.uninput.events.UKeyboardPressEvent;
import xyz.e3ndr.uninput.hooks.BoundsHook;
import xyz.e3ndr.uninput.hooks.KeyboardHook;
import xyz.e3ndr.uninput.hooks.MouseHook;

public class Uninput implements Closeable {
    public static final String hostname;
    public static final BoundingBox box;

    public static final int targetX;
    public static final int targetY;

    private FastLogger logger = new FastLogger();

    private @Getter boolean isMouseOnThisMachinesScreen = true;
    private @Getter String externalTarget = null;

    private BoundsHook boundsHook;
    private MouseHook mouseHook;
    private KeyboardHook keyboardHook;

    @Getter
    private Config config;

    static {
        String hst = "?";
        try {
            hst = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        hostname = hst;

        box = new BoundingBox(null);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        targetX = screenSize.width / 2;
        targetY = screenSize.height / 2;
    }

    public Uninput(Config config) throws Exception {
        this.config = config;

        this.logger.info("Registering listeners.");
        GlobalScreen.registerNativeHook();

        this.boundsHook = new BoundsHook(this);
        this.mouseHook = new MouseHook(this);
        this.keyboardHook = new KeyboardHook(this);

        this.logger.info("This machine's hostname: %s", hostname);
        this.logger.info("Done! Server is open and listening on port %d.", config.getPort());
    }

    public void selfEvent(UEvent event) {
        if (event instanceof UKeyboardPressEvent) {
            UKeyboardPressEvent e = (UKeyboardPressEvent) event;

            if (e.getVk() == KeyEvent.VK_END) {
                this.logger.info("User panicked! (Pressed VK_END)");
                this.restoreControl();
                return;
            }
        }

        this.logger.trace("Sent: %s", Rson.DEFAULT.toJson(event));
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
            this.keyboardHook.close();
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
        Inputter.moveMouseBack();
    }

    public void borderTouched(TouchResult result, BorderConfig borderConfig) {
        Border touched = borderConfig.getBorder();

        this.isMouseOnThisMachinesScreen = false;
        this.externalTarget = borderConfig.getTargetDisplay();

        this.logger.info("Touched border %s! Switching control to %s.", touched, this.externalTarget);
        this.logger.debug("The mouse will have a distance of %.2f%%.", result.distance * 100);

        Inputter.moveMouseOffScreen(touched);
    }

}
