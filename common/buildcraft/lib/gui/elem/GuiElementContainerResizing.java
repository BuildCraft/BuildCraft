package buildcraft.lib.gui.elem;

import java.util.List;

import net.minecraft.client.gui.Gui;

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
        return minX;
    }

    @Override
    public double getY() {
        return minY;
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
        // FIXME: THis is broken!
        double x0 = 0, x1 = 0, y0 = 0, y1 = 0;
        x0 = x1 = minX = maxX = childRoot.getX();
        y0 = y1 = minY = maxY = childRoot.getY();
        for (IGuiElement elem : getChildElements()) {
            x0 = Math.min(x0, elem.getX());
            y0 = Math.min(y0, elem.getY());
            x1 = Math.max(x1, elem.getEndX());
            y1 = Math.max(y1, elem.getEndY());
        }
        minX = x0;
        maxX = x1;
        minY = y0;
        maxY = y1;
    }

    @Override
    public void drawBackground(float partialTicks) {
        Gui.drawRect((int) getX(), (int) getY(), (int) getEndX(), (int) getEndY(), hashCode() | 0xFF_00_00_00);
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
        if (contains(gui.mouse)) {
            // temp for testing
            tooltips.add(new ToolTip("Container"));
        } else {
            tooltips.add(new ToolTip((gui.mouse.getX() - getX()) + ", " + (gui.mouse.getY() - getY())));
        }
        super.addToolTips(tooltips);
    }
}
