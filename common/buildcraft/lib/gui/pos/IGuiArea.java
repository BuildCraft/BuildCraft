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
        return create(() -> getX() + x.getAsInt(), () -> getY() + y.getAsInt(), this::getWidth, this::getHeight);
    }

    @Override
    default IGuiArea offset(int x, int y) {
        return create(() -> getX() + x, () -> getY() + y, this::getWidth, this::getHeight);
    }

    default IGuiArea resize(int newWidth, int newHeight) {
        return create(this::getX, this::getY, () -> newWidth, () -> newHeight);
    }

    default IGuiArea expand(int by) {
        return expand(by, by);
    }

    default IGuiArea expand(int dX, int dY) {
        return create(() -> getX() - dX, () -> getY() - dY, () -> getWidth() + dX * 2, () -> getHeight() + dY * 2);
    }

    public static IGuiArea create(IntSupplier x, IntSupplier y, IntSupplier width, IntSupplier height) {
        return new IGuiArea() {
            @Override
            public int getX() {
                return x.getAsInt();
            }

            @Override
            public int getY() {
                return y.getAsInt();
            }

            @Override
            public int getWidth() {
                return width.getAsInt();
            }

            @Override
            public int getHeight() {
                return height.getAsInt();
            }
        };
    }
}
