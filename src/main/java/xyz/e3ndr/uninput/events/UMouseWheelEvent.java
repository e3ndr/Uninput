package xyz.e3ndr.uninput.events;

import java.io.IOException;

import co.casterlabs.commons.io.bytes.reading.ByteReader;
import co.casterlabs.commons.io.bytes.writing.ByteWriter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UMouseWheelEvent extends UEvent {
    private int wheelDelta;

    @Override
    public UEventType getType() {
        return UEventType.MOUSE_WHEEL;
    }

    @Override
    protected void serialize0(ByteWriter writer) throws IOException {
        writer.be.s32(this.wheelDelta);
    }

    @Override
    protected void deserialize0(ByteReader reader) throws IOException {
        this.wheelDelta = reader.be.s32();
    }

}
