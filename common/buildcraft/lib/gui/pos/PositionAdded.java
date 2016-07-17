package buildcraft.lib.gui.pos;

public class PositionAdded implements IGuiPosition {
    private final IGuiPosition a, b;

    public PositionAdded(IGuiPosition a, IGuiPosition b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public int getX() {
        return a.getX() + b.getX();
    }

    @Override
    public int getY() {
        return a.getY() + b.getY();
    }
}
