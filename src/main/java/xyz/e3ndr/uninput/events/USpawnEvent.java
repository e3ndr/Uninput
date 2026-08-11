package xyz.e3ndr.uninput.events;

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

}
