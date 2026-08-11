package xyz.e3ndr.uninput.events;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import co.casterlabs.commons.io.bytes.reading.ByteReader;
import co.casterlabs.commons.io.bytes.writing.ByteWriter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.e3ndr.uninput.config.Border;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class USpawnEvent extends UEvent {
    private Border border;
    private double distance;
    private String display;

    @Override
    public UEventType getType() {
        return UEventType.SPAWN;
    }

    @Override
    protected void serialize0(ByteWriter writer) throws IOException {
        writer.be.s32(this.border.ordinal());
        writer.be.dbl(this.distance);

        byte[] displayBytes = this.display.getBytes(StandardCharsets.UTF_8);
        writer.be.s32(displayBytes.length);
        writer.write(displayBytes);
    }

    @Override
    protected void deserialize0(ByteReader reader) throws IOException {
        this.border = Border.values()[reader.be.s32()];
        this.distance = reader.be.dbl();

        int displayLength = reader.be.s32();
        byte[] displayBytes = new byte[displayLength];
        reader.read(displayBytes, 0, displayLength);
        this.display = new String(displayBytes, StandardCharsets.UTF_8);
    }

}
