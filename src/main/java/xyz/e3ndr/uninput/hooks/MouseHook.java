package xyz.e3ndr.uninput.hooks;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseWheelEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseWheelListener;

import co.casterlabs.commons.functional.tuples.Pair;
import lombok.SneakyThrows;
import xyz.e3ndr.uninput.Downsampler;
import xyz.e3ndr.uninput.Uninput;
import xyz.e3ndr.uninput.events.UMouseMoveEvent;
import xyz.e3ndr.uninput.events.UMousePressEvent;
import xyz.e3ndr.uninput.events.UMouseReleaseEvent;
import xyz.e3ndr.uninput.events.UMouseWheelEvent;

public class MouseHook implements Closeable {
    private static final long SAMPLE_RATE = 50/*hz*/;

    public static volatile int lastX = 0;
    public static volatile int lastY = 0;

    private Uninput uninput;

    private Listener listener = new Listener();

    private Thread sampleThread = new Thread(this::doSampleLoop);

    private Downsampler<Integer> wheelDs = new Downsampler<>();
    private Downsampler<Pair<Integer, Integer>> moveDs = new Downsampler<>();

    public MouseHook(Uninput uninput) {
        this.uninput = uninput;

        GlobalScreen.addNativeMouseMotionListener(this.listener);
        GlobalScreen.addNativeMouseWheelListener(this.listener);
        GlobalScreen.addNativeMouseListener(this.listener);
        this.sampleThread.start();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void close() throws IOException {
        GlobalScreen.removeNativeMouseMotionListener(this.listener);
        GlobalScreen.removeNativeMouseWheelListener(this.listener);
        GlobalScreen.removeNativeMouseListener(this.listener);
        this.sampleThread.stop(); // Safe, we don't have any resources to close or any locks.
    }

    @SneakyThrows
    private void doSampleLoop() {
        while (true) {
            // Process wheel samples by adding them up and sending out a single packet.
            this.wheelDs.empty((samples) -> {
                if (samples.isEmpty()) return;

                int wheelDeltaTotal = 0;
                for (Integer sample : samples) {
                    wheelDeltaTotal += sample;
                }

                this.uninput.selfEvent(new UMouseWheelEvent(wheelDeltaTotal));
            });

            // Do the same for mouse movement.
            this.moveDs.empty((samples) -> {
                if (samples.isEmpty()) return;

                int xDeltaTotal = 0;
                int yDeltaTotal = 0;
                for (Pair<Integer, Integer> sample : samples) {
                    xDeltaTotal += sample.a();
                    yDeltaTotal += sample.b();
                }

                this.uninput.selfEvent(new UMouseMoveEvent(xDeltaTotal, yDeltaTotal));
            });

            TimeUnit.MILLISECONDS.sleep(1000 / SAMPLE_RATE);
        }
    }

    private class Listener implements NativeMouseInputListener, NativeMouseWheelListener {

        // Send these immediately.

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

        // We want to downsample mouseWheel and mouseMove inputs.

        @Override
        public void nativeMouseWheelMoved(NativeMouseWheelEvent nativeEvent) {
            if (uninput.isMouseOnThisMachinesScreen()) return;

            int wheelDelta = nativeEvent.getWheelRotation();

            wheelDs.add(wheelDelta);
        }

        @Override
        public void nativeMouseMoved(NativeMouseEvent nativeEvent) {
            if (uninput.isMouseOnThisMachinesScreen()) return;

            int x = nativeEvent.getX();
            int y = nativeEvent.getY();

//            if ((x == Uninput.targetX) && (y == Uninput.targetY)) {
//                return; // No movement.
//            }

            int xDelta = nativeEvent.getX() - lastX;
            int yDelta = nativeEvent.getY() - lastY;

            lastX = x;
            lastY = y;

            moveDs.add(new Pair<>(xDelta, yDelta));
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
