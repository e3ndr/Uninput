package xyz.e3ndr.uninput;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonDeserializationMethod;
import co.casterlabs.rakurai.json.annotating.JsonExclude;
import co.casterlabs.rakurai.json.annotating.JsonSerializationMethod;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import co.casterlabs.rakurai.json.validation.JsonValidationException;
import lombok.Getter;

@Getter
@JsonClass(exposeAll = true)
public class Config {
    private @JsonExclude Map<String, BorderConfig> borders = new HashMap<>();
    private int port = 14189;

    public Config() {
        for (Bounds bounds : Uninput.box.getBounds()) {
            borders.put(bounds.getName(), null);
        }
    }

    @JsonSerializationMethod("borders")
    private JsonElement $serialize_borders() {
        return Rson.DEFAULT.toJson(this.borders);
    }

    @JsonDeserializationMethod("borders")
    private void $deserialize_borders(JsonElement e) throws JsonValidationException, JsonParseException {
        JsonObject obj = e.getAsObject();

        for (Entry<String, JsonElement> entry : obj.entrySet()) {
            if (!entry.getValue().isJsonObject()) return;

            String key = entry.getKey();
            BorderConfig value = Rson.DEFAULT.fromJson(entry.getValue(), BorderConfig.class);

            this.borders.put(key, value);
        }
    }

    @Getter
    @JsonClass(exposeAll = true)
    public static class BorderConfig {
        private Border border;
        private String targetDisplay;
    }

}
