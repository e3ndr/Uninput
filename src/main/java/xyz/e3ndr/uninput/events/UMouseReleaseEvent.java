package xyz.e3ndr.uninput.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UMouseReleaseEvent extends UEvent {
    private int button;

    @Override
    public UEventType getType() {
        return UEventType.MOUSE_RELEASE;
    }

}
