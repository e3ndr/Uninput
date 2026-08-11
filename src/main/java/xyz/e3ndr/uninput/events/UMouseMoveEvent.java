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
public class UMouseMoveEvent extends UEvent {
    private int xDelta;
    private int yDelta;

    @Override
    public UEventType getType() {
        return UEventType.MOUSE_MOVE;
    }

    @Override
    protected void serialize0(ByteWriter writer) throws IOException {
        writer.be.s32(this.xDelta);
        writer.be.s32(this.yDelta);
    }

    @Override
    protected void deserialize0(ByteReader reader) throws IOException {
        this.xDelta = reader.be.s32();
        this.yDelta = reader.be.s32();
    }

}
