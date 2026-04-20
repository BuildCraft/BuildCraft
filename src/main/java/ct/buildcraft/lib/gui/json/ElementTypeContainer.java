package ct.buildcraft.lib.gui.json;

import ct.buildcraft.lib.gui.elem.GuiElementContainerScissor;
import ct.buildcraft.lib.expression.FunctionContext;
import ct.buildcraft.lib.gui.IGuiElement;
import ct.buildcraft.lib.gui.elem.GuiElementContainerResizing;
import ct.buildcraft.lib.gui.pos.IGuiArea;
import ct.buildcraft.lib.gui.pos.IGuiPosition;

public class ElementTypeContainer extends ElementType {
    public static final String NAME = "buildcraftlib:container";
    public static final ElementTypeContainer INSTANCE = new ElementTypeContainer();

    private ElementTypeContainer() {
        super(NAME);
    }

    @Override
    protected IGuiElement deserialize0(BuildCraftJsonGui gui, IGuiPosition parent, JsonGuiInfo info,
        JsonGuiElement json) {
        FunctionContext ctx = createContext(json);
        boolean scissor = resolveEquationBool(json, "limit", ctx, false);
        if (scissor) {
            IGuiArea area = resolveArea(json, "area", parent, ctx);
            return new GuiElementContainerScissor(gui, area);
        } else {
            IGuiPosition pos = resolvePosition(json, "pos", parent, ctx);
            return new GuiElementContainerResizing(gui, pos);
        }
    }
}
