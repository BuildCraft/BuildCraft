package buildcraft.lib.gui.pos;

public class PositionAbsolute implements IGuiPosition {
    private final int x, y;

    public PositionAbsolute(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public IGuiPosition offset(int xOffset, int yOffset) {
        return new PositionAbsolute(xOffset + x, yOffset + y);
    }

    @Override
    public IGuiPosition offset(IGuiPosition by) {
        return by.offset(x, y);
    }
}
