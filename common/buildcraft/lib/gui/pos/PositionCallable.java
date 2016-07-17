package buildcraft.lib.gui.pos;

import java.util.function.IntSupplier;

public class PositionCallable implements IGuiPosition {
    private final IntSupplier x, y;

    public PositionCallable(IntSupplier x, IntSupplier y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int getX() {
        return x.getAsInt();
    }

    @Override
    public int getY() {
        return y.getAsInt();
    }
}
