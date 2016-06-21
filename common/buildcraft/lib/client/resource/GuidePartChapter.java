package buildcraft.lib.client.resource;

import buildcraft.core.lib.client.render.RenderUtils;
import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.IComparableLine;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.client.guide.parts.GuidePart;
import buildcraft.lib.gui.ISimpleDrawable;

public class GuidePartChapter extends GuidePart {
    public static final int[] COLOURS = { 0x9dd5c0, 0xfac174, 0x27a4dd };

    public final PageLine chapter;
    private float hoverProgress = 0;
    private EnumGuiSide lastDrawn = null;

    public enum EnumGuiSide {
        LEFT,
        RIGHT;
    }

    public GuidePartChapter(GuiGuide gui, String chapter) {
        this(gui, 0, chapter, null);
    }

    private int getColour() {
        int index = gui.getChapterIndex(this);
        if (index < 0) return -1;
        return COLOURS[index % COLOURS.length];
    }

    public GuidePartChapter(GuiGuide gui, int indent, String text, IComparableLine line) {
        super(gui);
        ISimpleDrawable icon = (x, y) -> {
            GuiGuide.BOX_EMPTY.drawAt(x, y);
            RenderUtils.setGLColorFromInt(getColour());
            GuiGuide.BOX_CHAPTER.drawAt(x, y);
            RenderUtils.setGLColorFromInt(-1);
        };
        ISimpleDrawable selected = (x, y) -> {
            GuiGuide.BOX_SELECTED_EMPTY.drawAt(x, y);
            RenderUtils.setGLColorFromInt(getColour());
            GuiGuide.BOX_SELECTED_CHAPTER.drawAt(x, y);
            RenderUtils.setGLColorFromInt(-1);
        };
        this.chapter = new PageLine(icon, selected, indent, text, line, false);
    }

    public void reset() {
        lastDrawn = EnumGuiSide.RIGHT;
    }

    @Override
    public PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition current, int index) {
        PagePosition n = renderLine(current, chapter, x, y, width, height, index);
        index /= 2;
        if (n.page / 2 < index) {
            lastDrawn = EnumGuiSide.LEFT;
        } else if (index == current.page / 2 || index == current.page / 2) {
            lastDrawn = null;
        }
        return n;
    }

    @Override
    public PagePosition handleMouseClick(int x, int y, int width, int height, PagePosition current, int index, int mouseX, int mouseY) {
        return renderLine(current, chapter, x, y, width, height, -1);
    }

    public EnumGuiSide draw(int leftY, int rightY, int index) {
        IFontRenderer font = gui.getCurrentFont();
        String text = chapter.text;
        int width = font.getStringWidth(text);
        int colour = COLOURS[index % COLOURS.length];

        if (lastDrawn == EnumGuiSide.LEFT) {
            int x = gui.minX - width - 4;

            RenderUtils.setGLColorFromInt(colour);
            GuiGuide.CHAPTER_MARKER_LEFT.drawAt(x - 5, leftY - 4);
            int oX = x - 5 + GuiGuide.CHAPTER_MARKER_LEFT.width;
            GuiGuide.CHAPTER_MARKER_SPACE.drawScaledInside(oX, leftY - 4, width + 4, 16);
            RenderUtils.setGLColorFromInt(-1);

            font.drawString(text, x, leftY, 0);
        } else if (lastDrawn == EnumGuiSide.RIGHT) {
            int x = gui.minX + GuiGuide.PAGE_LEFT.width + GuiGuide.PAGE_RIGHT.width;

            RenderUtils.setGLColorFromInt(colour);
            GuiGuide.CHAPTER_MARKER_SPACE.drawScaledInside(x, rightY - 4, width + 4, 16);
            GuiGuide.CHAPTER_MARKER_RIGHT.drawAt(x + width + 4, rightY - 4);
            RenderUtils.setGLColorFromInt(-1);

            font.drawString(text, x + 4, rightY, 0);
        }
        return lastDrawn;
    }
}
