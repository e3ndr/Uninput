package xyz.e3ndr.uninput.events;

public abstract class UEvent {

    public abstract UEventType getType();

    public static enum UEventType {
        SPAWN,

        MOUSE_MOVE,
        MOUSE_WHEEL,
        MOUSE_PRESS,
        MOUSE_RELEASE,

        KEYBOARD_PRESS,
        KEYBOARD_RELEASE;
    }

}
