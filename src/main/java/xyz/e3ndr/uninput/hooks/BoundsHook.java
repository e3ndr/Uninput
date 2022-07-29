package xyz.e3ndr.uninput.hooks;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.Closeable;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;

import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;
import xyz.e3ndr.uninput.Border;
import xyz.e3ndr.uninput.BoundingBox;
import xyz.e3ndr.uninput.Uninput;

public class BoundsHook implements Closeable {
    private Uninput uninput;

    private BoundingBox box = new BoundingBox();
    private Border switchAt;

    private Listener listener = new Listener();

    public BoundsHook(Uninput uninput, Border switchAt) {
        this.uninput = uninput;
        this.switchAt = switchAt;

        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (GraphicsDevice device : environment.getScreenDevices()) {
            Rectangle bounds = device.getDefaultConfiguration().getBounds();
            this.box.add(bounds);
        }

        GlobalScreen.addNativeMouseMotionListener(this.listener);
    }

    @Override
    public void close() {
        GlobalScreen.removeNativeMouseMotionListener(this.listener);
    }

    private class Listener implements NativeMouseInputListener {

        @Override
        public void nativeMouseMoved(NativeMouseEvent nativeEvent) {
            int x = nativeEvent.getX();
            int y = nativeEvent.getY();

            Border border = box.isTouchingBorder(x, y);

            if (border == switchAt) {
                FastLogger.logStatic(LogLevel.DEBUG, "%s,%s", x, y);
                uninput.borderTouched(border);
            }
        }

        @Override
        public void nativeMouseDragged(NativeMouseEvent nativeEvent) {
            this.nativeMouseMoved(nativeEvent);
        }

        // Unused.

        @Override
        public void nativeMouseClicked(NativeMouseEvent nativeEvent) {}

        @Override
        public void nativeMousePressed(NativeMouseEvent nativeEvent) {}

        @Override
        public void nativeMouseReleased(NativeMouseEvent nativeEvent) {}

    }

}
