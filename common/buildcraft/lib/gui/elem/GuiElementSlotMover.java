package buildcraft.lib.gui.elem;

import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.GuiElementSimple;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.inventory.Slot;

public class GuiElementSlotMover extends GuiElementSimple {

    public final INodeBoolean visible;
    public final Slot toMove;

    public GuiElementSlotMover(BuildCraftGui gui, IGuiPosition pos, INodeBoolean visible, Slot toMove) {
        super(gui, IGuiArea.create(pos, 18, 18));
        this.visible = visible;
        this.toMove = toMove;
    }

    @Override
    public void drawBackground(float partialTicks, PoseStack poseStack) {
        if (visible.evaluate()) {
            toMove.x = 1 + (int) Math.round(getX());
            toMove.y = 1 + (int) Math.round(getY());
        } else {
            toMove.x = -10000;
            toMove.y = -10000;
        }
    }
}
