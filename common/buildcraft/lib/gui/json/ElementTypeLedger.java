package buildcraft.lib.gui.json;

import buildcraft.api.core.BCLog;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.config.GuiConfigManager;
import buildcraft.lib.gui.ledger.Ledger_Neptune;
import buildcraft.lib.gui.pos.IGuiPosition;

public class ElementTypeLedger extends ElementType {
    public static final String NAME = "buildcraftlib:ledger";
    public static final ElementTypeLedger INSTANCE = new ElementTypeLedger();

    private ElementTypeLedger() {
        super(NAME);
    }

    @Override
    public IGuiElement deserialize0(GuiJson<?> gui, IGuiPosition parent, JsonGuiInfo info, JsonGuiElement json) {
        FunctionContext ctx = createContext(gui, json);

        inheritProperty(json, "color", "colour");

        String side = json.properties.get("side");
        String title = json.properties.get("title");
        int colour = resolveEquationInt(json, "colour", ctx);

        boolean positive = "right".equalsIgnoreCase(side);

        Ledger_Neptune ledger = new Ledger_Neptune(gui, colour, positive);
        ledger.setTitle(title);

        for (JsonGuiElement c : json.getChildren(info, "closed")) {
            String typeName = c.properties.get("type");
            ElementType type = JsonGuiTypeRegistry.TYPES.get(typeName);
            if (type == null) {
                BCLog.logger.warn("Unknown type " + typeName);
            } else {
                IGuiElement e = type.deserialize(gui, ledger.positionLedgerIconStart, info, c);
                gui.properties.put("custom." + json.name + "." + c.name, e);
                ledger.getClosedElements().add(e);
            }
        }

        ledger.calculateMaxSize();
        ledger.setOpenProperty(GuiConfigManager.getOrAddBoolean(gui.guiDefinition, json.name + ".is_open", false));
        return ledger;
    }
}
