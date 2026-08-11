package xyz.e3ndr.uninput.config;

import java.util.HashMap;
import java.util.Map;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;
import xyz.e3ndr.uninput.Uninput;

@JsonClass(exposeAll = true)
public class Config {
    public Map<String, BorderConfig> borders = new HashMap<>();
    public int port = 14189;

    public Config() {
        for (Bounds bounds : Uninput.box.bounds) {
            borders.put(bounds.name, null);
        }
    }

    @Getter
    @JsonClass(exposeAll = true)
    public static class BorderConfig {
        private Border border;
        private String targetDisplay;
    }

}
