package buildcraft.lib.gui.pos;

public interface IGuiPosition {
    int getX();

    int getY();

    default IGuiPosition offset(int x, int y) {
        return PositionOffset.createOffset(this, x, y);
    }

    default IGuiPosition offset(IGuiPosition by) {
        return new PositionAdded(this, by);
    }
}
