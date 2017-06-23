package buildcraft.lib.gui.statement;

import java.util.List;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IGuiSlot;

import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.IInteractionElement;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.json.GuiJson;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.misc.data.IReference;
import buildcraft.lib.statement.StatementContext;
import buildcraft.lib.statement.StatementContext.StatementGroup;

public class GuiElementStatementSource<S extends IGuiSlot> implements IInteractionElement {
    public final GuiJson<?> gui;
    public final IGuiPosition position;
    public final StatementContext<S> ctx;
    private final boolean left;
    private boolean isSelected = false;
    private S selected;

    public GuiElementStatementSource(GuiJson<?> gui, boolean left, StatementContext<S> ctx) {
        this.gui = gui;
        this.left = left;
        this.ctx = ctx;
        if (left) {
            position = gui.lowerLeftLedgerPos.offset(-getWidth(), 0);
            gui.lowerLeftLedgerPos = getPosition(1, 1);
        } else {
            position = gui.lowerRightLedgerPos;
            gui.lowerRightLedgerPos = getPosition(-1, 1);
        }
    }

    @Override
    public int getX() {
        return position.getX();
    }

    @Override
    public int getY() {
        return position.getY();
    }

    @Override
    public int getWidth() {
        return 4 * 18;
    }

    @Override
    public int getHeight() {
        int size = 0;
        for (StatementGroup<S> group : ctx.getAllPossible()) {
            size += group.getValues().size();
        }
        return size / 4 * 18 + 4;
    }

    private void iterateSlots(ISlotIter<S> iter) {
        int x = 0;
        int y = 0;
        for (StatementGroup<S> group : ctx.getAllPossible()) {
            int visited = 0;
            for (S slot : group.getValues()) {
                int px = getX() + x * 18;
                int py = getY() + y * 18;
                iter.iterate(slot, new GuiRectangle(px, py, 18, 18));
                visited++;
                x++;
                if (x > 3) {
                    x = 0;
                    y++;
                }
            }

            if (visited > 0) {
                y++;
            }
        }
    }

    interface ISlotIter<S extends IGuiSlot> {
        void iterate(S slot, GuiRectangle area);
    }

    @Override
    public void drawBackground(float partialTicks) {
        iterateSlots((s, area) -> {
            // ...oh. We need a way of drawing arbitrary slots from the API. Great :/
            drawAt(s, area.x, area.y);
        });
        if (selected != null) {
            int x = gui.mouse.getX();
            int y = gui.mouse.getY();
            drawAt(selected, x - 9, y - 9);
        }
    }

    private void drawAt(S slot, int x, int y) {
        ISprite sprite = slot.getSprite();
        if (sprite != null) {
            GuiIcon.drawAt(sprite, x + 1, y + 1, 16);
        }

    }

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        iterateSlots((slot, area) -> {
            if (area.contains(gui.mouse)) {
                tooltips.add(new ToolTip(slot.getDescription()));
            }
        });
    }

    @Override
    public void onMouseClicked(int button) {
        iterateSlots((slot, area) -> {
            if (area.contains(gui.mouse)) {
                isSelected = true;
                selected = slot;
            }
        });
    }

    @Override
    public void onMouseReleased(int button) {
        if (isSelected) {
            for (IGuiElement e : gui.getElementsAt(gui.mouse.getX(), gui.mouse.getY())) {
                if (e instanceof IReference<?>) {
                    IReference<?> elem = (IReference<?>) e;
                    elem.setIfCan(selected);
                }
            }
            isSelected = false;
            selected = null;
        }
    }
}
