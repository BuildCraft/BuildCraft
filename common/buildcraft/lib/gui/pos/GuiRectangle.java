package buildcraft.lib.gui.pos;

import net.minecraft.util.math.MathHelper;

/** An immutable {@link IGuiArea}. */
public final class GuiRectangle implements IGuiArea {
    /** A rectangle where all of the fields are set to 0. */
    public static final GuiRectangle ZERO = new GuiRectangle(0, 0, 0, 0);

    public final int x, y, width, height;

    public GuiRectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
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
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public GuiRectangle asImmutable() {
        return this;
    }

    @Override
    public String toString() {
        return "Rectangle [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
    }

    public GuiRectangle createProgress(double widthPercent, double heightPercent) {
        int nWidth = MathHelper.ceil(width * widthPercent);
        int nHeight = MathHelper.ceil(height * heightPercent);
        return new GuiRectangle(x, y, nWidth, nHeight);
    }
}
