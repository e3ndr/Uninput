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
public class UMousePressEvent extends UEvent {
    private int button;

    @Override
    public UEventType getType() {
        return UEventType.MOUSE_PRESS;
    }

    @Override
    protected void serialize0(ByteWriter writer) throws IOException {
        writer.be.s32(this.button);
    }

    @Override
    protected void deserialize0(ByteReader reader) throws IOException {
        this.button = reader.be.s32();
    }

}
