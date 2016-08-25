package buildcraft.lib.gui;

import buildcraft.lib.gui.pos.IGuiPosition;

public class GuiElementSimple<G extends GuiBC8<?>> implements IGuiElement {
    public final G gui;
    private final IGuiPosition parent;
    private final GuiRectangle rectangle;

    public GuiElementSimple(G gui, IGuiPosition parent, GuiRectangle rectangle) {
        this.gui = gui;
        this.parent = parent;
        this.rectangle = rectangle;
    }

    @Override
    public int getX() {
        return (parent == null ? 0 : parent.getX()) + rectangle.x;
    }

    @Override
    public int getY() {
        return (parent == null ? 0 : parent.getY()) + rectangle.y;
    }

    @Override
    public int getWidth() {
        return rectangle.width;
    }

    @Override
    public int getHeight() {
        return rectangle.height;
    }
}
