package buildcraft.lib.gui;

public final class MousePosition implements IPositionedElement {
    private int x, y;

    void setMousePosition(int mouseX, int mouseY) {
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
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public String toString() {
        return "Mouse [" + x + "," + y + "]";
    }
}
