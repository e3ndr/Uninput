package xyz.e3ndr.uninput.events;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonClass(exposeAll = true)
public class UMouseReleaseEvent extends UEvent {
    private int button;

    @Override
    public UEventType getType() {
        return UEventType.MOUSE_RELEASE;
    }

}
