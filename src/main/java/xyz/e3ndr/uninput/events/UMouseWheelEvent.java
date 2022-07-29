package xyz.e3ndr.uninput.events;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonClass(exposeAll = true)
public class UMouseWheelEvent extends UEvent {
    private int wheelDelta;

    @Override
    public UEventType getType() {
        return UEventType.MOUSE_WHEEL;
    }

}
