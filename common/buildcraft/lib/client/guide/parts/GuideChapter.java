/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.client.sprite.SpriteNineSliced;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.misc.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class GuideChapter extends GuidePart {
    public static final int[] COLOURS = { 0x9dd5c0, 0xfac174, 0x27a4dd };

    public static final int MAX_HOWEVER_PROGRESS = 5;
    public static final int MAX_HOVER_DISTANCE = 20;

    private static final boolean FOLLOW_SIDE = false;

    public final PageLine chapter;

    /** 0 is the minimum value, and corresponds to the top-level of chapter. */
    public final int level;
    private int hoverProgress = 0, hoverProgressLast = 0;
    protected EnumGuiSide lastDrawn = null;

    private boolean expanded = false;

    @Nullable
    protected GuideChapter parent;
    protected final List<GuideChapter> children = new ArrayList<>();
    protected int colourIndex = -1;

    public enum EnumGuiSide {
        LEFT,
        RIGHT
    }

    //public GuideChapter(GuiGuide gui, String chapter)
    public GuideChapter(GuiGuide gui, String chapterKey, Component chapter) {
//        this(gui, 0, chapter);
        this(gui, 0, chapterKey, chapter);
    }

    // public GuideChapter(GuiGuide gui, int level, String text)
    public GuideChapter(GuiGuide gui, int level, String textKey, Component text) {
        super(gui);
        ISimpleDrawable icon = (p, x, y) ->
        {
            GuiGuide.BOX_EMPTY.drawAt(p, x, y);
            RenderUtil.setGLColorFromInt(getColour());
            GuiGuide.BOX_CHAPTER.drawAt(p, x, y);
            RenderUtil.setGLColorFromInt(-1);
        };
        ISimpleDrawable selected = (p, x, y) ->
        {
            GuiGuide.BOX_SELECTED_EMPTY.drawAt(p, x, y);
            RenderUtil.setGLColorFromInt(getColour());
            GuiGuide.BOX_SELECTED_CHAPTER.drawAt(p, x, y);
            RenderUtil.setGLColorFromInt(-1);
        };
        this.level = Math.max(0, level);
        icon = null;
        selected = null;
//        this.chapter = new PageLine(icon, selected, this.level + 1, text, false);
        this.chapter = new PageLine(icon, selected, this.level + 1, textKey, text, false);
    }

    private int getColour() {
        if (colourIndex < 0) {
            return chapter.text.hashCode();
        }
        return COLOURS[colourIndex % COLOURS.length];
    }

    public void reset() {
        lastDrawn = FOLLOW_SIDE ? EnumGuiSide.RIGHT : EnumGuiSide.LEFT;
    }

    public boolean hasParent() {
        return parent != null;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    void assignChildIndices() {
        int cIdx = 0;
        for (GuideChapter child : children) {
            if (cIdx % COLOURS.length == colourIndex) {
                cIdx++;
            }
            child.colourIndex = cIdx++ % COLOURS.length;
            if (child.hasChildren()) {
                child.assignChildIndices();
            }
        }
    }

    @Override
    public PagePosition renderIntoArea(PoseStack poseStack, int x, int y, int width, int height, PagePosition current, int index) {
        current = current.guaranteeSpace(getFontRenderer().getMaxFontHeight() * 4, height);

        int colour = getColour();

        if (current.page == index) {
            RenderUtil.setGLColorFromInt(colour);
            int _x = x + 12;
            int _y = y + current.pixel;
            PagePosition n2 = renderLine(poseStack, current, chapter, x, y, width, height, -1);
            int _height = n2.pixel - current.pixel;
            GuiGuide.CHAPTER_MARKER_9.draw(poseStack, _x - 5, _y - 4, width - 24, _height);
            RenderUtil.setGLColorFromInt(-1);
        }

        PagePosition n = renderLine(poseStack, current, chapter, x, y, width, height, index);
        index /= 2;
        if (n.page / 2 < index) {
            lastDrawn = EnumGuiSide.LEFT;
        } else if (index == current.page / 2 || index == current.page / 2) {
            lastDrawn = FOLLOW_SIDE ? null : EnumGuiSide.LEFT;
        }
        if (lastDrawn != null && parent != null) {
            GuideChapter p = parent;
            while (p != null) {
                if (p.lastDrawn == null) {
                    p.lastDrawn = lastDrawn;
                }
                p = p.parent;
            }
        }
        return n;
    }

    @Override
    public PagePosition handleMouseClick(PoseStack poseStack, int x, int y, int width, int height, PagePosition current, int index,
                                         double mouseX, double mouseY) {
        current = current.guaranteeSpace(getFontRenderer().getMaxFontHeight() * 4, height);
        return renderLine(poseStack, current, chapter, x, y, width, height, -1);
    }

    /** @param drawCentral TODO
     * @return The additional number of chapter segments drawn. */
    public int draw(PoseStack poseStack, int yIndex, float partialTicks, boolean drawCentral) {

        int drawnCount = 1;

        IFontRenderer font = gui.getCurrentFont();
        String text = chapter.text.getString();
        float hoverWidth = getHoverWidth(partialTicks);
        float width = font.getStringWidth(text) + hoverWidth;
        int colour = getColour();

        int baseY = drawCentral ? ((int) GuiGuide.FLOATING_CHAPTER_MENU.getY() + 6) : gui.minY;
        int y = baseY + (font.getMaxFontHeight() + 8) * (yIndex + 1);
        boolean hasChildren = !children.isEmpty();
        float _width = font.getStringWidth(text) + 12 + hoverWidth + (hasChildren ? 16 : 0);
        int fullHeight = font.getFontHeight(text) + 6;
        int childHeight = 0;

        if (hasChildren && expanded) {
            childHeight = fullHeight + getChildrenFullHeight();
            // _width = Math.max(_width, getChildrenMaxWidth());
        }

        if (drawCentral || lastDrawn == EnumGuiSide.RIGHT) {
            float x;
            if (drawCentral) {
                x = (float) GuiGuide.FLOATING_CHAPTER_MENU.getX() + 4 + hoverWidth;
                _width -= hoverWidth;
                hoverWidth = 0;
            } else {
                x = gui.minX + GuiGuide.PAGE_LEFT.width + GuiGuide.PAGE_RIGHT.width - 11;
            }
            x += level * 14;
            GuideChapter p = parent;
            while (p != null) {
                x += p.getHoverWidth(partialTicks);
                p = p.parent;
            }

            RenderUtil.setGLColorFromInt(colour);
            SpriteNineSliced icon = drawCentral ? GuiGuide.CHAPTER_MARKER_9 : GuiGuide.CHAPTER_MARKER_9_RIGHT;
            if (childHeight > 0) {
                icon.draw(poseStack, x + 10, y + fullHeight - 12, _width - 16, childHeight);
            }
            icon.draw(poseStack, x, y - 4, _width, fullHeight);
            if (hasChildren) {
                (expanded ? GuiGuide.EXPANDED_ARROW : GuiGuide.CLOSED_ARROW).drawAt(poseStack, x + hoverWidth, y - 4);
                x += 16;

                if (expanded) {
                    for (GuideChapter child : children) {
                        EnumGuiSide old = child.lastDrawn;
                        child.lastDrawn = lastDrawn;
                        drawnCount += child.draw(poseStack, yIndex + drawnCount, partialTicks, drawCentral);
                        child.lastDrawn = old;
                    }
                }
            }

            RenderUtil.setGLColorFromInt(-1);

            font.drawString(poseStack, text, (int) (x + 6 + hoverWidth), y, 0);
        } else if (lastDrawn == EnumGuiSide.LEFT) {
            float x = gui.minX - width + 5;
            if (hasChildren) {
                x -= 16;
            }

            RenderUtil.setGLColorFromInt(colour);
            if (childHeight > 0) {
                GuiGuide.CHAPTER_MARKER_9_LEFT.draw(poseStack, x + 10, y + fullHeight - 12, _width - 16, childHeight);
            }
            GuiGuide.CHAPTER_MARKER_9_LEFT.draw(poseStack, x - 6, y - 4, _width, fullHeight);

            if (hasChildren) {
                (expanded ? GuiGuide.EXPANDED_ARROW : GuiGuide.CLOSED_ARROW).drawAt(poseStack, x - 6, y - 4);
                x += 16;

                if (expanded) {
                    for (GuideChapter child : children) {
                        EnumGuiSide old = child.lastDrawn;
                        child.lastDrawn = lastDrawn;
                        drawnCount += child.draw(poseStack, yIndex + drawnCount, partialTicks, drawCentral);
                        child.lastDrawn = old;
                    }
                }
            }

            RenderUtil.setGLColorFromInt(-1);

            font.drawString(poseStack, text, (int) x, y, 0);
        }
        return drawnCount;
    }

    private int getChildrenFullHeight() {
        if (!expanded) {
            return 0;
        }
        int fullHeight = 0;
        IFontRenderer font = gui.getCurrentFont();
        for (GuideChapter c : children) {
            fullHeight += font.getFontHeight(c.chapter.text.getString()) + 6;
            fullHeight += c.getChildrenFullHeight();
            fullHeight += 2;
        }
        return fullHeight;
    }

    /** @return 0 for not hovered, 1 for the main chapter, or 2 for the arrow (if present). */
    protected int getMousePart() {
        IFontRenderer font = gui.getCurrentFont();
        String text = chapter.text.getString();
        float hoverWidth = getHoverWidth(0);
        final float realHoverWidth = hoverWidth;
        int width = (int) (font.getStringWidth(text) + hoverWidth) + (children.isEmpty() ? 0 : 16);

        int chapterIndex = 0;

        GuideChapter p = parent;
        while (p != null) {
            if (!p.expanded) {
                return 0;
            }
            p = p.parent;
        }

        for (GuideChapter c : gui.getChapters()) {
            chapterIndex++;
            if (c == this) {
                break;
            }
            p = c.parent;
            while (p != null) {
                if (!p.expanded) {
                    chapterIndex--;
                    break;
                }
                p = p.parent;
            }
        }

        boolean isCentral = gui.isSmallScreen();
        int baseY = isCentral ? ((int) GuiGuide.FLOATING_CHAPTER_MENU.getY() + 6) : gui.minY;
        int y = baseY + (font.getMaxFontHeight() + 8) * chapterIndex;

        if (isCentral || lastDrawn == EnumGuiSide.RIGHT) {
            int x;
            if (isCentral) {
                x = (int) GuiGuide.FLOATING_CHAPTER_MENU.getX() + 4 + (int) hoverWidth;
                width -= hoverWidth;
                hoverWidth = 0;
            } else {
                x = gui.minX + GuiGuide.PAGE_LEFT.width + GuiGuide.PAGE_RIGHT.width - 11;
            }
            x += level * 14;
            p = parent;
            while (p != null) {
                x += p.getHoverWidth(0);
                p = p.parent;
            }

            GuiRectangle drawRect = new GuiRectangle(x - realHoverWidth, y - 4, width + 16, 16);
            if (drawRect.contains(gui.mouse)) {
                GuiRectangle arrowRect = new GuiRectangle(x - realHoverWidth, y - 4, 24 + realHoverWidth, 16);
                if (hasChildren() && arrowRect.contains(gui.mouse)) {
                    return 2;
                }
                return 1;
            }
        } else if (lastDrawn == EnumGuiSide.LEFT) {
            int x = gui.minX - width - 5;

            GuiRectangle drawRect = new GuiRectangle(x, y - 4, width + 16, 16);
            if (drawRect.contains(gui.mouse)) {
                if (hasChildren() && new GuiRectangle(x, y - 4, 24, 16).contains(gui.mouse)) {
                    return 2;
                }
                return 1;
            }
        }
        return 0;
    }

    private float getHoverWidth(float partialTicks) {
        float prog = partialTicks * hoverProgress + (1 - partialTicks) * hoverProgressLast;
        return (prog * MAX_HOVER_DISTANCE) / MAX_HOWEVER_PROGRESS;
    }

    public int handleClick() {
        int part = getMousePart();
        if (part == 1) {
            return onClick() ? 1 : 0;
        } else if (part == 2) {
            expanded = !expanded;
            return 2;
        }
        return 0;
    }

    protected abstract boolean onClick();

    @Override
    public void updateScreen() {
        hoverProgressLast = hoverProgress;
        if (getMousePart() != 0) {
            hoverProgress += 2;
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
