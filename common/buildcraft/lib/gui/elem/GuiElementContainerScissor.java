package buildcraft.lib.gui.elem;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.GuiUtil;
import buildcraft.lib.misc.GuiUtil.AutoGlScissor;

/** A type of {@link GuiElementContainer2} that restricts the visible size of elements contained within. */
public class GuiElementContainerScissor extends GuiElementContainer2 {

    public final IGuiArea area;

    public GuiElementContainerScissor(GuiBC8<?> gui, IGuiArea area) {
        super(gui);
        this.area = area;
    }

    @Override
    public double getX() {
        return area.getX();
    }

    @Override
    public double getY() {
        return area.getY();
    }

    @Override
    public double getWidth() {
        return area.getWidth();
    }

    @Override
    public double getHeight() {
        return area.getHeight();
    }

    @Override
    public void drawBackground(float partialTicks) {
        try (AutoGlScissor s = GuiUtil.scissor(area)) {
            for (IGuiElement elem : getChildElements()) {
                elem.drawBackground(partialTicks);
            }
        }
    }

    @Override
    public void drawForeground(float partialTicks) {
        try (AutoGlScissor s = GuiUtil.scissor(area)) {
            for (IGuiElement elem : getChildElements()) {
                elem.drawForeground(partialTicks);
            }
        }
    }
}
