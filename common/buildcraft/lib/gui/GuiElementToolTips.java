package buildcraft.lib.gui;

import java.util.ArrayList;
import java.util.List;

import buildcraft.core.lib.gui.tooltips.ToolTip;
import buildcraft.lib.misc.GuiUtil;

public class GuiElementToolTips implements IGuiElement {
    private final GuiBC8<?> gui;

    public GuiElementToolTips(GuiBC8<?> gui) {
        this.gui = gui;
    }

    @Override
    public int getX() {
        return gui.mouse.getX();
    }

    @Override
    public int getY() {
        return gui.mouse.getY();
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public void drawForeground() {
        List<ToolTip> tooltips = new ArrayList<>();
        for (IGuiElement elem : gui.guiElements) {
            ToolTip tooltip = elem.getToolTip();
            if (tooltip != null) {
                tooltip.refresh();
                tooltips.add(tooltip);
            }
        }
        GuiUtil.drawVerticallyAppending(this, tooltips, this::drawTooltip);
    }

    private int drawTooltip(ToolTip tooltip, int x, int y) {
        return 4 + GuiUtil.drawHoveringText(tooltip, x, y, gui.width, gui.height, -1, gui.mc.fontRendererObj);
    }
}
