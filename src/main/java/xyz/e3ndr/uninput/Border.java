package xyz.e3ndr.uninput;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Border {
    // @formatter:off
    TOP     (  0,  10, true ),
    BOTTOM  (  0, -10, true ),
    LEFT    ( 10,   0, false),
    RIGHT   (-10,   0, false);
    // @formatter:on

    private int xSafe;
    private int ySafe;
    private boolean isHorizontal;

}
