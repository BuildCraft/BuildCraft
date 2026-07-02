package ct.buildcraft.lib.gui.pos;

import java.util.function.DoubleSupplier;

public class AreaCallable implements IGuiArea {
    public final DoubleSupplier x, y, width, height;

    public AreaCallable(DoubleSupplier x, DoubleSupplier y, DoubleSupplier width, DoubleSupplier height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public AreaCallable(DoubleSupplier width, DoubleSupplier height) {
        this(() -> 0, () -> 0, width, height);
    }

    @Override
    public double getX() {
        return x.getAsDouble();
    }

    @Override
    public double getY() {
        return y.getAsDouble();
    }

    @Override
    public double getWidth() {
        return width.getAsDouble();
    }

    @Override
    public double getHeight() {
        return height.getAsDouble();
    }
}
