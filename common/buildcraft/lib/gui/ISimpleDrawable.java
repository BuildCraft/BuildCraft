package buildcraft.lib.gui;

import buildcraft.lib.gui.pos.IGuiPosition;

@FunctionalInterface
public interface ISimpleDrawable {
    void drawAt(int x, int y);

    default void drawAt(IGuiPosition element) {
        drawAt(element.getX(), element.getY());
    }
}
