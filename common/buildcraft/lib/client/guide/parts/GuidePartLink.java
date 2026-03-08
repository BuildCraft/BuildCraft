package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.contents.PageLink;
import com.mojang.blaze3d.matrix.MatrixStack;

public class GuidePartLink extends GuidePart {

    public final PageLink link;

    public GuidePartLink(GuiGuide gui, PageLink link) {
        super(gui);
        this.link = link;
    }

    @Override
    public PagePosition renderIntoArea(MatrixStack poseStack, int x, int y, int width, int height, PagePosition current, int index) {
        return renderLine(poseStack, current, link.text, x, y, width, height, index);
    }

    @Override
    public PagePosition handleMouseClick(MatrixStack poseStack, int x, int y, int width, int height, PagePosition current, int index, double mouseX, double mouseY) {
        PagePosition pos = renderLine(poseStack, current, link.text, x, y, width, height, -1);
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
