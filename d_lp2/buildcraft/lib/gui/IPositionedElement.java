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

    default IPositionedElement offset(IPositionedElement by) {
        IPositionedElement containing = this;
        return new IPositionedElement() {
            @Override
            public int getX() {
                return by.getX() + containing.getX();
            }

            @Override
            public int getY() {
                return by.getY() + containing.getY();
            }

            @Override
            public int getWidth() {
                return containing.getWidth();
            }

            @Override
            public int getHeight() {
                return containing.getHeight();
            }
        };
    }
}
