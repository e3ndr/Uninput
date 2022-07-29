package xyz.e3ndr.uninput.events;

import co.casterlabs.rakurai.json.annotating.JsonSerializationMethod;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonString;

public abstract class UEvent {

    @JsonSerializationMethod("type")
    private JsonElement $serialize_type() {
        return new JsonString(this.getType().name());
    }

    public abstract UEventType getType();

    public static enum UEventType {
        MOUSE_MOVE,
        MOUSE_PRESS,
        MOUSE_RELEASE,
        MOUSE_WHEEL,

        KEYBOARD_PRESS,
        KEYBOARD_RELEASE,

    }

}
