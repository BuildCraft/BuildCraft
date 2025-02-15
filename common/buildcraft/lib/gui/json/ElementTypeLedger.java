package buildcraft.lib.gui.json;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.config.GuiConfigManager;
import buildcraft.lib.gui.ledger.Ledger_Neptune;
import buildcraft.lib.gui.pos.IGuiPosition;
import net.minecraft.resources.ResourceLocation;

public class ElementTypeLedger extends ElementType {
    public static final String NAME = "buildcraftlib:ledger";
    public static final ElementTypeLedger INSTANCE = new ElementTypeLedger();

    private ElementTypeLedger() {
        super(NAME);
    }

    @Override
    public IGuiElement deserialize0(BuildCraftJsonGui gui, IGuiPosition parent, JsonGuiInfo info, JsonGuiElement json) {
        FunctionContext ctx = createContext(json);

        inheritProperty(json, "color", "colour");

        String side = json.properties.get("side");
        String title = json.properties.get("title");
        int colour = resolveEquationInt(json, "colour", ctx);

        boolean positive = "right".equalsIgnoreCase(side);

        Ledger_Neptune ledger = new Ledger_Neptune(gui, colour, positive);
        ledger.setTitle(title);

        addChildren(gui, ledger.positionLedgerIconStart, info, json, "closed", ledger.getClosedElements()::add);

        ledger.calculateMaxSize();
        ResourceLocation def = gui.jsonGuiDefinition;
//        def = new ResourceLocation(def.getResourceDomain(), def.getResourcePath().replace(".json", ""));
        def = new ResourceLocation(def.getNamespace(), def.getPath().replace(".json", ""));
        ledger.setOpenProperty(GuiConfigManager.getOrAddBoolean(def, json.name + ".is_open", false));
        return ledger;
    }
}
