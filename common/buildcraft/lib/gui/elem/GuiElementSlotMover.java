package buildcraft.lib.gui.elem;

import buildcraft.lib.gui.BuildCraftGui;
import net.minecraft.inventory.Slot;

import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.gui.GuiElementSimple;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;

public class GuiElementSlotMover extends GuiElementSimple {

    public final INodeBoolean visible;
    public final Slot toMove;

    public GuiElementSlotMover(BuildCraftGui gui, IGuiPosition pos, INodeBoolean visible, Slot toMove) {
        super(gui, IGuiArea.create(pos, 18, 18));
        this.visible = visible;
        this.toMove = toMove;
    }

    @Override
    public void drawBackground(float partialTicks) {
        if (visible.evaluate()) {
            toMove.xPos = 1 + (int) Math.round(getX());
            toMove.yPos = 1 + (int) Math.round(getY());
        } else {
            toMove.xPos = -10000;
            toMove.yPos = -10000;
        }
    }
}
