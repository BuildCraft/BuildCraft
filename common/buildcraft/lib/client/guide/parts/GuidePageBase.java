/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.pos.GuiRectangle;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class GuidePageBase extends GuidePart {
    /** The current page that is being rendered */
    private int index = 0;
    protected int numPages = -1;

    public GuidePageBase(GuiGuide gui) {
        super(gui);
    }

    protected void setupChapters() {
        List<GuideChapter> lastChapterAtLevel = new ArrayList<>();
        List<GuideChapter> chapters = getChapters();
        for (GuideChapter chapter : chapters) {
            chapter.parent = null;
            chapter.children.clear();
        }
        for (GuideChapter chapter : chapters) {
            int gap = chapter.level - lastChapterAtLevel.size();

            if (gap < 0) {
                // There's some previous children that we need to clean up
                lastChapterAtLevel.subList(chapter.level, lastChapterAtLevel.size()).clear();
            }

            for (int g = Math.min(chapter.level, lastChapterAtLevel.size()) - 1; g >= 0; g--) {
                GuideChapter parent = lastChapterAtLevel.get(g);
                if (parent != null) {
                    parent.children.add(chapter);
                    chapter.parent = parent;
                    break;
                }
            }

            for (int g = 1; g < gap; g++) {
                lastChapterAtLevel.add(null);
            }
            lastChapterAtLevel.add(chapter);
        }

        int idx = 0;
        for (GuideChapter c : chapters) {
            if (c.hasParent()) {
                continue;
            }
            c.colourIndex = idx++ % GuideChapter.COLOURS.length;
            if (c.hasChildren()) {
                c.assignChildIndices();
            }
        }
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
        if (numPages > 0 && page >= numPages) {
            page = numPages - 1;
        }
        // Make it a multiple of 2
        index = page / 2;
        index *= 2;
        if (index < 0) {
            index = 0;
        }
    }

    public int getPage() {
        return index;
    }

    public int getPageCount() {
        return numPages;
    }

    public void tick() {
    }

    @Override
    public final PagePosition renderIntoArea(MatrixStack poseStack, int x, int y, int width, int height, PagePosition current, int index) {
        // NO-OP
        return current;
    }

    // public abstract String getTitle();
    public abstract ITextComponent getTitle();

    public boolean shouldPersistHistory() {
        return true;
    }

    /** Called when the {@link GuideManager} is reloaded.
     *
     * @return A page that can be shown and is valid after the reload, or null if this page cannot continue through a
     *         reload. */
    @Nullable
    public GuidePageBase createReloaded() {
        return null;
    }

    public abstract List<GuideChapter> getChapters();

    protected GuidePart getClicked(MatrixStack poseStack, Iterable<GuidePart> iterable, int x, int y, int width, int height, int mouseX, int mouseY, int index) {
        PagePosition pos = new PagePosition(0, 0);
        for (GuidePart part : iterable) {
            pos = part.renderIntoArea(poseStack, x, y, width, height, pos, -1);
            if (pos.page == index && part.wasHovered) {
                return part;
            }
            if (pos.page > index) {
                return null;
            }
        }
        return null;
    }

    public void renderFirstPage(MatrixStack poseStack, int x, int y, int width, int height) {
        renderPage(poseStack, x, y, width, height, index);
    }

    public void renderSecondPage(MatrixStack poseStack, int x, int y, int width, int height) {
        renderPage(poseStack, x, y, width, height, index + 1);
    }

    protected void renderPage(MatrixStack poseStack, int x, int y, int width, int height, int index) {
        // Even => first page, draw page back button and first page index
        if (index % 2 == 0) {
            // Back page button
            if (index != 0) {
                GuiIcon icon = GuiGuide.TURN_BACK;
                GuiRectangle turnBox = new GuiRectangle(x - 30, y + height, icon.width + 30, icon.height + 30);
                if (turnBox.contains(gui.mouse)) {
                    icon = GuiGuide.TURN_BACK_HOVERED;
                }
                icon.drawAt(turnBox.offset(30, 0), poseStack);
            }
            // Page index
            String text = (index + 1) + " / " + numPages;
            double textX = x + GuiGuide.PAGE_LEFT_TEXT.width / 2 - getFontRenderer().getStringWidth(text) / 2;
            getFontRenderer().drawString(poseStack, text, (int) textX, (int) (y + height) + 6, 0x90816a);
        } else {
            // Odd => second page, draw forward button and second page index
            // Back page button
            if (index + 1 < numPages) {
                GuiIcon icon = GuiGuide.TURN_FORWARDS;
                GuiRectangle turnBox = new GuiRectangle(
                        x + width - icon.width, y + height, icon.width + 30, icon.height + 30
                );
                if (turnBox.contains(gui.mouse)) {
                    icon = GuiGuide.TURN_FORWARDS_HOVERED;
                }
                icon.drawAt(turnBox, poseStack);
            }
            // Page index
            if (index + 1 <= numPages) {
                String text = (index + 1) + " / " + numPages;
                double textX = x + (GuiGuide.PAGE_RIGHT_TEXT.width - getFontRenderer().getStringWidth(text)) / 2;
                getFontRenderer().drawString(poseStack, text, (int) textX, (int) (y + height) + 6, 0x90816a);
            }
        }
    }

    @Override
    public final PagePosition handleMouseClick(MatrixStack poseStack, int x, int y, int width, int height, PagePosition current, int index,
                                               double mouseX, double mouseY) {
        // NO-OP, use the below!
        return current;
    }

    public void handleMouseClick(MatrixStack poseStack, int x, int y, int width, int height, double mouseX, double mouseY, int mouseButton,
                                 int index, boolean isEditing) {
        // Even => first page, test page back button and first page text clicks
        if (index % 2 == 0) {
            if (index != 0) {
                GuiIcon icon = GuiGuide.TURN_BACK;
                GuiRectangle turnBox = new GuiRectangle(x - 30, y + height, icon.width + 30, icon.height + 30);
                if (turnBox.contains(gui.mouse)) {
                    lastPage();
                }
            }
        } else {
            // Odd => second page, test forward page button
            if (index + 1 < numPages) {
                GuiIcon icon = GuiGuide.TURN_FORWARDS;
                GuiRectangle turnBox = new GuiRectangle(
                        x + width - icon.width, y + height, icon.width + 30, icon.height + 30
                );
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

    // public boolean keyTyped(char typedChar, int keyCode) throws IOException
    public boolean keyTyped(int typedChar, int keyCode, int modifiers) {
        return false;
    }

    public boolean charTyped(char typedChar, int keyCode) {
        return false;
    }
}
