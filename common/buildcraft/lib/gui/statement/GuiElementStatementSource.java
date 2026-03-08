package buildcraft.lib.gui.statement;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IGuiSlot;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.IInteractionElement;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.statement.StatementContext;
import buildcraft.lib.statement.StatementContext.StatementGroup;
import buildcraft.lib.statement.StatementWrapper;
import com.mojang.blaze3d.matrix.MatrixStack;

import javax.annotation.Nullable;
import java.util.List;

public class GuiElementStatementSource<S extends IGuiSlot> implements IInteractionElement {
    public final BuildCraftGui gui;
    public final IGuiPosition position;
    public final StatementContext<S> ctx;
    private final boolean left;

    public final GuiElementStatementDrag dragger;

    public GuiElementStatementSource(BuildCraftGui gui, boolean left, StatementContext<S> ctx) {
        this.gui = gui;
        this.left = left;
        this.ctx = ctx;
        if (left) {
            position = gui.lowerLeftLedgerPos.offset(() -> -getWidth(), 0);
            gui.lowerLeftLedgerPos = getPosition(1, 1);
        } else {
            position = gui.lowerRightLedgerPos;
            gui.lowerRightLedgerPos = getPosition(-1, 1);
        }
        GuiElementStatementDrag drag = null;
        for (IGuiElement element : gui.shownElements) {
            if (element instanceof GuiElementStatementDrag) {
                drag = (GuiElementStatementDrag) element;
                break;
            }
        }
        if (drag == null) {
            drag = new GuiElementStatementDrag(gui);
            gui.shownElements.add(drag);
        }
        dragger = drag;
    }

    @Override
    public double getX() {
        return position.getX();
    }

    @Override
    public double getY() {
        return position.getY();
    }

    @Override
    public double getWidth() {
        int width = 0;
        for (StatementGroup<S> group : ctx.getAllPossible()) {
            int count = group.getValues().size();
            width = Math.max(width, count);
        }
        return Math.min(4, width) * 18;
    }

    @Override
    public double getHeight() {
        int height = 0;
        for (StatementGroup<S> group : ctx.getAllPossible()) {
            int count = group.getValues().size();
            height += (count + 3) / 4;
        }
        return height * 18 + 4;
    }

    private void iterateSlots(ISlotIter<S> iter) {
        int dx = left ? -1 : 1;
        int sx = left ? 3 : 0;
        int ex = sx + dx * 4;
        int x = sx;
        int y = 0;
        for (StatementGroup<S> group : ctx.getAllPossible()) {
            int visited = 0;
            for (S slot : group.getValues()) {
                double px = getX() + x * 18;
                double py = getY() + y * 18;
                iter.iterate(slot, new GuiRectangle(px, py, 18, 18));
                visited++;
                x += dx;
                if (x == ex) {
                    x = sx;
                    y++;
                }
            }

            if (visited > 0 && x != sx) {
                x = sx;
                y++;
            }
        }
    }

    interface ISlotIter<S extends IGuiSlot> {
        void iterate(@Nullable S slot, GuiRectangle area);
    }

    @Override
    public void drawBackground(float partialTicks, MatrixStack poseStack) {
        iterateSlots((s, area) ->
        {
            // ...oh. We need a way of drawing arbitrary slots from the API. Great :/
            drawAt(s, poseStack, area.x, area.y);
        });
    }

    private void drawAt(@Nullable S slot, MatrixStack poseStack, double x, double y) {
        drawGuiSlot(slot, poseStack, x, y);
    }

    public static void drawGuiSlot(@Nullable IGuiSlot guiSlot, MatrixStack poseStack, double x, double y) {
        if (guiSlot instanceof IStatementParameter) {
            ParameterRenderer.draw((IStatementParameter) guiSlot, poseStack, x, y);
            return;
        }
        GuiIcon background = GuiElementStatement.SLOT_COLOUR;
        if (guiSlot instanceof StatementWrapper) {
            EnumPipePart part = ((StatementWrapper) guiSlot).sourcePart;
            if (part != EnumPipePart.CENTER) {
                background = background.offset(0, (1 + part.getIndex()) * 18);
            }
        }
        background.drawAt(poseStack, x, y);
        if (guiSlot != null) {
            ISprite sprite = guiSlot.getSprite();
            if (sprite != null) {
                GuiIcon.drawAt(sprite, poseStack, x + 1, y + 1, 16);
            }
        }
    }

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        iterateSlots((slot, area) ->
        {
            if (slot == null) return;
            if (area.contains(gui.mouse)) {
                tooltips.add(new ToolTip(slot.getTooltip()));
            }
        });
    }

    @Override
    public void onMouseClicked(int button) {
        if (button == 0) {
            iterateSlots((slot, area) ->
            {
                if (area.contains(gui.mouse)) {
                    dragger.startDragging(slot);
                }
            });
        }
    }
}
