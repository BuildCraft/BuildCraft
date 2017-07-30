package buildcraft.lib.gui.pos;

import java.util.function.IntSupplier;

public class AreaCallable implements IGuiArea {
    public final IntSupplier x, y, width, height;

    public AreaCallable(IntSupplier x, IntSupplier y, IntSupplier width, IntSupplier height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

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
}
