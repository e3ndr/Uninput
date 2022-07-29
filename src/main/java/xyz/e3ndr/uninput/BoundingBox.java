package xyz.e3ndr.uninput;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.ToString;

@Getter
public class BoundingBox {
    private List<Bounds> bounds = new ArrayList<>();

    private List<Bounds> intersect(int x, int y) {
        List<Bounds> result = new ArrayList<>(this.bounds.size());

        for (Bounds bounds : this.bounds) {
            if (bounds.has(x, y)) {
                result.add(bounds);
            }
        }

        return result;
    }

    public void add(Rectangle rect) {
        this.bounds.add(new Bounds(rect));
    }

    @Override
    public String toString() {
        return this.bounds.toString();
    }

    public @Nullable Border isTouchingBorder(int x, int y) {
        List<Bounds> intersection = this.intersect(x, y);

        if (intersection.size() != 1) {
            // Zero means it's offscreen.
            // More than 1 means it's a shared border between the rectangles.
            return null;
        }

        Bounds b = intersection.get(0); // Remember, only 1 item.

        return b.isTouchingBorder(x, y);
    }

}

@Getter
@ToString
class Bounds {
    private int minX;
    private int minY;
    private int maxX;
    private int maxY;

    Bounds(Rectangle from) {
        this.minX = from.x;
        this.minY = from.y;
        this.maxX = from.x + from.width;
        this.maxY = from.y + from.height;
    }

    public @Nullable Border isTouchingBorder(int x, int y) {
        if (x == this.minX) {
            return Border.LEFT;
        }

        if (x == this.maxX) {
            return Border.RIGHT;
        }

        if (y == this.minY) {
            return Border.TOP;
        }

        if (y == this.maxY) {
            return Border.BOTTOM;
        }

        return null;
    }

    public boolean has(int x, int y) {
        return (x >= this.minX)
            && (x <= this.maxX)
            && (y >= this.minY)
            && (y <= this.maxY);
    }

}
