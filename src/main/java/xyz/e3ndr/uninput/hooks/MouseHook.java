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
    private Uninput uninput;

    private Listener listener = new Listener();

    public MouseHook(Uninput uninput) {
        this.uninput = uninput;

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

        @Override
        public void nativeMouseWheelMoved(NativeMouseWheelEvent nativeEvent) {
            if (uninput.isMouseOnThisMachinesScreen()) return;

            int wheelDelta = nativeEvent.getWheelRotation();

            uninput.selfEvent(new UMouseWheelEvent(wheelDelta));
        }

        @Override
        public void nativeMousePressed(NativeMouseEvent nativeEvent) {
            if (uninput.isMouseOnThisMachinesScreen()) return;

            int button = nativeEvent.getButton();

            uninput.selfEvent(new UMousePressEvent(button));
        }

        @Override
        public void nativeMouseReleased(NativeMouseEvent nativeEvent) {
            if (uninput.isMouseOnThisMachinesScreen()) return;

            int button = nativeEvent.getButton();

            uninput.selfEvent(new UMouseReleaseEvent(button));
        }

        @Override
        public void nativeMouseMoved(NativeMouseEvent nativeEvent) {
            if (uninput.isMouseOnThisMachinesScreen()) return;

            int x = nativeEvent.getX();
            int y = nativeEvent.getY();

            if ((x == Uninput.targetX) && (y == Uninput.targetY)) {
                return; // No movement.
            }

            int xDelta = nativeEvent.getX() - Uninput.targetX;
            int yDelta = nativeEvent.getY() - Uninput.targetY;

            uninput.selfEvent(new UMouseMoveEvent(xDelta, yDelta));
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
