package buildcraft.lib.gui;

import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;

public class GuiElementSimple<G extends GuiBC8<?>> implements IGuiElement {
    public final G gui;
    private final IGuiArea element;

    public GuiElementSimple(G gui, IGuiPosition parent, GuiRectangle rectangle) {
        this(gui, rectangle.offset(parent));
    }

    public GuiElementSimple(G gui, IGuiArea element) {
        this.gui = gui;
        this.element = element;
    }

    @Override
    public int getX() {
        return element.getX();
    }

    @Override
    public int getY() {
        return element.getY();
    }

    @Override
    public int getWidth() {
        return element.getWidth();
    }

    @Override
    public int getHeight() {
        return element.getHeight();
    }
}
