package buildcraft.lib.gui.elem;

import java.util.List;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.pos.IGuiPosition;

public class GuiElementContainerResizing extends GuiElementContainer2 {

    public final IGuiPosition childRoot;
    private double minX, minY;
    private double maxX, maxY;

    public GuiElementContainerResizing(GuiBC8<?> gui, IGuiPosition childRoot) {
        super(gui);
        this.childRoot = childRoot;
        minX = maxX = childRoot.getX();
        minY = maxY = childRoot.getY();
    }

    @Override
    public IGuiPosition getChildElementPosition() {
        return childRoot;
    }

    @Override
    public double getX() {
        return childRoot.getX() + minX;
    }

    @Override
    public double getY() {
        return childRoot.getY() + minY;
    }

    @Override
    public double getWidth() {
        return maxX - minX;
    }

    @Override
    public double getHeight() {
        return maxY - minY;
    }

    @Override
    public void calculateSizes() {
        maxX = minX = maxY = minY = 0;
        double x0, x1, y0, y1;
        double x = childRoot.getX();
        double y = childRoot.getY();
        x0 = x1 = x;
        y0 = y1 = y;
        for (IGuiElement elem : getChildElements()) {
            x0 = Math.min(x0, elem.getX());
            y0 = Math.min(y0, elem.getY());
            x1 = Math.max(x1, elem.getEndX());
            y1 = Math.max(y1, elem.getEndY());
        }
        minX = x0 - x;
        maxX = x1 - x;
        minY = y0 - y;
        maxY = y1 - y;
    }

    @Override
    public void drawBackground(float partialTicks) {
        for (IGuiElement elem : getChildElements()) {
            elem.drawBackground(partialTicks);
        }
    }

    @Override
    public void drawForeground(float partialTicks) {
        for (IGuiElement elem : getChildElements()) {
            elem.drawForeground(partialTicks);
        }
    }

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        super.addToolTips(tooltips);
    }
}
