package buildcraft.lib.gui.elem;

import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.IContainingElement;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.misc.GuiUtil;
import buildcraft.lib.misc.GuiUtil.AutoGlScissor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public class ScrollWindow implements IContainingElement {

    public final BuildCraftGui gui;
    public final IGuiArea area;
    public final List<IGuiElement> innerElements = new ArrayList<>();
    public final IGuiPosition basePosition = new ScrollingElement();

    // Don't allow half-pixel scrolling: it breaks a lot of stuff.
    private int scrollPosition = 0;

    public ScrollWindow(BuildCraftGui gui, IGuiArea area) {
        this.gui = gui;
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
    public List<IGuiElement> getChildElements() {
        return innerElements;
    }

    @Override
    public void drawBackground(float partialTicks, GuiGraphics guiGraphics) {
        try (AutoGlScissor s = GuiUtil.scissor(area)) {
            for (IGuiElement element : innerElements) {
                element.drawBackground(partialTicks, guiGraphics);
            }
        }
    }

    @Override
    public void drawForeground(GuiGraphics guiGraphics, float partialTicks) {
        try (AutoGlScissor s = GuiUtil.scissor(area)) {
            for (IGuiElement element : innerElements) {
                element.drawForeground(guiGraphics, partialTicks);
            }
        }
    }

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        if (area.contains(gui.mouse)) {
            for (IGuiElement element : innerElements) {
                element.addToolTips(tooltips);
            }
        }
    }

    @Override
    public void onMouseClicked(int button) {
        if (area.contains(gui.mouse)) {
            IContainingElement.super.onMouseClicked(button);
        }
    }

    public IGuiPosition calculateNextPosition() {
        if (innerElements.isEmpty()) {
            return basePosition;
        } else {
            return innerElements.get(innerElements.size() - 1).getPosition(-1, 1);
        }
    }

    /** Assumes that all elements added are added starting at {@link #calculateNextPosition()}, after every one was
     * added. */
    public ScrollbarData calculateScrollbarData() {
        double totalHeight = 0;
        for (IGuiElement element : innerElements) {
            totalHeight += element.getHeight();
        }
        return new ScrollbarData(getHeight(), totalHeight, scrollPosition);
    }

    public int getScrollPosition() {
        return scrollPosition;
    }

    public class ScrollbarData {
        public final double shownHeight;
        public final double totalHeight;
        public final double position;

        public ScrollbarData(double shownHeight, double totalHeight, double position) {
            this.shownHeight = shownHeight;
            this.totalHeight = totalHeight;
            this.position = position;
        }

        public void setScrollPosition(double newPosition) {
            int rounded = (int) Math.round(newPosition);
            double maxDist = totalHeight - shownHeight;
            if (maxDist <= 0) {
                scrollPosition = 0;
                return;
            }
            if (rounded + 1 > maxDist) {
                rounded = 1 + (int) maxDist;
            } else if (rounded < 0) {
                rounded = 0;
            }
            scrollPosition = rounded;
        }
    }

    private class ScrollingElement implements IGuiPosition {
        @Override
        public double getX() {
            return area.getX();
        }

        @Override
        public double getY() {
            return area.getY() - scrollPosition;
        }
    }
}
