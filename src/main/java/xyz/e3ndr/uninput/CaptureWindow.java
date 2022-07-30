package xyz.e3ndr.uninput;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

import xyz.e3ndr.uninput.events.UKeyboardPressEvent;
import xyz.e3ndr.uninput.events.UKeyboardReleaseEvent;

public class CaptureWindow {
    private final Uninput uninput;

    private JFrame frame = new JFrame("Uninput");

    public CaptureWindow(Uninput uninput) {
        this.uninput = uninput;

        this.frame.setUndecorated(true);
        this.frame.setOpacity(.15f); // Need *some* opacity.
        this.frame.setBackground(Color.BLACK);
        this.frame.setBounds(Uninput.box.getFullSize());
        this.frame.setAlwaysOnTop(true);

        this.frame.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (uninput.isMouseOnThisMachinesScreen()) return;

                int vk = e.getKeyCode();

                uninput.selfEvent(new UKeyboardPressEvent(vk));
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (uninput.isMouseOnThisMachinesScreen()) return;

                int vk = e.getKeyCode();

                uninput.selfEvent(new UKeyboardReleaseEvent(vk));
            }

            @Override
            public void keyTyped(KeyEvent e) {} // Unused.
        });
    }

    public void enable() {
        this.frame.setVisible(true);
        this.frame.requestFocus();
    }

    public void disable() {
        this.frame.setVisible(false);
    }

}
