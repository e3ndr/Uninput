package xyz.e3ndr.uninput.events;

import java.io.IOException;
import java.util.function.Supplier;

import co.casterlabs.commons.io.bytes.reading.ByteReader;
import co.casterlabs.commons.io.bytes.writing.ByteWriter;
import lombok.AllArgsConstructor;

public abstract class UEvent {

    public abstract UEventType getType();

    protected abstract void serialize0(ByteWriter writer) throws IOException;

    protected abstract void deserialize0(ByteReader reader) throws IOException;

    public static void serialize(UEvent event, ByteWriter writer) throws IOException {
        writer.be.s32(event.getType().ordinal());
        event.serialize0(writer);
    }

    public static UEvent deserialize(ByteReader reader) throws IOException {
        int typeOrdinal = reader.be.s32();
        UEventType type = UEventType.values()[typeOrdinal];

        UEvent event = type.constructor.get();
        event.deserialize0(reader);
        return event;
    }

    @AllArgsConstructor
    public static enum UEventType {
        PING(UPingEvent::new),

        SPAWN(USpawnEvent::new),

        MOUSE_MOVE(UMouseMoveEvent::new),
        MOUSE_WHEEL(UMouseWheelEvent::new),
        MOUSE_PRESS(UMousePressEvent::new),
        MOUSE_RELEASE(UMouseReleaseEvent::new),

        KEYBOARD_PRESS(UKeyboardPressEvent::new),
        KEYBOARD_RELEASE(UKeyboardReleaseEvent::new);

        private final Supplier<UEvent> constructor;

    }

}
