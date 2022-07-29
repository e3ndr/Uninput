package xyz.e3ndr.uninput;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Border {
    // @formatter:off
    TOP     (  0,  10),
    BOTTOM  (  0, -10),
    LEFT    ( 10,   0),
    RIGHT   (-10,   0);
    // @formatter:on

    private int xSafe;
    private int ySafe;

}
