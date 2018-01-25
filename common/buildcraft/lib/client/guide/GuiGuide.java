/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import buildcraft.lib.client.guide.parts.contents.GuidePageContents;
import com.google.common.collect.Queues;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import buildcraft.lib.client.guide.font.FontManager;
import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.client.guide.parts.GuideChapter;
import buildcraft.lib.client.guide.parts.GuidePageBase;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.MousePosition;
import net.minecraft.util.text.TextFormatting;

public class GuiGuide extends GuiScreen {
    public static final ResourceLocation ICONS_1 = Gui.ICONS;
    public static final ResourceLocation ICONS_2 = new ResourceLocation("buildcraftlib:textures/gui/guide/icons.png");
    public static final ResourceLocation COVER = new ResourceLocation("buildcraftlib:textures/gui/guide/cover.png");
    public static final ResourceLocation LEFT_PAGE =
        new ResourceLocation("buildcraftlib:textures/gui/guide/left_page.png");
    public static final ResourceLocation RIGHT_PAGE =
        new ResourceLocation("buildcraftlib:textures/gui/guide/right_page.png");
    public static final ResourceLocation LEFT_PAGE_BACK =
        new ResourceLocation("buildcraftlib:textures/gui/guide/left_page_back.png");
    public static final ResourceLocation RIGHT_PAGE_BACK =
        new ResourceLocation("buildcraftlib:textures/gui/guide/right_page_back.png");
    public static final ResourceLocation NOTE = new ResourceLocation("buildcraftlib:textures/gui/guide/note.png");

    public static final GuiIcon BOOK_COVER = new GuiIcon(COVER, 0, 0, 202, 248);
    public static final GuiIcon BOOK_BINDING = new GuiIcon(COVER, 204, 0, 11, 248);

    public static final GuiIcon PAGE_LEFT = new GuiIcon(LEFT_PAGE, 0, 0, 193, 248);
    public static final GuiIcon PAGE_RIGHT = new GuiIcon(RIGHT_PAGE, 0, 0, 193, 248);

    public static final GuiIcon PAGE_LEFT_BACK = new GuiIcon(LEFT_PAGE_BACK, 0, 0, 193, 248);
    public static final GuiIcon PAGE_RIGHT_BACK = new GuiIcon(RIGHT_PAGE_BACK, 0, 0, 193, 248);

    public static final GuiRectangle PAGE_LEFT_TEXT = new GuiRectangle(23, 25, 168, 190);
    public static final GuiRectangle PAGE_RIGHT_TEXT = new GuiRectangle(4, 25, 168, 190);

    public static final GuiIcon PEN_UP = new GuiIcon(ICONS_2, 0, 0, 14, 135);
    public static final GuiIcon PEN_ANGLED = new GuiIcon(ICONS_2, 17, 0, 100, 100);

    public static final GuiIcon PEN_HIDDEN_MIN = new GuiIcon(ICONS_2, 0, 4, 10, 5);
    public static final GuiIcon PEN_HIDDEN_MAX = new GuiIcon(ICONS_2, 0, 4, 10, 15);

    public static final GuiIcon TURN_BACK = new GuiIcon(ICONS_2, 23, 139, 18, 10);
    public static final GuiIcon TURN_BACK_HOVERED = new GuiIcon(ICONS_2, 23, 152, 18, 10);

    public static final GuiIcon TURN_FORWARDS = new GuiIcon(ICONS_2, 0, 139, 18, 10);
    public static final GuiIcon TURN_FORWARDS_HOVERED = new GuiIcon(ICONS_2, 0, 152, 18, 10);

    public static final GuiIcon BACK = new GuiIcon(ICONS_2, 48, 139, 17, 9);
    public static final GuiIcon BACK_HOVERED = new GuiIcon(ICONS_2, 48, 152, 17, 9);

    public static final GuiIcon BOX_EMPTY = new GuiIcon(ICONS_2, 0, 164, 16, 16);
    public static final GuiIcon BOX_MINUS = new GuiIcon(ICONS_2, 16, 164, 16, 16);
    public static final GuiIcon BOX_PLUS = new GuiIcon(ICONS_2, 32, 164, 16, 16);
    public static final GuiIcon BOX_TICKED = new GuiIcon(ICONS_2, 48, 164, 16, 16);
    public static final GuiIcon BOX_CHAPTER = new GuiIcon(ICONS_2, 64, 164, 16, 16);

