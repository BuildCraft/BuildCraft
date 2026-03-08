package buildcraft.lib.gui.statement;

import buildcraft.api.statements.IGuiSlot;
import buildcraft.lib.client.sprite.SpriteNineSliced;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.GuiElementSimple;
import buildcraft.lib.gui.IMenuElement;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.RenderUtil;
import buildcraft.lib.misc.data.IReference;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.Arrays;
import java.util.List;

public class GuiElementStatementVariant extends GuiElementSimple implements IMenuElement {
    public static final SpriteNineSliced SELECTION_HOVER = GuiElementStatement.SELECTION_HOVER;

    /** An array containing [offset][X,Y] */
    private static final int[][] OFFSET_HOVER = {
            // Centre
            { 0, 0 },
            // First 8
            { -1, -1 }, { 0, -1 }, { 1, -1 }, { 1, 0 }, { 1, 1 }, { 0, 1 }, { -1, 1 }, { -1, 0 },
            // Top row + going down
            { -2, -2 }, { -1, -2 }, { 0, -2 }, { 1, -2 }, { 2, -2 }, { 2, -1 }, { 2, 0 }, { 2, 1 },
            // Bottom row + going up
            { 2, 2 }, { 1, 2 }, { 0, 2 }, { -1, 2 }, { -2, 2 }, { -2, 1 }, { -2, 0 }, { -2, -1 } //
    };

    private final IReference<? extends IGuiSlot> ref;
    private final IGuiSlot[] possible;
    private final IGuiArea[] posPossible;

    public GuiElementStatementVariant(BuildCraftGui gui, IGuiArea element, IReference<? extends IGuiSlot> ref, IGuiSlot[] possible, IGuiArea[] posPossible) {
        super(gui, element);
        this.ref = ref;
        this.possible = possible;
        this.posPossible = posPossible;
    }

    public static GuiElementStatementVariant create(BuildCraftGui gui, IGuiArea parent, IReference<? extends IGuiSlot> ref, IGuiSlot[] possible) {
        int count = Math.min(OFFSET_HOVER.length, possible.length);
        possible = possible.length == count ? possible : Arrays.copyOf(possible, count);
        IGuiArea[] posPossible = new IGuiArea[count];
        IGuiArea base = new GuiRectangle(18, 18).offset(parent);
        for (int i = 0; i < count; i++) {
            posPossible[i] = base.offset(OFFSET_HOVER[i][0] * 18, OFFSET_HOVER[i][1] * 18);
        }
        int sub = 18 * (count > 9 ? 2 : 1);
        int add = 18 * (count > 9 ? 3 : 1);
        int offset = -sub - 4;
        int size = 8 + add + 18 * 2;
        IGuiArea area = new GuiRectangle(offset, offset, size, size).offset(parent);
        return new GuiElementStatementVariant(gui, area, ref, possible, posPossible);
    }

    interface ISlotIter {
        void iterate(IGuiArea area, IGuiSlot slot);
    }

    private void iteratePossible(ISlotIter iter) {
        for (int p = 0; p < possible.length; p++) {
            IGuiSlot slot = possible[p];
            if (slot != null) {
                iter.iterate(posPossible[p], slot);
            }
        }
    }

    // IGuiElement

    @Override
    public void drawBackground(float partialTicks, PoseStack poseStack) {
//        GlStateManager.pushMatrix();
        poseStack.pushPose();
        // Render above items in the players inventory
//        GlStateManager.translate(0, 0, 1000);
        poseStack.translate(0, 0, 1000);
//        GlStateManager.color(1, 1, 1);
        RenderUtil.color(1, 1, 1);
        SELECTION_HOVER.draw(this, poseStack);
        iteratePossible((pos, slot) ->
        {
            double x = pos.getX();
            double y = pos.getY();
            GuiElementStatementSource.drawGuiSlot(slot, poseStack, x, y);
        });
//        GlStateManager.popMatrix();
        poseStack.popPose();
    }

    @Override
    public void drawForeground(PoseStack poseStack, float partialTicks) {

    }

    // ITooltipElement

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        iteratePossible((pos, slot) ->
        {
            if (pos.contains(gui.mouse)) {
                tooltips.add(new ToolTip(slot.getTooltip()));
            }
        });
    }

    // IInteractionElement

    @Override
    public void onMouseReleased(int button) {
        gui.currentMenu = null;
        iteratePossible((pos, slot) ->
        {
            if (pos.contains(gui.mouse)) {
                ref.setIfCan(slot);
            }
        });
    }
}
