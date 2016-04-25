package buildcraft.lib.gui;

public class GuiElementSimple<G extends GuiBC8<C>, C extends ContainerBC8> implements IGuiElement {
    public final G gui;
    private final IPositionedElement parent;
    private final GuiRectangle rectangle;

    public GuiElementSimple(G gui, IPositionedElement parent, GuiRectangle rectangle) {
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
