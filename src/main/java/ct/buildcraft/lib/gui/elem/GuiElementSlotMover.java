package ct.buildcraft.lib.gui.elem;

import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import ct.buildcraft.lib.gui.BuildCraftGui;
import ct.buildcraft.lib.gui.GuiElementSimple;
import ct.buildcraft.lib.gui.pos.IGuiArea;
import ct.buildcraft.lib.gui.pos.IGuiPosition;
import net.minecraft.world.inventory.Slot;

/**Use accessTransforme*/@Deprecated //Useless
public class GuiElementSlotMover extends GuiElementSimple {

    public final INodeBoolean visible;
    public final Slot toMove;

    public GuiElementSlotMover(BuildCraftGui gui, IGuiPosition pos, INodeBoolean visible, Slot toMove) {
        super(gui, IGuiArea.create(pos, 18, 18));
        this.visible = visible;
        this.toMove = toMove;
    }

    @Override
    public void drawBackground(PoseStack pose, float partialTicks) {
    	//throw new UnsupportedOperationException("ct.buildcraft.lib.gui.elem.GuiElementSlotMover : Should not use this class");
/*        if (visible.evaluate()) {
            toMove.x = 1 + (int) Math.round(getX());
            toMove.y = 1 + (int) Math.round(getY());
        } else {
            toMove.x = -10000;
            toMove.y = -10000;
        }*/
    }
}