    public static final GuiIcon BOX_SELECTED_EMPTY = new GuiIcon(ICONS_2, 0, 180, 16, 16);
    public static final GuiIcon BOX_SELECTED_MINUS = new GuiIcon(ICONS_2, 16, 180, 16, 16);
    public static final GuiIcon BOX_SELECTED_PLUS = new GuiIcon(ICONS_2, 32, 180, 16, 16);
    public static final GuiIcon BOX_SELECTED_TICKED = new GuiIcon(ICONS_2, 48, 180, 16, 16);
    public static final GuiIcon BOX_SELECTED_CHAPTER = new GuiIcon(ICONS_2, 64, 180, 16, 16);

    public static final GuiIcon BORDER_TOP_LEFT = new GuiIcon(ICONS_2, 0, 196, 13, 13);
    public static final GuiIcon BORDER_TOP_RIGHT = new GuiIcon(ICONS_2, 13, 196, 13, 13);
    public static final GuiIcon BORDER_BOTTOM_LEFT = new GuiIcon(ICONS_2, 0, 209, 13, 13);
    public static final GuiIcon BORDER_BOTTOM_RIGHT = new GuiIcon(ICONS_2, 13, 209, 13, 13);

    public static final GuiIcon ORDER_TYPE = new GuiIcon(ICONS_2, 14, 100, 14, 14);
    public static final GuiIcon ORDER_MOD_TYPE = new GuiIcon(ICONS_2, 42, 100, 14, 14);
    public static final GuiIcon ORDER_MOD = new GuiIcon(ICONS_2, 56, 100, 14, 14);

    public static final GuiIcon CHAPTER_MARKER_LEFT = new GuiIcon(ICONS_2, 0, 223, 5, 16);
    public static final GuiIcon CHAPTER_MARKER_SPACE = new GuiIcon(ICONS_2, 6, 223, 19, 16);
    public static final GuiIcon CHAPTER_MARKER_RIGHT = new GuiIcon(ICONS_2, 27, 223, 5, 16);

    public static final GuiIcon NOTE_PAGE = new GuiIcon(NOTE, 0, 0, 131, 164);
    public static final GuiIcon NOTE_UNDERLAY = new GuiIcon(ICONS_2, 0, 1, 3, 4);
    public static final GuiIcon NOTE_OVERLAY = new GuiIcon(ICONS_2, 0, 1, 2, 3);

    public static final GuiIcon SEARCH_ICON = new GuiIcon(ICONS_2, 26, 196, 12, 12);
    public static final GuiIcon SEARCH_TAB_CLOSED = new GuiIcon(ICONS_2, 58, 196, 14, 6);
    public static final GuiIcon SEARCH_TAB_OPEN = new GuiIcon(ICONS_2, 40, 209, 106, 14);

    public static final GuiIcon[] ORDERS = { ORDER_TYPE, ORDER_MOD_TYPE, ORDER_MOD };

    public static final GuiRectangle BACK_POSITION =
        new GuiRectangle(PAGE_LEFT.width - BACK.width / 2, PAGE_LEFT.height - BACK.height - 2, BACK.width, BACK.height);

    public static final TypeOrder[] SORTING_TYPES = { //
        new TypeOrder(ETypeTag.TYPE, ETypeTag.SUB_TYPE), //
        new TypeOrder(ETypeTag.MOD, ETypeTag.TYPE), //
        new TypeOrder(ETypeTag.MOD, ETypeTag.SUB_MOD),//
    };

    // REMOVE FROM HERE...
    private static final int PEN_HIDDEN_Y = 0, PEN_HIDDEN_X = 4, PEN_HIDDEN_WIDTH = 10;
    private static final int PEN_HIDDEN_HEIGHT_MIN = 5, PEN_HIDDEN_HEIGHT_MAX = 15;
    // TO HERE

