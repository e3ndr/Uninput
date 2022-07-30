package xyz.e3ndr.uninput.events;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonClass(exposeAll = true)
public class UKeyboardPressEvent extends UEvent {
    private int vk;

    @Override
    public UEventType getType() {
        return UEventType.KEYBOARD_PRESS;
    }

}
