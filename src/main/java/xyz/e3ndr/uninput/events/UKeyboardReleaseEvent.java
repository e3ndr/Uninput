package xyz.e3ndr.uninput.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UKeyboardReleaseEvent extends UEvent {
    private int vk;

    @Override
    public UEventType getType() {
        return UEventType.KEYBOARD_RELEASE;
    }

}