    public static final GuiRectangle PEN_HIDDEN_AREA = new GuiRectangle(PAGE_LEFT.width - PEN_HIDDEN_WIDTH / 2,
        -PEN_HIDDEN_HEIGHT_MAX, PEN_HIDDEN_WIDTH, PEN_HIDDEN_HEIGHT_MAX);

    // private static final int PEN_HIDDEN_BOX_X_MIN = PAGE_LEFT.width - PEN_HIDDEN_WIDTH / 2;
    // private static final int PEN_HIDDEN_BOX_Y_MIN = -PEN_HIDDEN_HEIGHT_MAX;
    // private static final int PEN_HIDDEN_BOX_X_MAX = PAGE_LEFT.width + PEN_HIDDEN_WIDTH / 2;
    // private static final int PEN_HIDDEN_BOX_Y_MAX = 0;

    private static final float PEN_HOVER_TIME = 9f;
    private static final float BOOK_OPEN_TIME = 10f; // 20

    public final MousePosition mouse = new MousePosition();

    public int sortingOrderIndex = 0;
    private boolean isOpen = false, isEditing = false;
    private boolean isOpening = false;

    /** Float between -90 and 90} */
    private float openingAngleLast = -90, openingAngleNext = -90;

    /** Float between {@link #PEN_HIDDEN_HEIGHT_MIN} and {@link #PEN_HIDDEN_HEIGHT_MAX} */
    private float hoverStageLast = 0, hoverStageNext = 0;
    private boolean isOverHover = false;

    public int minX, minY;
    public ItemStack tooltipStack = null;
    public final List<String> tooltip = new ArrayList<>();

    private final Deque<GuidePageBase> pages = Queues.newArrayDeque();
    private final List<GuideChapter> chapters = new ArrayList<>();
    private GuidePageBase currentPage;
    private IFontRenderer currentFont = FontManager.INSTANCE.getOrLoadFont("SansSerif", 9);
    private float lastPartialTicks;

    public GuiGuide() {
        mc = Minecraft.getMinecraft();
        openPage(new GuidePageContents(this));
        // TODO: Add a full screen option, with a constant colour (or gradiant, or computed noise?)
    }

    public GuiGuide(String noteId) {
        mc = Minecraft.getMinecraft();
        // TODO (AlexIIL): add support for notes!
        // TODO (AlexIIL): Separate text drawing from everything else (layer [gl, buffered, gl])
    }

    public void initForExport() {
        // TODO: Move this out of this gui, and also change factories in some way to support
        // exporting to other formats.
        isOpening = true;
        isOpen = true;
        setWorldAndResolution(Minecraft.getMinecraft(), 1920, 1080);
    }

    public void openPage(GuidePageBase page) {
        if (currentPage != null && currentPage.shouldPersistHistory()) {
            pages.push(currentPage);
        }
        setPageInternal(page);
    }

    public void closePage() {
        if (pages.isEmpty()) {
            mc.displayGuiScreen(null);
        } else {
            setPageInternal(pages.pop());
        }
    }

    public void goBackToMenu() {
        GuidePageBase newPage = currentPage;
        while (!pages.isEmpty()) {
            newPage = pages.pop();
        }
        setPageInternal(newPage);
    }

    private void setPageInternal(GuidePageBase page) {
        currentPage = page;
        refreshChapters();
    }

    public GuidePageBase getCurrentPage() {
        return currentPage;
    }

    public IFontRenderer getCurrentFont() {
        return this.currentFont;
    }

    public int getChapterIndex(GuideChapter chapter) {
        return chapters.indexOf(chapter);
    }

