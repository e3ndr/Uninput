package xyz.e3ndr.uninput.events;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonSerializationMethod;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.element.JsonString;
import lombok.AllArgsConstructor;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public abstract class UEvent {

    @JsonSerializationMethod("type")
    private JsonElement $serialize_type() {
        return new JsonString(this.getType().name());
    }

    public abstract UEventType getType();

    @Override
    public String toString() {
        return Rson.DEFAULT.toJsonString(this);
    }

    @AllArgsConstructor
    public static enum UEventType {
        // @formatter:off
        SPAWN             (USpawnEvent.class          ),
        MOUSE_MOVE        (UMouseMoveEvent.class      ),
        MOUSE_WHEEL       (UMouseWheelEvent.class     ),
        MOUSE_PRESS       (UMousePressEvent.class     ),
        MOUSE_RELEASE     (UMouseReleaseEvent.class   ),
        KEYBOARD_PRESS    (UKeyboardPressEvent.class  ),
        KEYBOARD_RELEASE  (UKeyboardReleaseEvent.class), 
        // @formatter:on
        ;

        private Class<? extends UEvent> eventClass;

        public static UEvent parseEvent(JsonObject eventJson) {
            String eventType = eventJson.getString("event_type");

            try {
                // 1) Lookup the event type
                // 2) Use RSON to deserialize to object using the eventClass.
                // 3) Profit!
                UEventType type = UEventType.valueOf(eventType);
                UEvent event = Rson.DEFAULT.fromJson(eventJson, type.eventClass);

                return event;
            } catch (IllegalArgumentException e) {
                // 1.1) Lookup failed, so we don't actually have that event.
                // 1.2) Return nothing.
                return null;
            } catch (Exception e) {
                // 2.1) *Something* failed, so we probably don't have that event structured
                // correctly.
                // 2.2) Return nothing.
                FastLogger.logStatic(LogLevel.SEVERE, "An error occured while converting an event of type %s", eventType);
                FastLogger.logException(e);
                FastLogger.logStatic(LogLevel.DEBUG, eventJson);
                return null;
            }
        }

    }

}
