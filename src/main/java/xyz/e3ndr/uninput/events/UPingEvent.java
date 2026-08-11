package xyz.e3ndr.uninput.events;

import java.io.IOException;

import co.casterlabs.commons.io.bytes.reading.ByteReader;
import co.casterlabs.commons.io.bytes.writing.ByteWriter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UPingEvent extends UEvent {
    public static final UPingEvent INSTANCE = new UPingEvent();

    @Override
    public UEventType getType() {
        return UEventType.PING;
    }

    @Override
    protected void serialize0(ByteWriter writer) throws IOException {}

    @Override
    protected void deserialize0(ByteReader reader) throws IOException {}

}
