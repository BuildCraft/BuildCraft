package buildcraft.lib.client.guide.parts;

import buildcraft.core.lib.client.render.RenderUtils;
import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.gui.GuiRectangle;
import buildcraft.lib.gui.ISimpleDrawable;

public abstract class GuideChapter extends GuidePart {
    public static final int[] COLOURS = { 0x9dd5c0, 0xfac174, 0x27a4dd };

    public static final int MAX_HOWEVER_PROGRESS = 5;
    public static final int MAX_HOVER_DISTANCE = 20;

    public final PageLine chapter;
    private int hoverProgress = 0;
    protected EnumGuiSide lastDrawn = null;

    public enum EnumGuiSide {
        LEFT,
        RIGHT;
    }

    public GuideChapter(GuiGuide gui, String chapter) {
        this(gui, 0, chapter);
    }

    private int getColour() {
        int index = gui.getChapterIndex(this);
        if (index < 0) return -1;
        return COLOURS[index % COLOURS.length];
    }

    public GuideChapter(GuiGuide gui, int indent, String text) {
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
        this.chapter = new PageLine(icon, selected, indent, text, false);
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

    public EnumGuiSide draw(int index) {
        IFontRenderer font = gui.getCurrentFont();
        String text = chapter.text;
        int hoverWidth = getHoverWidth();
        int width = font.getStringWidth(text) + hoverWidth;
        int colour = COLOURS[index % COLOURS.length];

        int y = gui.minY + 20 * (index + 1);
        if (lastDrawn == EnumGuiSide.LEFT) {
            int x = gui.minX - width - 4 + 11;

            RenderUtils.setGLColorFromInt(colour);
            GuiGuide.CHAPTER_MARKER_LEFT.drawAt(x - 5, y - 4);
            int oX = x - 5 + GuiGuide.CHAPTER_MARKER_LEFT.width;
            GuiGuide.CHAPTER_MARKER_SPACE.drawScaledInside(oX, y - 4, width + 4, 16);
            RenderUtils.setGLColorFromInt(-1);

            font.drawString(text, x, y, 0);
        } else if (lastDrawn == EnumGuiSide.RIGHT) {
            int x = gui.minX + GuiGuide.PAGE_LEFT.width + GuiGuide.PAGE_RIGHT.width - 11;

            RenderUtils.setGLColorFromInt(colour);
            GuiGuide.CHAPTER_MARKER_SPACE.drawScaledInside(x, y - 4, width + 4, 16);
            GuiGuide.CHAPTER_MARKER_RIGHT.drawAt(x + width + 4, y - 4);
            RenderUtils.setGLColorFromInt(-1);

            font.drawString(text, x + 4 + hoverWidth, y, 0);
        }
        return lastDrawn;
    }

    protected boolean isMouseInside() {
        IFontRenderer font = gui.getCurrentFont();
        String text = chapter.text;
        int width = font.getStringWidth(text) + getHoverWidth();

        int y = gui.minY + 20 * (gui.getChapterIndex(this) + 1);

        if (lastDrawn == EnumGuiSide.LEFT) {
            int x = gui.minX - width - 4 + 11;

            GuiRectangle drawRect = new GuiRectangle(x, y - 4, width + GuiGuide.CHAPTER_MARKER_LEFT.width, 16);
            if (drawRect.contains(gui.mouse)) {
                return true;
            }
        } else if (lastDrawn == EnumGuiSide.RIGHT) {
            int x = gui.minX + GuiGuide.PAGE_LEFT.width + GuiGuide.PAGE_RIGHT.width - 11;

            GuiRectangle drawRect = new GuiRectangle(x, y - 4, width + GuiGuide.CHAPTER_MARKER_RIGHT.width, 16);
            if (drawRect.contains(gui.mouse)) {
                return true;
            }
        }
        return false;
    }

    private int getHoverWidth() {
        return (hoverProgress * MAX_HOVER_DISTANCE) / MAX_HOWEVER_PROGRESS;
    }

    public boolean handleClick() {
        if (isMouseInside()) {
            return onClick();
        }
        return false;
    }

    protected abstract boolean onClick();

    @Override
    public void updateScreen() {
        if (isMouseInside()) {
            hoverProgress++;
            if (hoverProgress > MAX_HOWEVER_PROGRESS) {
                hoverProgress = MAX_HOWEVER_PROGRESS;
            }
        } else {
            hoverProgress--;
            if (hoverProgress < 0) {
                hoverProgress = 0;
            }
        }
    }
}
