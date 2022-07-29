package xyz.e3ndr.uninput;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.Closeable;
import java.io.IOException;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;

import co.casterlabs.rakurai.json.Rson;
import lombok.Getter;
import lombok.Setter;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.uninput.events.UEvent;
import xyz.e3ndr.uninput.events.UKeyboardPressEvent;
import xyz.e3ndr.uninput.hooks.BoundsHook;
import xyz.e3ndr.uninput.hooks.KeyboardHook;
import xyz.e3ndr.uninput.hooks.MouseHook;

public class Uninput implements Closeable {
    public static final int targetX;
    public static final int targetY;

    private FastLogger logger = new FastLogger();

    private String hostname = LocalHostUtil.getMachine();

    @Getter
    @Setter
    private boolean isMouseOnThisMachinesScreen = true;

    private BoundsHook boundsHook;
    private MouseHook mouseHook;
    private KeyboardHook keyboardHook;

    static {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        targetX = screenSize.width / 2;
        targetY = screenSize.height / 2;
    }

    public Uninput(Config config) throws Exception {
        this.logger.info("Registering listeners.");
        GlobalScreen.registerNativeHook();

        this.boundsHook = new BoundsHook(this, config.getBorder());
        this.mouseHook = new MouseHook(this);
        this.keyboardHook = new KeyboardHook(this);

        this.logger.info("This machine's hostname: %s", this.hostname);
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
        Inputter.moveMouseBack();
    }

    public void borderTouched(Border border) {
        this.logger.info("Touched border %s! Switching control.", border);
        this.isMouseOnThisMachinesScreen = false;

        Inputter.moveMouseOffScreen(border);
    }

}
