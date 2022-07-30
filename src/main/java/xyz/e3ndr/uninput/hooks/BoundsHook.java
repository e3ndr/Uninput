package xyz.e3ndr.uninput.hooks;

import java.io.Closeable;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;

import xyz.e3ndr.uninput.BoundingBox.TouchResult;
import xyz.e3ndr.uninput.Config.BorderConfig;
import xyz.e3ndr.uninput.Uninput;

public class BoundsHook implements Closeable {
    private Uninput uninput;

    private Listener listener = new Listener();

    public BoundsHook(Uninput uninput) {
        this.uninput = uninput;

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

            TouchResult result = Uninput.box.isTouchingBorder(x, y);
            if (result == null) return;

            BorderConfig borderConfig = uninput.getConfig().getBorders().get(result.displayName);

            if (result.touched == borderConfig.getBorder()) {
                uninput.borderTouched(result, borderConfig);
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
