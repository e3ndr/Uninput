package xyz.e3ndr.uninput.events;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.e3ndr.uninput.Border;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonClass(exposeAll = true)
public class USpawnEvent extends UEvent {
    private Border border;
    private double distance;
    private String display;

    @Override
    public UEventType getType() {
        return UEventType.SPAWN;
    }

}
