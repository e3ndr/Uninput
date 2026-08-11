package xyz.e3ndr.uninput.events;

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

}
