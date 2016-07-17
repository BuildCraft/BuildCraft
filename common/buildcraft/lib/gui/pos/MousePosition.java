package buildcraft.lib.gui.pos;

public final class MousePosition implements IGuiPosition {
    private int x = -10, y = -10;

    public void setMousePosition(int mouseX, int mouseY) {
        this.x = mouseX;
        this.y = mouseY;
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
    public String toString() {
        return "Mouse [" + x + "," + y + "]";
    }
}