    public void refreshChapters() {
        chapters.clear();
        chapters.addAll(currentPage.getChapters());
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (isOpen) {
            // Calculate pen hover position
            float hoverDiff = (PEN_HIDDEN_HEIGHT_MAX - PEN_HIDDEN_HEIGHT_MIN) / PEN_HOVER_TIME;
            hoverStageLast = hoverStageNext;
            if (hoverStageNext > PEN_HIDDEN_HEIGHT_MAX) {
                hoverStageNext -= hoverDiff * 5;
            } else if (isOverHover) {
                hoverStageNext += hoverDiff;
                if (hoverStageNext > PEN_HIDDEN_HEIGHT_MAX) {
                    hoverStageNext = PEN_HIDDEN_HEIGHT_MAX;
                }
            } else {
                if (hoverStageNext > PEN_HIDDEN_HEIGHT_MIN) {
                    hoverStageNext -= hoverDiff;
                }
                if (hoverStageNext < PEN_HIDDEN_HEIGHT_MIN) {
                    hoverStageNext = PEN_HIDDEN_HEIGHT_MIN;
                }
            }
            currentPage.updateScreen();
            for (GuideChapter chapter : chapters) {
                chapter.updateScreen();
            }
        } else if (isOpening) {
            openingAngleLast = openingAngleNext;
            openingAngleNext += 180 / BOOK_OPEN_TIME;
        }
        if (currentPage != null) {
            setupFontRenderer();
            currentPage.tick();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        lastPartialTicks = partialTicks = mc.getRenderPartialTicks();
        minX = (width - PAGE_LEFT.width * 2) / 2;
        minY = (height - BOOK_COVER.height) / 2;
        mouse.setMousePosition(mouseX, mouseY);
        try {
            if (isOpen) {
                drawOpen(partialTicks);
            } else if (isOpening) {
                drawOpening(partialTicks);
            } else {
                drawCover();
            }
        } catch (Throwable t) {
            // Temporary fix for crash report classes crashing so we can see the ACTUAL error
            t.printStackTrace();
            throw new RuntimeException(t);
        }
    }

    public float getLastPartialTicks() {
        return this.lastPartialTicks;
    }

    public void drawTooltip(ItemStack stack, int x, int y) {
        renderToolTip(stack, x, y);
    }

    private void drawCover() {
        minX = (width - BOOK_COVER.width) / 2;
        minY = (height - BOOK_COVER.height) / 2;

        mc.renderEngine.bindTexture(COVER);
        BOOK_COVER.drawAt(minX, minY);
    }

    private void drawOpening(float partialTicks) {
        minX = (width - BOOK_COVER.width) / 2;
        minY = (height - BOOK_COVER.height) / 2;

        float openingAngle = openingAngleLast * (1 - partialTicks) + openingAngleNext * partialTicks;
        float sin = MathHelper.sin((float) (openingAngle * Math.PI / 180));
        if (sin < 0) {
            sin *= -1;
        }
        if (openingAngle >= 90) {
            isOpen = true;
        }
        if (openingAngle < 0) {
            minX = (width - BOOK_COVER.width) / 2;
            minY = (height - BOOK_COVER.height) / 2;

            int coverWidth = (int) (sin * BOOK_COVER.width);
            sin = 1 - sin;
            float offset = sin * 50;
            int bindingWidth = (int) (sin * BOOK_BINDING.width);

            mc.renderEngine.bindTexture(RIGHT_PAGE);
            PAGE_RIGHT.drawAt(minX + BOOK_COVER.width - PAGE_RIGHT.width, minY);

            mc.renderEngine.bindTexture(COVER);

            // BOOK_COVER.drawScaledInside(minX, minY, coverWidth, BOOK_COVER.height);
            // BOOK_COVER.drawCustomQuad(
            // minX, minY + BOOK_COVER.height,
            // minX + coverWidth, minY + BOOK_COVER.height,
            // minX + coverWidth, minY,
            // minX, minY
            // ); // like drawScaledInside, but using drawCustomQuad
            BOOK_COVER.drawCustomQuad(minX, minY + BOOK_COVER.height, minX + coverWidth,
                minY + BOOK_COVER.height + offset, minX + coverWidth, minY - offset, minX, minY);

            BOOK_BINDING.drawScaledInside((int) (minX + coverWidth - bindingWidth * 0.5), (int) (minY - offset),
                bindingWidth, (int) (BOOK_BINDING.height + offset * 2));

        } else if (openingAngle == 0) {
            minX = (width - BOOK_COVER.width) / 2;
            minY = (height - BOOK_COVER.height) / 2;

            mc.renderEngine.bindTexture(RIGHT_PAGE);
            PAGE_RIGHT.drawAt(minX + BOOK_COVER.width - PAGE_LEFT.width, minY);

            mc.renderEngine.bindTexture(COVER);
            BOOK_COVER.drawAt(minX, minY);
        } else if (openingAngle > 0) {
            int pageWidth = (int) (sin * PAGE_LEFT.width);
            int bindingWidth = (int) ((1 - sin) * BOOK_BINDING.width);

            int penHeight = (int) (sin * PEN_HIDDEN_HEIGHT_MIN);
            float offset = (1 - sin) * 50;

            minX = (width - PAGE_LEFT.width - pageWidth) / 2;
            minY = (height - BOOK_COVER.height) / 2;

            mc.renderEngine.bindTexture(RIGHT_PAGE);
            PAGE_RIGHT.drawAt(minX + pageWidth + bindingWidth, minY);

            mc.renderEngine.bindTexture(LEFT_PAGE);
            // PAGE_LEFT.drawCustomQuad(
            // minX + bindingWidth, minY + PAGE_LEFT.height + offset,
            // minX + bindingWidth + pageWidth, minY + PAGE_LEFT.height,
            // minX + bindingWidth + pageWidth, minY,
            // minX + bindingWidth, minY - offset
            // );
            PAGE_LEFT.drawCustomQuad(minX + bindingWidth, minY + PAGE_LEFT.height + offset,
                minX + bindingWidth + pageWidth, minY + PAGE_LEFT.height, minX + bindingWidth + pageWidth, minY,
                minX + bindingWidth, minY - offset);
            // PAGE_LEFT.drawScaledInside(minX + bindingWidth, minY, pageWidth, PAGE_LEFT.height);

            mc.renderEngine.bindTexture(COVER);
            BOOK_BINDING.drawScaledInside((int) (minX + bindingWidth * 0.5), (int) (minY - offset), bindingWidth,
                (int) (BOOK_BINDING.height + offset * 2));

            mc.renderEngine.bindTexture(ICONS_2);
            drawTexturedModalRect(minX + pageWidth + bindingWidth - (PEN_HIDDEN_WIDTH / 2), minY - penHeight,
                PEN_HIDDEN_X, PEN_HIDDEN_Y, PEN_HIDDEN_WIDTH, penHeight);
        }
    }

    private void drawOpen(float partialTicks) {
        // Draw the pages
        mc.renderEngine.bindTexture(LEFT_PAGE);
        PAGE_LEFT.drawAt(minX, minY);

        mc.renderEngine.bindTexture(RIGHT_PAGE);
        PAGE_RIGHT.drawAt(minX + PAGE_LEFT.width, minY);

        isOverHover = PEN_HIDDEN_AREA.offset(minX, minY).contains(mouse);

        // Now draw the actual contents of the book
        String title = currentPage.getTitle();
        if (title != null) {
            int x = /* this.minX + */ (width - currentFont.getStringWidth(title)) / 2;
            currentFont.drawString(title, x, minY + 12, 0);
        }

        tooltipStack = null;
        tooltip.clear();
        setupFontRenderer();
        for (GuideChapter chapter : chapters) {
            chapter.reset();
        }

        currentPage.renderFirstPage(minX + (int) PAGE_LEFT_TEXT.x, minY + (int) PAGE_LEFT_TEXT.y,
                (int) PAGE_LEFT_TEXT.width, (int) PAGE_LEFT_TEXT.height);
        currentPage.renderSecondPage(minX + PAGE_LEFT.width + (int) PAGE_RIGHT_TEXT.x, minY + (int) PAGE_RIGHT_TEXT.y,
                (int) PAGE_RIGHT_TEXT.width, (int) PAGE_RIGHT_TEXT.height);

        int chapterIndex = 0;
        for (GuideChapter chapter : chapters) {
            chapter.draw(chapterIndex, partialTicks);
            chapterIndex++;
        }

        // Draw the back button if there are any pages on the stack
        if (!pages.isEmpty()) {
            GuiIcon icon = BACK;
            IGuiArea position = BACK_POSITION.offset(minX, minY);
            if (position.contains(mouse)) {
                icon = BACK_HOVERED;
            }
            icon.drawAt(position);
        }

        // Reset the colour for the pen
        GlStateManager.color(1, 1, 1);

        // Draw the pen
        if (isEditing) {
            mc.renderEngine.bindTexture(ICONS_2);

            if (isOverHover) {
                PEN_UP.drawAt(mouse.getX() - PEN_UP.width / 2, mouse.getY() - PEN_UP.height);
            } else {
                PEN_ANGLED.drawAt(mouse.getX() - 2, mouse.getY() - PEN_ANGLED.height - 2);
            }
        } else {
            int h = (int) (hoverStageLast * (1 - partialTicks) + hoverStageNext * partialTicks);

            // Draw pen
            mc.renderEngine.bindTexture(ICONS_2);
            drawTexturedModalRect(minX + PAGE_LEFT.width - PEN_HIDDEN_WIDTH / 2, minY - h, PEN_HIDDEN_X, PEN_HIDDEN_Y,
                PEN_HIDDEN_WIDTH, h);

            if (tooltipStack != null) {
                renderToolTip(tooltipStack, (int) mouse.getX(), (int) mouse.getY());
            } else if (!tooltip.isEmpty()) {
                drawHoveringText(tooltip, (int) mouse.getX(), (int) mouse.getY());
            }
        }
    }

    public void setupFontRenderer() {
        currentPage.setFontRenderer(currentFont);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        mouse.setMousePosition(mouseX, mouseY);
        // Primary mouse button
        if (mouseButton == 0) {
            if (isOpen) {
                int page0xMin = this.minX + (int) PAGE_LEFT_TEXT.x;
                int page0xMax = page0xMin + (int) PAGE_LEFT_TEXT.width;
                int page1xMin = this.minX + PAGE_LEFT.width + (int) PAGE_RIGHT_TEXT.x;
                int page1xMax = page1xMin + (int) PAGE_RIGHT_TEXT.width;
                int pageYMin = this.minY + (int) PAGE_RIGHT_TEXT.y;
                int pageYMax = pageYMin + (int) PAGE_RIGHT_TEXT.height;

                GuidePageBase current = currentPage;
                current.setFontRenderer(currentFont);

                for (GuideChapter chapter : chapters) {
                    if (chapter.handleClick()) {
                        return;
                    }
                }

                current.handleMouseClick(page0xMin, pageYMin, page0xMax - page0xMin, pageYMax - pageYMin, mouseX,
                    mouseY, mouseButton, currentPage.getPage(), isEditing);
                current.handleMouseClick(page1xMin, pageYMin, page1xMax - page1xMin, pageYMax - pageYMin, mouseX,
                    mouseY, mouseButton, currentPage.getPage() + 1, isEditing);

                if ((!pages.isEmpty()) && BACK_POSITION.offset(minX, minY).contains(mouse)) {
                    closePage();
                }

                if (isOverHover) {
                    isEditing = !isEditing;
                    if (!isEditing) {
                        hoverStageNext = PEN_UP.height;
                    }
                }
            } else {
                if (mouseX >= minX && mouseY >= minY && mouseX <= minX + BOOK_COVER.width
                    && mouseY <= minY + BOOK_COVER.height) {
                    if (isOpening) {// So you can double-click to open it instantly
                        isOpen = true;
                    }
                    isOpening = true;
                }
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        if (currentPage.keyTyped(typedChar, keyCode)) {
            return;
        }
        if (keyCode == mc.gameSettings.keyBindLeft.getKeyCode()) {
            currentPage.lastPage();
        } else if (keyCode == mc.gameSettings.keyBindRight.getKeyCode()) {
            currentPage.nextPage();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }


    public List<String> getItemToolTip(ItemStack stack)
    {
        List<String> list = stack.getTooltip(this.mc.player, this.mc.gameSettings.advancedItemTooltips);

        for (int i = 0; i < list.size(); ++i) {
            if (i == 0) {
                list.set(i, stack.getRarity().rarityColor + list.get(i));
            } else {
                list.set(i, TextFormatting.GRAY + list.get(i));
            }
        }

        return list;
    }
}
