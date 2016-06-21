package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;

public class GuidePartNewPage extends GuidePart {
    public GuidePartNewPage(GuiGuide gui) {
        super(gui);
    }

    @Override
    public PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition current, int index) {
        return current.newPage();
    }
    
    @Override
    public PagePosition handleMouseClick(int x, int y, int width, int height, PagePosition current, int index, int mouseX, int mouseY) {
        return current.newPage();
    }
}
