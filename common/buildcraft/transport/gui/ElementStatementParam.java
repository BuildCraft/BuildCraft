package buildcraft.transport.gui;

import net.minecraft.item.ItemStack;

import buildcraft.api.statements.IStatementParameter;

import buildcraft.lib.gui.pos.IPositionedElement;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.data.IReference;

public class ElementStatementParam extends ElementGuiSlot<IStatementParameter> {
    public final int paramIndex;
    public final ElementStatement<?> parent;

    public ElementStatementParam(GuiGate gui, IPositionedElement element, IReference<IStatementParameter> reference, int paramIndex, ElementStatement<?> elemParent) {
        super(gui, element, reference);
        this.paramIndex = paramIndex;
        this.parent = elemParent;
    }

    @Override
    public void drawBackground(float partialTicks) {
        IStatementParameter param = reference.get();
        if (!parent.hasParam(paramIndex)) {
            GuiGate.ICON_SLOT_BLOCKED.drawAt(this);
        } else if (param == null) {
            GuiGate.ICON_SLOT_NOT_SET.drawAt(this);
        } else {
            GuiGate.SLOT_COLOUR.drawAt(this);
            super.drawBackground(partialTicks);
            ItemStack stack = param.getItemStack();
            if (StackUtil.isValid(stack)) {
                gui.drawItemStackAt(stack, getX() + 1, getY() + 1);
            }
        }
    }
}
