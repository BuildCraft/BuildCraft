package ct.buildcraft.lib.gui.json;

import java.util.HashMap;
import java.util.Map;

/** Turns several json elements into some functional gui data. */
public class JsonGuiTypeRegistry {
    public static final Map<String, ElementType> TYPES = new HashMap<>();

    static {
        registerType(ElementTypeText.INSTANCE);
        registerType(ElementTypeHelp.INSTANCE);
        registerType(ElementTypeSlot.INSTANCE);
        registerType(ElementTypeSprite.INSTANCE);
        registerType(ElementTypeButton.INSTANCE);
        registerType(ElementTypeLedger.INSTANCE);
        registerType(ElementTypeToolTip.INSTANCE);
        registerType(ElementTypeContainer.INSTANCE);
        registerType(ElementTypeDrawnStack.INSTANCE);
        registerType(ElementTypeStatementSlot.INSTANCE);
        registerType(ElementTypeStatementParam.INSTANCE);
        registerType(ElementTypeStatementSource.INSTANCE);
    }

    public static void registerType(ElementType type) {
        TYPES.put(type.name, type);
    }


}
