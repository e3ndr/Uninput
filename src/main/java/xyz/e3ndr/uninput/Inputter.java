package xyz.e3ndr.uninput;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;

import lombok.Lombok;

public class Inputter {
    private static Robot robot;

    private static boolean isActive;

    private static Point startingPoint;
    private static Point lastGoodPoint;

    static {
        try {
            robot = new Robot();
        } catch (Exception e) {
            throw Lombok.sneakyThrow(e);
        }
    }

    public static void moveMouseOffScreen(Border border) {
        lastGoodPoint = MouseInfo.getPointerInfo().getLocation();

        // Add some safe distance to avoid re-triggering the lock.
        lastGoodPoint.x += border.getXSafe();
        lastGoodPoint.y += border.getYSafe();

        new Thread(() -> {
            while (lastGoodPoint != null) {
                robot.mouseMove(Uninput.targetX, Uninput.targetY);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {}
            }
        }).start();
    }

    public static void moveMouseBack() {
        int x = lastGoodPoint.x;
        int y = lastGoodPoint.y;
        lastGoodPoint = null;

        robot.mouseMove(x, y);
    }

    public static void start() {
        startingPoint = MouseInfo.getPointerInfo().getLocation();
        isActive = true;
    }

    public static void stop() {
        isActive = false;
    }

}
