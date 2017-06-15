package buildcraft.lib.gui.statement;

import java.util.Arrays;

import buildcraft.api.statements.IGuiSlot;

import buildcraft.lib.gui.IInteractionElement;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.misc.data.IReference;

public class GuiElementStatementVariant<S extends IGuiSlot> implements IInteractionElement {

    /** An array containing [offset][X,Y] */
    private static final int[][] OFFSET_HOVER = {
        // First 8
        { -1, -1 }, { 0, -1 }, { 1, -1 }, { 1, 0 }, { 1, 1 }, { 0, 1 }, { -1, 1 }, { -1, 0 },
        // Top row + going down
        { -2, -2 }, { -1, -2 }, { 0, -2 }, { 1, -2 }, { 2, -2 }, { 2, -1 }, { 2, 0 }, { 2, 1 },
        // Bottom row + going up
        { 2, 2 }, { 1, 2 }, { 0, 2 }, { -1, 2 }, { -2, 2 }, { -2, 1 }, { -2, 0 }, { -2, -1 } //
    };

    private final IGuiArea area;
    private final IReference<S> ref;
    private final S[] possible;
    private final IGuiArea[] posPossible;

    public GuiElementStatementVariant(IGuiPosition startOfMiddle, IReference<S> ref, S[] possible) {
        this.ref = ref;
        int count = Math.min(OFFSET_HOVER.length, possible.length);
        this.possible = possible.length == count ? possible : Arrays.copyOf(possible, count);
        posPossible = new IGuiArea[count];
        IGuiArea base = new GuiRectangle(18, 18).offset(startOfMiddle);
        for (int i = 0; i < count; i++) {
            posPossible[i] = base.offset(OFFSET_HOVER[i][0], OFFSET_HOVER[i][1]);
        }
        
    }

    @Override
    public int getX() {
        return area.getX();
    }

    @Override
    public int getY() {
        return area.getY();
    }

    @Override
    public int getWidth() {
        return area.getWidth();
    }

    @Override
    public int getHeight() {
        return area.getHeight();
    }
}
