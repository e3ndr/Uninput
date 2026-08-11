package xyz.e3ndr.uninput.events;

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

}
