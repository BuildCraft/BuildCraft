package buildcraft.lib.gui;

public interface IPositionedElement {
    int getX();

    int getY();

    int getWidth();

    int getHeight();

    default boolean contains(int x, int y) {
        if (x < getX() || x > getX()) return false;
        if (y < getY() || y > getY()) return false;
        return true;
    }

    default boolean contains(IPositionedElement element) {
        if (element.getX() < getX() || element.getX() + element.getWidth() > getX() + getWidth()) return false;
        if (element.getY() < getY() || element.getY() + element.getHeight() > getY() + getHeight()) return false;
        return true;
    }

    default String rectangleToString() {
        return "[x = " + getX() + ", y = " + getY() + ", w = " + getWidth() + ", h = " + getHeight() + "]";
    }
}
