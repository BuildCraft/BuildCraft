/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts;

import java.io.IOException;
import java.util.List;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.pos.GuiRectangle;

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

    public final void nextPage() {
        if (index + 2 < numPages) {
            index += 2;
        }
    }

    public final void lastPage() {
        index -= 2;
        if (index < 0) {
            index = 0;
        }
    }

    protected final void goToPage(int page) {
        // Make it a multiple of 2
        index = page / 2;
        index *= 2;
        if (index < 0) {
            index = 0;
        }
        if (index >= numPages) {
            index = numPages - 1;
        }
    }

    public int getPage() {
        return index;
    }

    public void tick() {}

    @Override
    public final PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition current, int index) {
        // NO-OP
        return current;
    }

    public abstract String getTitle();

    public boolean shouldPersistHistory() {
        return true;
    }

    public abstract List<GuideChapter> getChapters();

    protected GuidePart getClicked(Iterable<GuidePart> iterable, int x, int y, int width, int height, int mouseX, int mouseY, int index) {
        PagePosition pos = new PagePosition(0, 0);
        for (GuidePart part : iterable) {
            pos = part.renderIntoArea(x, y, width, height, pos, -1);
            if (pos.page == index && part.wasHovered) {
                return part;
            }
            if (pos.page > index) {
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
                GuiRectangle turnBox = new GuiRectangle(x, y + height, icon.width, icon.height);
                if (turnBox.contains(gui.mouse)) {
                    icon = GuiGuide.TURN_BACK_HOVERED;
                }
                icon.drawAt(turnBox);
            }
            // Page index
            String text = (index + 1) + " / " + numPages;
            double textX = x + GuiGuide.PAGE_LEFT_TEXT.width / 2 - getFontRenderer().getStringWidth(text) / 2;
            getFontRenderer().drawString(text, (int) textX, y + height + 6, 0);
        } else {
            // Odd => second page, draw forward button and second page index
            // Back page button
            if (index + 1 < numPages) {
                GuiIcon icon = GuiGuide.TURN_FORWARDS;
                GuiRectangle turnBox = new GuiRectangle(x + width - icon.width, y + height, icon.width, icon.height);
                if (turnBox.contains(gui.mouse)) {
                    icon = GuiGuide.TURN_FORWARDS_HOVERED;
                }
                icon.drawAt(turnBox);
            }
            // Page index
            if (index + 1 <= numPages) {
                String text = (index + 1) + " / " + numPages;
                double textX = x + (GuiGuide.PAGE_RIGHT_TEXT.width - getFontRenderer().getStringWidth(text)) / 2;
                getFontRenderer().drawString(text, (int) textX, y + height + 6, 0);
            }
        }
    }

    @Override
    public final PagePosition handleMouseClick(int x, int y, int width, int height, PagePosition current, int index, int mouseX, int mouseY) {
        // NO-OP, use the below!
        return current;
    }

    public void handleMouseClick(int x, int y, int width, int height, int mouseX, int mouseY, int mouseButton, int index, boolean isEditing) {
        // Even => first page, test page back button and first page text clicks
        if (index % 2 == 0) {
            if (index != 0) {
                GuiIcon icon = GuiGuide.TURN_BACK;
                GuiRectangle turnBox = new GuiRectangle(x, y + height, icon.width, icon.height);
                if (turnBox.contains(gui.mouse)) {
                    lastPage();
                }
            }
        } else {
            // Odd => second page, test forward page button
            if (index + 1 < numPages) {
                GuiIcon icon = GuiGuide.TURN_FORWARDS;
                GuiRectangle turnBox = new GuiRectangle(x + width - icon.width, y + height, icon.width, icon.height);
                if (turnBox.contains(gui.mouse)) {
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

    public boolean keyTyped(char typedChar, int keyCode) throws IOException {
        return false;
    }
}
