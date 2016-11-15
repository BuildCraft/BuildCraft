package buildcraft.transport.gui;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;

import buildcraft.api.core.EnumPipePart;

import buildcraft.lib.gui.GuiRectangle;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.gui.pos.IPositionedElement;
import buildcraft.lib.misc.data.IReference;
import buildcraft.transport.gate.StatementWrapper;

public abstract class ElementStatement<T extends StatementWrapper> extends ElementGuiSlot<T> {

    /** An array containing [offset][X|Y] */
    private static final int[][] OFFSET_HOVER = {
        // First 8
        { -1, -1 }, { 0, -1 }, { 1, -1 }, { 1, 0 }, { 1, 1 }, { 0, 1 }, { -1, 1 }, { -1, 0 },
        // Top row + going down
        { -2, -2 }, { -1, -2 }, { 0, -2 }, { 1, -2 }, { 2, -2 }, { 2, -1 }, { 2, 0 }, { 2, 1 },
        // Bottom row + going up
        { 2, 2 }, { 1, 2 }, { 0, 2 }, { -1, 2 }, { -2, 2 }, { -2, 1 }, { -2, 0 }, { -2, -1 }

    };

    private T[] possible = null;
    private IPositionedElement[] posPossible = null;

    public ElementStatement(GuiGate gui, IPositionedElement element, IReference<T> reference) {
        super(gui, element, reference);
    }

    public boolean hasParam(int param) {
        T statement = reference.get();
        if (statement == null) {
            return false;
        } else {
            return param < statement.maxParameters();
        }
    }

    @Override
    public void drawBackground(float partialTicks) {
        draw(gui, reference.get(), this);
    }

    @Override
    public void drawForeground(float partialTicks) {
        if (possible != null) {
            int add = 18 * (possible.length > 8 ? 3 : 1);
            int x = getX() - add - 4;
            int y = getY() - add - 4;
            int size = 8 + add + 18 * 2;
            GuiGate.SELECTION_HOVER.draw(x, y, size, size);
            for (int i = 0; i < possible.length; i++) {
                draw(gui, possible[i], posPossible[i]);
            }
            draw(gui, reference.get(), this);
        }
    }

    public static void draw(GuiGate gui, StatementWrapper statement, IGuiPosition element) {
        if (statement == null) {
            GuiGate.SLOT_COLOUR.drawAt(element);
            return;
        }
        EnumPipePart part = statement.sourcePart;
        int yOffset = (part.getIndex() + 1) % 7;
        GuiGate.SLOT_COLOUR.offset(0, yOffset * 18).drawAt(element);
        ElementGuiSlot.drawSprite(gui, statement, element);
    }

    protected abstract T[] getPossible();

    @Override
    public void onMouseClicked(int button) {
        if (contains(gui.mouse)) {
            if (GuiScreen.isShiftKeyDown()) {
                reference.set(null);
            } else if (gui.currentHover == null) {
                T value = reference.get();
                if (value != null) {
                    possible = getPossible();
                    if (possible != null) {
                        gui.currentHover = this;
                        posPossible = new IPositionedElement[possible.length];
                        for (int i = 0; i < possible.length; i++) {
                            if (i < OFFSET_HOVER.length) {
                                posPossible[i] = offset(OFFSET_HOVER[i][0] * 18, OFFSET_HOVER[i][1] * 18);
                            } else {
                                // Too many elements, they will go offscreen
                                posPossible[i] = new GuiRectangle(-200, -200, 18, 18);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onMouseReleased(int button) {
        gui.currentHover = null;
        if (possible != null) {
            for (int i = 0; i < possible.length; i++) {
                if (posPossible[i].contains(gui.mouse)) {
                    reference.set(possible[i]);
                    break;
                }
            }
        }

        possible = null;
        posPossible = null;
    }

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        super.addToolTips(tooltips);
        if (possible != null) {
            for (int i = 0; i < possible.length; i++) {
                if (possible[i] != null && posPossible[i].contains(gui.mouse)) {
                    tooltips.add(new ToolTip(possible[i].getDescription()));
                    break;
                }
            }
        }
    }
}
