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
        registerType(ElementTypeText.NAME, ElementTypeText.INSTANCE);
        registerType(ElementTypeHelp.NAME, ElementTypeHelp.INSTANCE);
        registerType(ElementTypeSprite.NAME, ElementTypeSprite.INSTANCE);
        registerType(ElementTypeButton.NAME, ElementTypeButton.INSTANCE);
        registerType(ElementTypeLedger.NAME, ElementTypeLedger.INSTANCE);
        registerType(ElementTypeStatementSlot.NAME, ElementTypeStatementSlot.INSTANCE);
        registerType(ElementTypeStatementParam.NAME, ElementTypeStatementParam.INSTANCE);
        registerType(ElementTypeStatementSource.NAME, ElementTypeStatementSource.INSTANCE);
    }

    public static void registerType(String id, ElementType type) {
        TYPES.put(id, type);
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
