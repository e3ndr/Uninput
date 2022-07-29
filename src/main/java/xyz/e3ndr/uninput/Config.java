package xyz.e3ndr.uninput;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Data;

@Data
@JsonClass(exposeAll = true)
public class Config {

    private Border border = Border.BOTTOM;

    private int port = 14189;

}
