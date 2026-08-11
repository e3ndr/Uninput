package xyz.e3ndr.uninput.hooks;

import xyz.e3ndr.uninput.Uninput;

public class Inputter {
    private static volatile boolean isActive;
    private static volatile int currX;
    private static volatile int currY;

    public static void start() {
        currX = Uninput.centerX;
        currY = Uninput.centerY;
        Uninput.robot.mouseMove(currX, currY);
        isActive = true;
    }

    public static void move(int xDelta, int yDelta) {
        if (!isActive) return;
        currX += xDelta;
        currY += yDelta;
        Uninput.robot.mouseMove(currX, currY);
    }

    public static void stop() {
        isActive = false;
    }

}
