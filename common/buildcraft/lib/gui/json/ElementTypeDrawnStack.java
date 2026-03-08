package buildcraft.lib.gui.json;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.gui.GuiStack;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.gui.elem.GuiElementDrawable;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JSONUtils;

public class ElementTypeDrawnStack extends ElementType {
    public static final String NAME = "buildcraftlib:drawable/stack";
    public static final ElementTypeDrawnStack INSTANCE = new ElementTypeDrawnStack();

    private ElementTypeDrawnStack() {
        super(NAME);
    }

    @Override
    protected IGuiElement deserialize0(BuildCraftJsonGui gui, IGuiPosition parent, JsonGuiInfo info, JsonGuiElement json) {
        FunctionContext ctx = createContext(json);
        IGuiPosition pos = resolvePosition(json, "pos", parent, ctx);

        INodeBoolean visible = getEquationBool(json, "visible", ctx, true);
        boolean foreground = resolveEquationBool(json, "foreground", ctx, false);

//        Item item = JsonUtils.getItem(json.json, "id");
        Item item = JSONUtils.getAsItem(json.json, "id");

        // 1.18.2: to ensure no meta appears
        int meta = resolveEquationInt(json, "meta", ctx);
        if (meta != 0) {
            throw new RuntimeException("[lib.gui.json] Found stack with meta in " + json + " , but meta is not supported in this MC version!");
        }

//        ItemStack stack = new ItemStack(item, 1, meta);
        ItemStack stack = new ItemStack(item, 1);

        ISimpleDrawable icon = new GuiStack(stack);
        IGuiArea area = IGuiArea.create(pos, 16, 16);
        return new GuiElementDrawable(gui, area, icon, foreground, visible);
    }
}
