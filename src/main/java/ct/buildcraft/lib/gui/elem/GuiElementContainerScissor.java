package ct.buildcraft.lib.gui.elem;

import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.lib.gui.IGuiElement;
import ct.buildcraft.lib.gui.json.BuildCraftJsonGui;
import ct.buildcraft.lib.gui.pos.IGuiArea;
import ct.buildcraft.lib.misc.GuiUtil;
import ct.buildcraft.lib.misc.GuiUtil.AutoGlScissor;

/** A type of {@link GuiElementContainer2} that restricts the visible size of elements contained within. */
public class GuiElementContainerScissor extends GuiElementContainer2 {

    public final IGuiArea area;

    public GuiElementContainerScissor(BuildCraftJsonGui gui, IGuiArea area) {
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
    public void drawBackground(PoseStack pose, float partialTicks) {
        try (AutoGlScissor s = GuiUtil.scissor(area)) {
            for (IGuiElement elem : getChildElements()) {
                elem.drawBackground(pose, partialTicks);
            }
        }
    }

    @Override
    public void drawForeground(PoseStack pose,float partialTicks) {
        try (AutoGlScissor s = GuiUtil.scissor(area)) {
            for (IGuiElement elem : getChildElements()) {
                elem.drawForeground(pose, partialTicks);
            }
        }
    }
}
