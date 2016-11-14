package buildcraft.transport.gui;

import net.minecraft.item.ItemStack;

import buildcraft.api.statements.IStatementParameter;

import buildcraft.lib.gui.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.misc.StackUtil;

public class ElementStatementParam extends ElementGuiSlot<IStatementParameter> {
    public final ElementStatement<?> parent;

    public ElementStatementParam(GuiGate gui, IGuiPosition parent, GuiRectangle rectangle, ElementStatement<?> elemParent, IStatementParameter[] paramArray, int index) {
        super(gui, parent, rectangle, paramArray, index);
        this.parent = elemParent;
    }

    @Override
    public void drawBackground(float partialTicks) {
        IStatementParameter param = values[index];
        if (!parent.hasParam(index)) {
            GuiGate.ICON_SLOT_BLOCKED.drawAt(this);
        } else if (param == null) {
            GuiGate.ICON_SLOT_NOT_SET.drawAt(this);
        } else {
            super.drawBackground(partialTicks);
            ItemStack stack = param.getItemStack();
            if (StackUtil.isValid(stack)) {
                gui.drawItemStackAt(stack, getX() + 1, getY() + 1);
            }
        }
    }
}
