package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.contents.PageLink;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;

public class GuidePartLink extends GuidePart {

    public final PageLink link;

    public GuidePartLink(GuiGuide gui, PageLink link) {
        super(gui);
        this.link = link;
    }

    @Override
    public PagePosition renderIntoArea(GuiGraphics guiGraphics, int x, int y, int width, int height, PagePosition current, int index) {
        return renderLine(guiGraphics, current, link.text, x, y, width, height, index);
    }

    @Override
    public PagePosition handleMouseClick(GuiGraphics guiGraphics, int x, int y, int width, int height, PagePosition current, int index, double mouseX, double mouseY) {
        PagePosition pos = renderLine(guiGraphics, current, link.text, x, y, width, height, -1);
        if (pos.page == index && wasHovered()) {
            GuidePageFactory factory = link.getFactoryLink();
            GuidePageBase page = factory.createNew(gui);
            if (page != null) {
                gui.openPage(page);
            }
        }
        return pos;
    }
}
