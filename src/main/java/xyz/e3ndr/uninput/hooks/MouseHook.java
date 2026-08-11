package xyz.e3ndr.uninput.hooks;

import java.io.Closeable;
import java.io.IOException;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseWheelEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseWheelListener;

import xyz.e3ndr.uninput.Uninput;
import xyz.e3ndr.uninput.events.UMouseMoveEvent;
import xyz.e3ndr.uninput.events.UMousePressEvent;
import xyz.e3ndr.uninput.events.UMouseReleaseEvent;
import xyz.e3ndr.uninput.events.UMouseWheelEvent;

public class MouseHook implements Closeable {
    private Listener listener = new Listener();

    public static volatile int localMouseX = 0;
    public static volatile int localMouseY = 0;

    public void init() {
        GlobalScreen.addNativeMouseMotionListener(this.listener);
        GlobalScreen.addNativeMouseWheelListener(this.listener);
        GlobalScreen.addNativeMouseListener(this.listener);
    }

    @Override
    public void close() throws IOException {
        GlobalScreen.removeNativeMouseMotionListener(this.listener);
        GlobalScreen.removeNativeMouseWheelListener(this.listener);
        GlobalScreen.removeNativeMouseListener(this.listener);
    }

    private class Listener implements NativeMouseInputListener, NativeMouseWheelListener {

        // Send these immediately.

        @Override
        public void nativeMousePressed(NativeMouseEvent nativeEvent) {
            if (Uninput.isMouseOnThisMachinesScreen) {
                return;
            }

            int button = Uninput.mouseButtonSwap(nativeEvent.getButton());

            Uninput.selfEvent(new UMousePressEvent(button));
        }

        @Override
        public void nativeMouseReleased(NativeMouseEvent nativeEvent) {
            if (Uninput.isMouseOnThisMachinesScreen) {
                return;
            }

            int button = Uninput.mouseButtonSwap(nativeEvent.getButton());

            Uninput.selfEvent(new UMouseReleaseEvent(button));
        }

        @Override
        public void nativeMouseWheelMoved(NativeMouseWheelEvent nativeEvent) {
            if (Uninput.isMouseOnThisMachinesScreen) {
                return;
            }

            int wheelDelta = nativeEvent.getWheelRotation();

            Uninput.selfEvent(new UMouseWheelEvent(wheelDelta));
        }

        @Override
        public void nativeMouseMoved(NativeMouseEvent nativeEvent) {
            int absX = nativeEvent.getX();
            int absY = nativeEvent.getY();

            localMouseX = absX;
            localMouseY = absY;

            if (Uninput.isMouseOnThisMachinesScreen) {
                return;
            }

            int deltaX = absX - Uninput.centerX;
            int deltaY = absY - Uninput.centerY;

            if (deltaX == 0 && deltaY == 0) {
                return; // No movement
            }

            Uninput.robot.mouseMove(Uninput.centerX, Uninput.centerY);
            Uninput.selfEvent(new UMouseMoveEvent(deltaX, deltaY));
        }

        // Unused

        @Override
        public void nativeMouseClicked(NativeMouseEvent nativeEvent) {}

        @Override
        public void nativeMouseDragged(NativeMouseEvent nativeEvent) {
            this.nativeMouseMoved(nativeEvent);
        }

    }

}
