package buildcraft.lib.gui.pos;

import javax.annotation.Nonnull;

public class PositionOffset implements IGuiPosition {
    @Nonnull
    public final IGuiPosition parent;

    public final int xOffset, yOffset;

    private PositionOffset(@Nonnull IGuiPosition parent, int xOffset, int yOffset) {
        this.parent = parent;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    public static IGuiPosition createOffset(IGuiPosition from, int x, int y) {
        if (from == null) {
            return new PositionAbsolute(x, y);
        } else if (from instanceof PositionOffset) {
            PositionOffset parent = (PositionOffset) from;
            int oX = x + parent.xOffset;
            int oY = y + parent.yOffset;
            return parent.parent.offset(oX, oY);
        } else {
            return new PositionOffset(from, x, y);
        }
    }

    @Override
    public int getX() {
        return parent.getX() + xOffset;
    }

    @Override
    public int getY() {
        return parent.getY() + yOffset;
    }

    @Override
    public IGuiPosition offset(int x, int y) {
        return new PositionOffset(parent, x + xOffset, y + yOffset);
    }

    @Override
    public IGuiPosition offset(IGuiPosition by) {
        if (by instanceof PositionOffset) {
            return offset(by.getX(), by.getY());
        } else {
            return parent.offset(by).offset(this);
        }
    }
}
