package buildcraft.lib.gui.pos;

import java.util.function.IntSupplier;

public interface IGuiPosition {
    int getX();

    int getY();

    default IGuiPosition offset(IntSupplier x, IntSupplier y) {
        return offset(new PositionCallable(x, y));
    }

    default IGuiPosition offset(int x, int y) {
        return PositionOffset.createOffset(this, x, y);
    }

    default IGuiPosition offset(IGuiPosition by) {
        return new PositionAdded(this, by);
    }
}
