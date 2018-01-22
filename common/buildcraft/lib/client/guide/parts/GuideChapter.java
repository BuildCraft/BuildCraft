/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.misc.RenderUtil;

public abstract class GuideChapter extends GuidePart {
    public static final int[] COLOURS = { 0x9dd5c0, 0xfac174, 0x27a4dd };

    public static final int MAX_HOWEVER_PROGRESS = 5;
    public static final int MAX_HOVER_DISTANCE = 20;

    public final PageLine chapter;
    private int hoverProgress = 0, hoverProgressLast = 0;
    protected EnumGuiSide lastDrawn = null;

    public enum EnumGuiSide {
        LEFT,
        RIGHT
    }

    public GuideChapter(GuiGuide gui, String chapter) {
        this(gui, 0, chapter);
    }

    private int getColour() {
        int index = gui.getChapterIndex(this);
        if (index < 0) {
            index = chapter.text.hashCode();
            return index;
        }
        return COLOURS[index % COLOURS.length];
    }

    public GuideChapter(GuiGuide gui, int indent, String text) {
        super(gui);
        ISimpleDrawable icon = (x, y) -> {
            GuiGuide.BOX_EMPTY.drawAt(x, y);
            RenderUtil.setGLColorFromInt(getColour());
            GuiGuide.BOX_CHAPTER.drawAt(x, y);
            RenderUtil.setGLColorFromInt(-1);
        };
        ISimpleDrawable selected = (x, y) -> {
            GuiGuide.BOX_SELECTED_EMPTY.drawAt(x, y);
            RenderUtil.setGLColorFromInt(getColour());
            GuiGuide.BOX_SELECTED_CHAPTER.drawAt(x, y);
            RenderUtil.setGLColorFromInt(-1);
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

    public EnumGuiSide draw(int index, float partialTicks) {
        IFontRenderer font = gui.getCurrentFont();
        String text = chapter.text;
        float hoverWidth = getHoverWidth(partialTicks);
        float width = font.getStringWidth(text) + hoverWidth;
        int colour = COLOURS[index % COLOURS.length];

        int y = gui.minY + 20 * (index + 1);
        if (lastDrawn == EnumGuiSide.LEFT) {
            float x = gui.minX - width - 4 + 11;

            RenderUtil.setGLColorFromInt(colour);
            GuiGuide.CHAPTER_MARKER_LEFT.drawAt(x - 5, y - 4);
            float oX = x - 5 + GuiGuide.CHAPTER_MARKER_LEFT.width;
            GuiGuide.CHAPTER_MARKER_SPACE.drawScaledInside(oX, y - 4, width + 4, 16);
            RenderUtil.setGLColorFromInt(-1);

            font.drawString(text, (int) x, y, 0);
        } else if (lastDrawn == EnumGuiSide.RIGHT) {
            int x = gui.minX + GuiGuide.PAGE_LEFT.width + GuiGuide.PAGE_RIGHT.width - 11;

            RenderUtil.setGLColorFromInt(colour);
            GuiGuide.CHAPTER_MARKER_SPACE.drawScaledInside(x, y - 4, width + 4, 16);
            GuiGuide.CHAPTER_MARKER_RIGHT.drawAt(x + width + 4, y - 4);
            RenderUtil.setGLColorFromInt(-1);

            font.drawString(text, (int) (x + 4 + hoverWidth), y, 0);
        }
        return lastDrawn;
    }

    protected boolean isMouseInside() {
        IFontRenderer font = gui.getCurrentFont();
        String text = chapter.text;
        int width = (int) (font.getStringWidth(text) + getHoverWidth(0));

        int y = gui.minY + 20 * (gui.getChapterIndex(this) + 1);

        if (lastDrawn == EnumGuiSide.LEFT) {
            int x = gui.minX - width - 4 + 11;

            GuiRectangle drawRect = new GuiRectangle(x, y - 4, width + GuiGuide.CHAPTER_MARKER_LEFT.width, 16);
            return drawRect.contains(gui.mouse);
        } else if (lastDrawn == EnumGuiSide.RIGHT) {
            int x = gui.minX + GuiGuide.PAGE_LEFT.width + GuiGuide.PAGE_RIGHT.width - 11;

            GuiRectangle drawRect = new GuiRectangle(x, y - 4, width + GuiGuide.CHAPTER_MARKER_RIGHT.width, 16);
            return drawRect.contains(gui.mouse);
        }
        return false;
    }

    private float getHoverWidth(float partialTicks) {
        float prog = partialTicks * hoverProgress + (1 - partialTicks) * hoverProgressLast;
        return (prog * MAX_HOVER_DISTANCE) / MAX_HOWEVER_PROGRESS;
    }

    public boolean handleClick() {
        return isMouseInside() && onClick();
    }

    protected abstract boolean onClick();

    @Override
    public void updateScreen() {
        hoverProgressLast = hoverProgress;
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
