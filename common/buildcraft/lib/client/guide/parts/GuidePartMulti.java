package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.font.IFontRenderer;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;
import java.util.function.BooleanSupplier;

@Deprecated
public class GuidePartMulti extends GuidePart {

    public final List<GuidePart> parts;
    public final BooleanSupplier visibleFuncion;

    public GuidePartMulti(GuiGuide gui, List<GuidePart> subParts, BooleanSupplier isVisible) {
        super(gui);
        this.parts = subParts;
        this.visibleFuncion = isVisible;
    }

    @Override
    public void setFontRenderer(IFontRenderer fontRenderer) {
        super.setFontRenderer(fontRenderer);
        for (GuidePart part : parts) {
            part.setFontRenderer(fontRenderer);
        }
    }

    protected boolean isVisible() {
        return visibleFuncion.getAsBoolean();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        for (GuidePart part : parts) {
            part.updateScreen();
        }
    }

    @Override
    public PagePosition renderIntoArea(GuiGraphics guiGraphics, int x, int y, int width, int height, PagePosition current, int index) {
        if (isVisible()) {
            for (GuidePart part : parts) {
                current = part.renderIntoArea(guiGraphics, x, y, width, height, current, index);
            }
        }
        return current;
    }

    @Override
    public PagePosition handleMouseClick(GuiGraphics guiGraphics, int x, int y, int width, int height, PagePosition current, int index, double mouseX, double mouseY) {
        if (isVisible()) {
            for (GuidePart part : parts) {
                current = part.handleMouseClick(guiGraphics, x, y, width, height, current, index, mouseX, mouseY);
            }
        }
        return current;
    }
}
