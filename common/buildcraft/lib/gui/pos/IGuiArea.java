package buildcraft.lib.gui.pos;

import java.util.function.IntSupplier;

/** Defines an area somewhere on the screen. */
public interface IGuiArea extends IGuiPosition {
    int getWidth();

    int getHeight();

    default int getCenterX() {
        return getX() + getWidth() / 2;
    }

    default int getCenterY() {
        return getY() + getHeight() / 2;
    }

    default int getEndX() {
        return getX() + getWidth();
    }

    default int getEndY() {
        return getY() + getHeight();
    }

    default boolean contains(int x, int y) {
        if (x < getX() || x >= getEndX()) return false;
        if (y < getY() || y >= getEndY()) return false;
        return true;
    }

    default boolean contains(IGuiPosition position) {
        return contains(position.getX(), position.getY());
    }

    default boolean contains(IGuiArea element) {
        if (element.getX() < getX() || element.getEndX() >= getEndX()) return false;
        if (element.getY() < getY() || element.getEndY() >= getEndY()) return false;
        return true;
    }

    default String rectangleToString() {
        return "[x = " + getX() + ", y = " + getY() + ", w = " + getWidth() + ", h = " + getHeight() + "]";
    }

    @Override
    default IGuiArea offset(IGuiPosition by) {
        return offset(by::getX, by::getY);
    }

    @Override
    default IGuiArea offset(int x, IntSupplier y) {
        return offset(() -> x, y);
    }

    @Override
    default IGuiArea offset(IntSupplier x, int y) {
        return offset(x, () -> y);
    }

    @Override
    default IGuiArea offset(IntSupplier x, IntSupplier y) {
        IGuiArea containing = this;
        return new IGuiArea() {
            @Override
            public int getX() {
                return x.getAsInt() + containing.getX();
            }

            @Override
            public int getY() {
                return y.getAsInt() + containing.getY();
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

    @Override
    default IGuiArea offset(int x, int y) {
        IGuiArea containing = this;
        return new IGuiArea() {
            @Override
            public int getX() {
                return x + containing.getX();
            }

            @Override
            public int getY() {
                return y + containing.getY();
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

    default IGuiArea resize(int newWidth, int newHeight) {
        IGuiArea containing = this;
        return new IGuiArea() {
            @Override
            public int getX() {
                return containing.getX();
            }

            @Override
            public int getY() {
                return containing.getY();
            }

            @Override
            public int getWidth() {
                return newWidth;
            }

            @Override
            public int getHeight() {
                return newHeight;
            }
        };
    }

    default IGuiArea expand(int by) {
        return expand(by, by);
    }

    default IGuiArea expand(int dX, int dY) {
        IGuiArea containing = this;
        return new IGuiArea() {
            @Override
            public int getX() {
                return containing.getX() - dX;
            }

            @Override
            public int getY() {
                return containing.getY() - dY;
            }

            @Override
            public int getWidth() {
                return containing.getWidth() + dX * 2;
            }

            @Override
            public int getHeight() {
                return containing.getHeight() + dY * 2;
            }
        };
    }
}
