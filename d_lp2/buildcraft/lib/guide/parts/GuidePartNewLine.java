package buildcraft.lib.guide.parts;

import buildcraft.lib.guide.GuiGuide;

public class GuidePartNewLine extends GuidePart {
    public GuidePartNewLine(GuiGuide gui) {
        super(gui);
    }

    @Override
    public PagePart renderIntoArea(int x, int y, int width, int height, PagePart current, int index) {
        return current.newPage();
    }
}
