package xyz.e3ndr.uninput.hooks;

import xyz.e3ndr.uninput.Uninput;

public class Inputter {
    private static volatile boolean isActive;

    public static void start(int startX, int startY) {
        Uninput.robot.mouseMove(startX, startY);
        isActive = true;
    }

    public static void move(int xDelta, int yDelta) {
        if (!isActive) {
            return;
        }

        int currX = MouseHook.localMouseX;
        int currY = MouseHook.localMouseY;

        currX += xDelta;
        currY += yDelta;
        Uninput.robot.mouseMove(currX, currY);
    }

    public static void stop() {
        isActive = false;
    }

}
