package xyz.e3ndr.uninput.events;

import co.casterlabs.rakurai.json.Rson;

public abstract class UEvent {

    public abstract UEventType getType();

    @Override
    public String toString() {
        return Rson.DEFAULT.toJsonString(this);
    }

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
