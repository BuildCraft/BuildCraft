package buildcraft.lib.client.guide.parts;

import java.util.List;
import java.util.function.BooleanSupplier;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.font.IFontRenderer;

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
    public PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition current, int index) {
        if (isVisible()) {
            for (GuidePart part : parts) {
                current = part.renderIntoArea(x, y, width, height, current, index);
            }
        }
        return current;
    }

    @Override
    public PagePosition handleMouseClick(int x, int y, int width, int height, PagePosition current, int index,
        int mouseX, int mouseY) {
        if (isVisible()) {
            for (GuidePart part : parts) {
                current = part.handleMouseClick(x, y, width, height, current, index, mouseX, mouseY);
            }
        }
        return current;
    }
}
