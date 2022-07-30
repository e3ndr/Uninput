package xyz.e3ndr.uninput.events;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonClass(exposeAll = true)
public class UMouseMoveEvent extends UEvent {
    private int xDelta;
    private int yDelta;

    @Override
    public UEventType getType() {
        return UEventType.MOUSE_MOVE;
    }

}
