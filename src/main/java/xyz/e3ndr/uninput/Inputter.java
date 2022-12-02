package xyz.e3ndr.uninput;

import java.awt.MouseInfo;
import java.awt.Point;

import xyz.e3ndr.uninput.hooks.MouseHook;

public class Inputter {
    private static boolean isActive;

    private static Point startingPoint;
    private static Point lastGoodPoint;

    public static void lockMouse(Border border) {
        lastGoodPoint = MouseInfo.getPointerInfo().getLocation();

        // Add some safe distance to avoid re-triggering the lock.
        lastGoodPoint.x += border.getXSafe();
        lastGoodPoint.y += border.getYSafe();

        new Thread(() -> {
            while (lastGoodPoint != null) {
                MouseHook.lastX = Uninput.targetX;
                MouseHook.lastY = Uninput.targetY;
                Uninput.robot.mouseMove(Uninput.targetX, Uninput.targetY);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {}
            }
        }).start();
    }

    public static void unlockMouse() {
        int x = lastGoodPoint.x;
        int y = lastGoodPoint.y;
        lastGoodPoint = null;

        Uninput.robot.mouseMove(x, y);
    }

    public static void start(Point point) {
        startingPoint = MouseInfo.getPointerInfo().getLocation();
        isActive = true;
        Uninput.robot.mouseMove(point.x, point.y);
    }

    public static void move(int xDelta, int yDelta) {
        if (!isActive) return;
        Uninput.robot.mouseMove(startingPoint.x + xDelta, startingPoint.y + yDelta);
        startingPoint = MouseInfo.getPointerInfo().getLocation();
    }

    public static void stop() {
        isActive = false;
    }

}
