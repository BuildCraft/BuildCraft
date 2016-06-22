package buildcraft.lib.client.guide.parts;

import net.minecraft.client.resources.I18n;

import buildcraft.lib.client.guide.GuiGuide;

public class GuideChapterContents extends GuideChapter {
    public GuideChapterContents(GuiGuide gui) {
        super(gui, I18n.format("buildcraft.guide.chapter.contents"));
    }

    @Override
    public void reset() {
        lastDrawn = EnumGuiSide.LEFT;
    }

    @Override
    protected boolean onClick() {
        gui.goBackToMenu();
        return true;
    }
}
