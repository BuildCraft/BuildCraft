package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;

public class GuidePartNewPage extends GuidePart {
    public GuidePartNewPage(GuiGuide gui) {
        super(gui);
    }

    @Override
    public PagePart renderIntoArea(int x, int y, int width, int height, PagePart current, int index) {
        return current.newPage();
    }
}
