/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.client.guide.node.FormatString;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.gui.pos.GuiRectangle;

/** Represents a single page, image or crafting recipe for displaying. Only exists on the client. */
public abstract class GuidePart {
    public static final int INDENT_WIDTH = 16;
    public static final int LINE_HEIGHT = 16;

    /** Stores information about the current rendering position */
    public static class PagePosition {
        public final int page;
        public final int pixel;

        public PagePosition(int page, int pixel) {
            this.page = page;
            this.pixel = pixel;
        }

        public PagePosition nextLine(int pixelDifference, int maxHeight) {
            int added = pixel + pixelDifference;
            if (added >= maxHeight) {
                return nextPage();
            }
            return new PagePosition(page, added);
        }

        public PagePosition guaranteeSpace(int required, int maxPageHeight) {
            PagePosition next = nextLine(required, maxPageHeight);
            if (next.page == page) return this;
            return next;
        }

        public PagePosition nextPage() {
            return new PagePosition(page + 1, 0);
        }

        public PagePosition newPage() {
            if (pixel != 0) {
                return nextPage();
            }
            return this;
        }
    }

    protected final GuiGuide gui;
    private IFontRenderer fontRenderer;
    protected boolean wasHovered = false;
    protected boolean wasIconHovered = false;

    public GuidePart(GuiGuide gui) {
        this.gui = gui;
    }

    public IFontRenderer getFontRenderer() {
        return fontRenderer;
    }

    public void setFontRenderer(IFontRenderer fontRenderer) {
        this.fontRenderer = fontRenderer;
    }

    public boolean wasHovered() {
        return wasHovered;
    }

    public void updateScreen() {}

    /** Renders a raw line at the position, lowering it appropriately */
    protected void renderTextLine(String text, int x, int y, int colour) {
        fontRenderer.drawString(text, x, y + 8 - (fontRenderer.getFontHeight(text) / 2), colour);
        GlStateManager.color(1, 1, 1);
    }

    /** @param x
     * @param y
     * @param width
     * @param height
     * @param current The current position to render from
     * @param index The current page index to render on
     * @return The new position for the next part to render from */
    public abstract PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition current, int index);

    /** Like {@link #renderIntoArea(int, int, int, int, PagePosition, int)} but for a mouse click. */
    public abstract PagePosition handleMouseClick(int x, int y, int width, int height, PagePosition current, int index,
        int mouseX, int mouseY);

    public void handleMouseDragPartial(int startX, int startY, int currentX, int currentY, int button) {}

    public void handleMouseDragFinish(int startX, int startY, int endX, int endY, int button) {}

    /** @param current The current location of the rendering. This will be different from start if this line needed to
     *            render over 2 (or more!) pages
     * @param line The line to render
     * @param x The x position the page rendering started from
     * @param y The y position the page rendering started from
     * @param width The width of rendering space available
     * @param height The height of rendering space available
     * @return The position for the next line to render at. Will automatically be the next page or line if necessary. */
    protected PagePosition renderLine(PagePosition current, PageLine line, int x, int y, int width, int height,
        int pageRenderIndex) {
        wasHovered = false;
        wasIconHovered = false;
        // Firstly break off the last chunk if the total length is greater than the width allows
        int allowedWidth = width - INDENT_WIDTH * line.indent;
        if (allowedWidth <= 0) {
            throw new IllegalStateException("Was indented too far");
        }

        String toRender = line.text;
        ISimpleDrawable icon = line.startIcon;

        FormatString next = FormatString.split(line.text);

        int neededSpace = fontRenderer.getFontHeight(line.text);
        if (icon != null) {
            neededSpace = Math.max(16, neededSpace);
        }

        current = current.guaranteeSpace(neededSpace, height);

        int _x = x + INDENT_WIDTH * line.indent;
        if (icon != null && current.page == pageRenderIndex) {
            int iconX = _x - 18;
            int iconY = y + current.pixel - 5;
            GuiRectangle rect = new GuiRectangle(iconX, iconY, 16, 16);
            if (rect.contains(gui.mouse) && line.startIconHovered != null) {
                icon = line.startIconHovered;
            }
            icon.drawAt(iconX, iconY);
        }

        while (next != null) {
            FormatString[] strings = next.wrap(fontRenderer, allowedWidth);

            String text = strings[0].getFormatted();
            boolean render = current.page == pageRenderIndex;

            int _y = y + current.pixel;
            int _w = fontRenderer.getStringWidth(text);
            GuiRectangle rect = new GuiRectangle(_x, _y, _w, neededSpace);
            wasHovered |= rect.contains(gui.mouse);
            if (render) {
                if (wasHovered && line.link) {
                    Gui.drawRect(_x - 2, _y - 2, _x + _w + 2, _y - 2 + neededSpace, 0xFFD3AD6C);
                }
                fontRenderer.drawString(text, _x, _y, 0);
            }

            next = strings.length == 1 ? null : strings[1];
            current = current.nextLine(fontRenderer.getFontHeight(text) + 3, height);
        }

        int additional = LINE_HEIGHT - fontRenderer.getFontHeight(toRender) - 3;
        current = current.nextLine(additional, height);
        return current;
    }

    protected PagePosition renderLines(Iterable<PageLine> lines, PagePosition part, int x, int y, int width, int height,
        int index) {
        for (PageLine line : lines) {
            part = renderLine(part, line, x, y, width, height, index);
        }
        return part;
    }

    protected PagePosition renderLines(Iterable<PageLine> lines, int x, int y, int width, int height, int index) {
        return renderLines(lines, new PagePosition(0, 0), x, y, width, height, index);
    }
}
