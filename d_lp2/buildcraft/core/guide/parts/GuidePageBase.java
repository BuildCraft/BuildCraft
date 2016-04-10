package buildcraft.core.guide.parts;

import buildcraft.core.guide.GuiGuide;
import buildcraft.core.guide.PageLine;
import buildcraft.core.lib.gui.GuiTexture.GuiIcon;

public abstract class GuidePageBase extends GuidePart {
    /** The current page that is being rendered */
    private int index = 0;
    protected int numPages = -1;

    public GuidePageBase(GuiGuide gui) {
        super(gui);
    }

    protected final int getIndex() {
        return index;
    }

    protected final void nextPage() {
        if (index + 1 < numPages) {
            index += 2;
        }
    }

    protected final void backPage() {
        index -= 2;
        if (index < 0) {
            index = 0;
        }
    }

    public int getPage() {
        return index;
    }

    public void tick(float elapsedTime) {}

    @Override
    public final PagePart renderIntoArea(int x, int y, int width, int height, PagePart current, int index) {
        // NO-OP
        return current;
    }

    protected PageLine getClicked(Iterable<PageLine> lines, int x, int y, int width, int height, int mouseX, int mouseY, int index) {
        PagePart part = new PagePart(0, 0);
        for (PageLine line : lines) {
            part = renderLine(part, part, line, x, y, width, height, -1);
            if (wasHovered) {
                return line;
            }
            if (part.page > index) {
                return null;
            }
        }
        return null;
    }

    protected PageLine getIconClicked(Iterable<PageLine> lines, int x, int y, int width, int height, int mouseX, int mouseY, int index) {
        PagePart part = new PagePart(0, 0);
        for (PageLine line : lines) {
            part = renderLine(part, part, line, x, y, width, height, -1);
            if (wasIconHovered) {
                return line;
            }
            if (part.page > index) {
                return null;
            }
        }
        return null;
    }

    public void renderFirstPage(int x, int y, int width, int height) {
        renderPage(x, y, width, height, index);
    }

    public void renderSecondPage(int x, int y, int width, int height) {
        renderPage(x, y, width, height, index + 1);
    }

    protected void renderPage(int x, int y, int width, int height, int index) {
        // Even => first page, draw page back button and first page index
        if (index % 2 == 0) {
            // Back page button
            if (index != 0) {
                GuiIcon icon = GuiGuide.TURN_BACK;
                if (icon.isMouseInside(x, y + height, mouseX, mouseY)) {
                    icon = GuiGuide.TURN_BACK_HOVERED;
                }
                icon.draw(x, y + height);
            }
            // Page index
            String text = (index + 1) + " / " + numPages;
            getFontRenderer().drawString(text, x + GuiGuide.PAGE_LEFT_TEXT.width / 2 - getFontRenderer().getStringWidth(text) / 2, y + height, 0);
        }
        // Odd => second page, draw forward button and second page index
        else {
            // Back page button
            if (index + 1 < numPages) {
                GuiIcon icon = GuiGuide.TURN_FORWARDS;
                if (icon.isMouseInside(x + width - icon.width, y + height, mouseX, mouseY)) {
                    icon = GuiGuide.TURN_FORWARDS_HOVERED;
                }
                icon.draw(x + width - icon.width, y + height);
            }
            // Page index
            if (index + 1 <= numPages) {
                String text = (index + 1) + " / " + numPages;
                getFontRenderer().drawString(text, x + (GuiGuide.PAGE_RIGHT_TEXT.width - getFontRenderer().getStringWidth(text)) / 2, y + height, 0);
            }
        }
    }

    @Override
    public final void handleMouseClick(int x, int y, int button, int... arguments) {
        // NO-OP, use the below!
    }

    public void handleMouseClick(int x, int y, int width, int height, int mouseX, int mouseY, int mouseButton, int index, boolean isEditing) {
        // Even => first page, test page back button and first page text clicks
        if (index % 2 == 0) {
            if (index != 0) {
                GuiIcon icon = GuiGuide.TURN_BACK;
                if (icon.isMouseInside(x, y + height, mouseX, mouseY)) {
                    backPage();
                }
            }
        }
        // Odd => second page, test forward page button
        else {
            if (index + 1 < numPages) {
                GuiIcon icon = GuiGuide.TURN_FORWARDS;
                if (icon.isMouseInside(x + width - icon.width, y + height, mouseX, mouseY)) {
                    nextPage();
                }
            }
        }
    }

    @Override
    public final void handleMouseDragPartial(int startX, int startY, int currentX, int currentY, int button) {

    }

    @Override
    public final void handleMouseDragFinish(int startX, int startY, int endX, int endY, int button) {

    }
}
