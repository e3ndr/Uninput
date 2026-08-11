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
public class UKeyboardReleaseEvent extends UEvent {
    private int vk;

    @Override
    public UEventType getType() {
        return UEventType.KEYBOARD_RELEASE;
    }

    @Override
    protected void serialize0(ByteWriter writer) throws IOException {
        writer.be.s32(this.vk);
    }

    @Override
    protected void deserialize0(ByteReader reader) throws IOException {
        this.vk = reader.be.s32();
    }

}
