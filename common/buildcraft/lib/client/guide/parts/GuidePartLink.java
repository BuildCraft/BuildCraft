package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.contents.PageLink;

public class GuidePartLink extends GuidePart {

    public final PageLink link;

    public GuidePartLink(GuiGuide gui, PageLink link) {
        super(gui);
        this.link = link;
    }

    @Override
    public PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition current, int index) {
        return renderLine(current, link.text, x, y, width, height, index);
    }

    @Override
    public PagePosition handleMouseClick(int x, int y, int width, int height, PagePosition current, int index,
        int mouseX, int mouseY) {
        PagePosition pos = renderLine(current, link.text, x, y, width, height, -1);
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
