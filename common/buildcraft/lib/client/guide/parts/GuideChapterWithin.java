package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.IComparableLine;

public class GuideChapterWithin extends GuideChapter {
    private int lastPage = -1;

    public GuideChapterWithin(GuiGuide gui, int indent, String text, IComparableLine line) {
        super(gui, indent, text, line);
    }

    public GuideChapterWithin(GuiGuide gui, String chapter) {
        super(gui, chapter);
    }

    @Override
    public PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition current, int index) {
        PagePosition pos = super.renderIntoArea(x, y, width, height, current, index);
        lastPage = pos.page;
        if (pos.pixel == 0) {
            lastPage = pos.page - 1;
        }
        return pos;
    }

    @Override
    protected boolean onClick() {
        if (lastPage != -1) {
            GuidePageBase page = gui.getCurrentPage();
            if (page.getChapters().contains(this)) {
                page.goToPage(lastPage);
                return true;
            }
        }
        return false;
    }
}
