package ct.buildcraft.lib.gui.json;

import ct.buildcraft.lib.expression.FunctionContext;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import ct.buildcraft.lib.gui.GuiStack;
import ct.buildcraft.lib.gui.IGuiElement;
import ct.buildcraft.lib.gui.ISimpleDrawable;
import ct.buildcraft.lib.gui.elem.GuiElementDrawable;
import ct.buildcraft.lib.gui.pos.IGuiArea;
import ct.buildcraft.lib.gui.pos.IGuiPosition;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

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

        Item item = GsonHelper.getAsItem(json.json, "id");
        //int meta = resolveEquationInt(json, "meta", ctx);
        ItemStack stack = new ItemStack(item, 1/*, meta*/);

        ISimpleDrawable icon = new GuiStack(stack);
        IGuiArea area = IGuiArea.create(pos, 16, 16);
        return new GuiElementDrawable(gui, area, icon, foreground, visible);
    }
}
