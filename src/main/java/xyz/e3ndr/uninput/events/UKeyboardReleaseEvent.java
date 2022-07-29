package xyz.e3ndr.uninput.events;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonClass(exposeAll = true)
public class UKeyboardReleaseEvent extends UEvent {
    private int vk;

    @Override
    public UEventType getType() {
        return UEventType.KEYBOARD_RELEASE;
    }

}
