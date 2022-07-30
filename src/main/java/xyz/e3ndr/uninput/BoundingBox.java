package xyz.e3ndr.uninput;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import kotlin.Pair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
public class BoundingBox {
    private @JsonField List<Bounds> bounds = new ArrayList<>();

    public BoundingBox(Void aVoid) { // Differentiate the constructor from the Rson one.
        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (GraphicsDevice device : environment.getScreenDevices()) {
            Rectangle bounds = device.getDefaultConfiguration().getBounds();
            this.bounds.add(new Bounds(bounds, device.getIDstring()));
        }
    }

    @Deprecated
    public BoundingBox() {} // For Rson.

    private List<Bounds> intersect(int x, int y) {
        List<Bounds> result = new ArrayList<>(this.bounds.size());

        for (Bounds bounds : this.bounds) {
            if (bounds.has(x, y)) {
                result.add(bounds);
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return Rson.DEFAULT.toJsonString(this);
    }

    public @Nullable TouchResult isTouchingBorder(int x, int y) {
        List<Bounds> intersection = this.intersect(x, y);

        if (intersection.size() != 1) {
            // Zero means it's offscreen.
            // More than 1 means it's a shared border between the rectangles.
            return null;
        }

        Bounds b = intersection.get(0); // Remember, only 1 item.
        Border touching = b.isTouchingBorder(x, y);

        if (touching == null) return null;

        String name = b.getName();
        double distance = 0;

        Pair<Double, Double> vec = b.normVector(x, y);
        if (touching.isHorizontal()) {
            distance = vec.component1();
        } else {
            distance = vec.component2();
        }

        return new TouchResult(touching, name, distance);
    }

    @AllArgsConstructor
    public static class TouchResult {
        public final Border touched;
        public final String displayName;
        public final double distance;
    }

}

@Getter
@ToString
@JsonClass(exposeAll = true)
class Bounds {
    private int minX;
    private int minY;
    private int maxX;
    private int maxY;
    private String name;

    public Bounds(Rectangle from, String name) {
        this.minX = from.x;
        this.minY = from.y;
        this.maxX = from.x + from.width;
        this.maxY = from.y + from.height;
        this.name = String.format("%s=%s", Uninput.hostname, name);
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

    public Pair<Double, Double> normVector(int x, int y) {
        double width = this.maxX - this.minX;
        double height = this.maxY - this.minY;

        int normX = x - this.minX;
        int normY = y - this.minY;

        double vecX = normX / width;
        double vecY = normY / height;

        return new Pair<>(vecX, vecY);
    }

}
