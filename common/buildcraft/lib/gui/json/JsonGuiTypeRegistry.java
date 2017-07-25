package buildcraft.lib.gui.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/** Turns several json elements into some functional gui data. */
public class JsonGuiTypeRegistry {
    public static final Map<String, ElementType> TYPES = new HashMap<>();

    static {
        registerType(ElementTypeText.INSTANCE);
        registerType(ElementTypeHelp.INSTANCE);
        registerType(ElementTypeSprite.INSTANCE);
        registerType(ElementTypeButton.INSTANCE);
        registerType(ElementTypeLedger.INSTANCE);
        registerType(ElementTypeToolTip.INSTANCE);
        registerType(ElementTypeStatementSlot.INSTANCE);
        registerType(ElementTypeStatementParam.INSTANCE);
        registerType(ElementTypeStatementSource.INSTANCE);
    }

    public static void registerType(ElementType type) {
        TYPES.put(type.name, type);
    }

    // Simple test
    public static void main(String[] args) throws IOException {
        String loc = "/assets/buildcraftbuilders/gui/filler.json";
        InputStream is = JsonGuiTypeRegistry.class.getResourceAsStream(loc);
        JsonObject obj;
        try (InputStreamReader isr = new InputStreamReader(is)) {
            obj = new Gson().fromJson(isr, JsonObject.class);
        }

        JsonGuiInfo info = new JsonGuiInfo(obj);
        info.printOut(System.out::println);
    }
}
