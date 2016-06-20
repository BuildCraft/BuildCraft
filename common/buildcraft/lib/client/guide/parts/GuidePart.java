package buildcraft.lib.client.guide.parts;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.gui.GuiRectangle;
import buildcraft.lib.gui.ISimpleDrawable;

/** Represents a single page, image or crafting recipe for displaying. Only exists on the client. */
@SideOnly(Side.CLIENT)
public abstract class GuidePart {
    public static final int INDENT_WIDTH = 16;
    public static final int LINE_HEIGHT = 17;

    /** Stores information about the current rendering position */
    public static class PagePart {
        public final int page;
        public final int pixel;

        public PagePart(int page, int pixel) {
            this.page = page;
            this.pixel = pixel;
        }

        public PagePart nextLine(int pixelDifference, int maxHeight) {
            int added = pixel + pixelDifference;
            if (added >= maxHeight) {
                return nextPage();
            }
            return new PagePart(page, added);
        }

        public PagePart guarenteeSpace(int required, int maxPageHeight) {
            PagePart next = nextLine(required, maxPageHeight);
            if (next.page == page) return this;
            return next;
        }

        public PagePart nextPage() {
            return new PagePart(page + 1, 0);
        }

        public PagePart newPage() {
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

    /** Renders a raw line at the position, lowering it appropriately */
    protected void renderTextLine(String text, int x, int y, int colour) {
        fontRenderer.drawString(text, x, y + 8 - (fontRenderer.getFontHeight() / 2), colour);
        GlStateManager.color(1, 1, 1);
    }

    /** @param x
     * @param y
     * @param width
     * @param height
     * @param current The current position to render from
     * @param index The current page index to render on
     * @return The new position for the next part to render from */
    public abstract PagePart renderIntoArea(int x, int y, int width, int height, PagePart current, int index);

    /** Like {@link #renderIntoArea(int, int, int, int, PagePart, int)} but for a mouse click. */
    public abstract PagePart handleMouseClick(int x, int y, int width, int height, PagePart current, int index, int mouseX, int mouseY);

    public void handleMouseDragPartial(int startX, int startY, int currentX, int currentY, int button) {}

    public void handleMouseDragFinish(int startX, int startY, int endX, int endY, int button) {}

    /** @param start Where to start the rendering from.
     * @param current The current location of the rendering. This will be different from start if this line needed to
     *            render over 2 (or more!) pages
     * @param line The line to render
     * @param x The x position the page rendering started from
     * @param y The y position the page rendering started from
     * @param width The width of rendering space available
     * @param height The height of rendering space available
     * @param simulate If true, this will just calculate the positions and return without rendering.
     * @return The position for the next line to render at. Will automatically be the next page or line if necessary. */
    protected PagePart renderLine(PagePart start, PagePart current, PageLine line, int x, int y, int width, int height, int pageRenderIndex) {
        wasHovered = false;
        wasIconHovered = false;
        // Firstly break off the last chunk if the total length is greater than the width allows
        int allowedWidth = width - INDENT_WIDTH * line.indent;
        if (allowedWidth <= 0) {
            throw new IllegalStateException("Was indented too far");
        }

        String toRender = line.text;
        ISimpleDrawable icon = line.startIcon;
        boolean firstLine = true;
        while (current.pixel <= height) {
            if (toRender.length() == 0) {
                break;
            }

            // Find out the longest string we can render
            int textLength = 1;
            int wordEnd = 0;
            // Start at 1 otherwise it will sometimes fail if there wasn't enough room
            while (textLength < toRender.length()) {
                String substring = toRender.substring(0, textLength);
                int textWidth = fontRenderer.getStringWidth(substring);
                if (textWidth > allowedWidth) {
                    break;
                }
                textLength++;
                // If we just completed a word
                if (substring.endsWith(" ")) {
                    wordEnd = textLength - 1;
                }
                // Or reach the end of the string
                else if (textLength == toRender.length()) {
                    wordEnd = textLength;
                }
            }
            if (wordEnd == 0) {
                wordEnd = textLength;
            }
            String thisLine = toRender.substring(0, wordEnd);
            toRender = toRender.substring(wordEnd);
            boolean render = pageRenderIndex == current.page;
            int stringWidth = fontRenderer.getStringWidth(thisLine);
            int linkX = x + INDENT_WIDTH * line.indent;
            int linkY = y + current.pixel;
            int linkXEnd = linkX + stringWidth + 2;
            int linkYEnd = linkY + fontRenderer.getFontHeight() + 2;
            GuiRectangle linkArea = new GuiRectangle(linkX, linkY, stringWidth + 2, fontRenderer.getFontHeight() + 2);
            if (line.link && linkArea.contains(gui.mouse)) {
                wasHovered = true;
                if (render) {
                    Gui.drawRect(linkX - 2, linkY - 2, linkXEnd, linkYEnd, 0xFFD3AD6C);
                }
            }
            if (render) {
                fontRenderer.drawString(thisLine, linkX, linkY, 0);
            }
            if (firstLine && icon != null) {
                int iconX = linkX - 18;
                /* Ok this is because minecraft default font size (The actual pixels) is 6, but fontRenderer.FONT_HEIGHT
                 * is 9. */
                int iconY = linkY - 5;
                GuiRectangle iconBox = new GuiRectangle(iconX, iconY, 16, 16);
                wasIconHovered = iconBox.contains(gui.mouse);
                if (wasIconHovered && line.startIconHovered != null) {
                    icon = line.startIconHovered;
                }
                if (render) {
                    icon.drawAt(iconX, iconY);
                    // icon.drawCutInside(iconBox);
                }
            }
            current = current.nextLine(LINE_HEIGHT, height);
            firstLine = false;
        }
        return current;
    }

    protected PagePart renderLines(Iterable<PageLine> lines, PagePart part, int x, int y, int width, int height, int index) {
        for (PageLine line : lines) {
            part = renderLine(part, part, line, x, y, width, height, index);
        }
        return part;
    }

    protected PagePart renderLines(Iterable<PageLine> lines, int x, int y, int width, int height, int index) {
        return renderLines(lines, new PagePart(0, 0), x, y, width, height, index);
    }
}
