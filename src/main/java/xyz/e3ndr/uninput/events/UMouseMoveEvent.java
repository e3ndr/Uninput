package xyz.e3ndr.uninput.events;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
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
