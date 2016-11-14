package buildcraft.transport.gui;

import buildcraft.api.core.EnumPipePart;

import buildcraft.lib.gui.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.gui.pos.IPositionedElement;
import buildcraft.transport.gate.StatementWrapper;

public abstract class ElementStatement<T extends StatementWrapper> extends ElementGuiSlot<T> {

    private T[] possible = null;
    private IPositionedElement[] posPossible = null;

    public ElementStatement(GuiGate gui, IGuiPosition parent, GuiRectangle rectangle, T[] values, int index) {
        super(gui, parent, rectangle, values, index);
    }

    public boolean hasParam(int param) {
        T statement = values[index];
        if (statement == null) {
            return false;
        } else {
            return param < statement.maxParameters();
        }
    }

    @Override
    public void drawBackground(float partialTicks) {
        draw(gui, values[index], this);
        if (possible != null) {
            for (int i = 0; i < possible.length; i++) {
                draw(gui, possible[i], posPossible[i]);
            }
        }
    }

    public static void draw(GuiGate gui, StatementWrapper statement, IGuiPosition element) {
        if (statement == null) {
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
            T value = values[index];
            if (value != null) {
                possible = getPossible();
                if (possible != null) {
                    posPossible = new IPositionedElement[possible.length];
                    for (int i = 0; i < possible.length; i++) {
                        // TODO: use a good algorithm for positioning this
                        posPossible[i] = this.offset(0, -18 * (i + 1));
                    }
                }
            }
        }
    }

    @Override
    public void onMouseReleased(int button) {
        if (possible != null) {
            for (int i = 0; i < possible.length; i++) {
                if (posPossible[i].contains(gui.mouse)) {
                    values[index] = possible[i];
                    break;
                }
            }
        }

        possible = null;
        posPossible = null;
    }
}
