package buildcraft.lib.gui.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/** Turns several json elements into some functional gui data. */
public class JsonGuiDeserialiser {
    public static final Map<String, ElementType> TYPES = new HashMap<>();

    static {
        TYPES.put(ElementTypeSprite.NAME, ElementTypeSprite.INSTANCE);
        TYPES.put(ElementTypeButton.NAME, ElementTypeButton.INSTANCE);
    }

    public static void main(String[] args) throws IOException {
        String loc = "/assets/buildcraftbuilders/gui/filler.json";
        InputStream is = JsonGuiDeserialiser.class.getResourceAsStream(loc);
        JsonObject obj;
        try (InputStreamReader isr = new InputStreamReader(is)) {
            obj = new Gson().fromJson(isr, JsonObject.class);
        }

        JsonGuiInfo info = new JsonGuiInfo(obj);
        info.printOut(System.out::println);
    }
}
