/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide;

import buildcraft.lib.BCLibItems;
import buildcraft.lib.client.ToastInformation;
import buildcraft.lib.client.guide.font.FontManager;
import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.client.guide.parts.GuideChapter;
import buildcraft.lib.client.guide.parts.GuidePageBase;
import buildcraft.lib.client.guide.parts.contents.GuidePageContents;
import buildcraft.lib.client.sprite.SpriteNineSliced;
import buildcraft.lib.client.sprite.SpriteRaw;
import buildcraft.lib.container.ContainerGuide;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.GuiStack;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.MousePosition;
import buildcraft.lib.guide.GuideBook;
import buildcraft.lib.guide.GuideBookRegistry;
import buildcraft.lib.guide.GuideContentsData;
import buildcraft.lib.misc.GuiUtil;
import buildcraft.lib.misc.GuiUtil.AutoGlScissor;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.RenderUtil;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

//public class GuiGuide extends GuiScreen
public class GuiGuide extends Screen implements MenuAccess<ContainerGuide> {
    public static final ResourceLocation ICONS_1 = Gui.GUI_ICONS_LOCATION;
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
    public static final ResourceLocation LEFT_PAGE_FIRST =
            new ResourceLocation("buildcraftlib:textures/gui/guide/left_page_first.png");
    public static final ResourceLocation RIGHT_PAGE_LAST =
            new ResourceLocation("buildcraftlib:textures/gui/guide/right_page_last.png");
    public static final ResourceLocation NOTE = new ResourceLocation("buildcraftlib:textures/gui/guide/note.png");

    public static final GuiIcon BOOK_COVER = new GuiIcon(COVER, 0, 0, 202, 248);
    public static final GuiIcon BOOK_BINDING = new GuiIcon(COVER, 204, 0, 11, 248);

    public static final GuiIcon PAGE_LEFT = new GuiIcon(LEFT_PAGE, 0, 0, 193, 248);
    public static final GuiIcon PAGE_RIGHT = new GuiIcon(RIGHT_PAGE, 0, 0, 193, 248);

    public static final GuiIcon PAGE_LEFT_BACK = new GuiIcon(LEFT_PAGE_BACK, 0, 0, 193, 248);
    public static final GuiIcon PAGE_RIGHT_BACK = new GuiIcon(RIGHT_PAGE_BACK, 0, 0, 193, 248);

    public static final GuiIcon PAGE_LEFT_FIRST = new GuiIcon(LEFT_PAGE_FIRST, 0, 0, 193, 248);
    public static final GuiIcon PAGE_RIGHT_LAST = new GuiIcon(RIGHT_PAGE_LAST, 0, 0, 193, 248);

    public static final int PAGE_WIDTH = 168;
    public static final int PAGE_HEIGHT = 190;

    public static final GuiRectangle PAGE_LEFT_TEXT = new GuiRectangle(23, 25, PAGE_WIDTH, PAGE_HEIGHT);
    public static final GuiRectangle PAGE_RIGHT_TEXT = new GuiRectangle(4, 25, PAGE_WIDTH, PAGE_HEIGHT);

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

    public static final SpriteRaw BOX_CODE_SPRITE = new SpriteRaw(ICONS_2, 80, 164, 16, 16, 256);
    public static final GuiIcon BOX_CODE = new GuiIcon(BOX_CODE_SPRITE, 256);
    public static final SpriteNineSliced BOX_CODE_SLICED = new SpriteNineSliced(BOX_CODE_SPRITE, 4, 4, 12, 12, 16);

    public static final GuiIcon BORDER_TOP_LEFT = new GuiIcon(ICONS_2, 0, 196, 13, 13);
    public static final GuiIcon BORDER_TOP_RIGHT = new GuiIcon(ICONS_2, 13, 196, 13, 13);
    public static final GuiIcon BORDER_BOTTOM_LEFT = new GuiIcon(ICONS_2, 0, 209, 13, 13);
    public static final GuiIcon BORDER_BOTTOM_RIGHT = new GuiIcon(ICONS_2, 13, 209, 13, 13);

    public static final GuiIcon ORDER_TYPE = new GuiIcon(ICONS_2, 0, 0, 14, 14);
    public static final GuiIcon ORDER_MOD_TYPE = new GuiIcon(ICONS_2, 14, 0, 14, 14);
    public static final GuiIcon ORDER_ALPHABETICAL = new GuiIcon(ICONS_2, 28, 0, 14, 14);

    public static final GuiIcon EXPANDED_ARROW = new GuiIcon(ICONS_2, 96, 164, 16, 16);
    public static final GuiIcon CLOSED_ARROW = new GuiIcon(ICONS_2, 96, 180, 16, 16);

    public static final GuiIcon CHAPTER_MARKER = new GuiIcon(ICONS_2, 0, 56, 32, 32);
    public static final GuiIcon CHAPTER_MARKER_LEFT = new GuiIcon(ICONS_2, 0, 56, 24, 32);
    public static final GuiIcon CHAPTER_MARKER_RIGHT = new GuiIcon(ICONS_2, 8, 56, 24, 32);

    public static final SpriteNineSliced CHAPTER_MARKER_9;
    public static final SpriteNineSliced CHAPTER_MARKER_9_LEFT;
    public static final SpriteNineSliced CHAPTER_MARKER_9_RIGHT;

    public static final GuiIcon NOTE_PAGE = new GuiIcon(NOTE, 0, 0, 131, 164);
    public static final GuiIcon NOTE_UNDERLAY = new GuiIcon(ICONS_2, 0, 1, 3, 4);
    public static final GuiIcon NOTE_OVERLAY = new GuiIcon(ICONS_2, 0, 1, 2, 3);

    public static final GuiIcon SEARCH_ICON = new GuiIcon(ICONS_2, 26, 196, 12, 12);
    public static final GuiIcon SEARCH_TAB_CLOSED = new GuiIcon(ICONS_2, 58, 196, 14, 6);
    public static final GuiIcon SEARCH_TAB_OPEN = new GuiIcon(ICONS_2, 40, 209, 106, 14);

    public static final GuiIcon[] ORDERS = {ORDER_TYPE, ORDER_MOD_TYPE, ORDER_ALPHABETICAL};

    public static final GuiRectangle BACK_POSITION = new GuiRectangle(
            PAGE_LEFT.width - BACK.width / 2, PAGE_LEFT.height - BACK.height - 2, BACK.width, BACK.height
    );

    public static final TypeOrder[] SORTING_TYPES = { //
            new TypeOrder("buildcraft.guide.order.type_subtype", ETypeTag.TYPE, ETypeTag.SUB_TYPE), //
            new TypeOrder("buildcraft.guide.order.mod_type", ETypeTag.MOD, ETypeTag.TYPE), //
            new TypeOrder("buildcraft.guide.order.alphabetical")//
    };

    public static final IGuiArea FLOATING_CHAPTER_MENU;

    private static final float BOOK_OPEN_TIME = 10f; // 20

    static {
        CHAPTER_MARKER_9 = new SpriteNineSliced(CHAPTER_MARKER.sprite, 8, 8, 24, 24, 32);
        CHAPTER_MARKER_9_LEFT = new SpriteNineSliced(CHAPTER_MARKER_LEFT.sprite, 8, 8, 24, 24, 24, 32);
        CHAPTER_MARKER_9_RIGHT = new SpriteNineSliced(CHAPTER_MARKER_RIGHT.sprite, 0, 8, 16, 24, 24, 32);

        FLOATING_CHAPTER_MENU = GuiUtil.moveRectangleToCentre(
                new GuiRectangle((PAGE_LEFT_TEXT.width + PAGE_RIGHT_TEXT.width) / 2, PAGE_LEFT.height - 20)
        );
    }

    public final MousePosition mouse = new MousePosition();

    public final GuideBook book;
    public final GuideContentsData bookData;

    public TypeOrder sortingOrder = SORTING_TYPES[0];
    private boolean isOpen = false, isEditing = false;
    private boolean isOpening = false;
    private boolean showingContentsMenu = false;

    /** Float between -90 and 90} */
    private float openingAngleLast = -90, openingAngleNext = -90;

    public int minX, minY;
    public ItemStack tooltipStack = null;
    public final List<List<Component>> tooltips = new ArrayList<>();

    private final Deque<GuidePageBase> pages = Queues.newArrayDeque();
    private final List<GuideChapter> chapters = new ArrayList<>();
    private GuidePageBase currentPage;
    private IFontRenderer currentFont = FontManager.INSTANCE.getOrLoadFont("SansSerif", 9);
    private float lastPartialTicks;

    // public GuiGuide()
    public GuiGuide(ContainerGuide container, Component component) {
        this(container, (GuideBook) null, component);
    }

    // public GuiGuide(String bookName)
    public GuiGuide(ContainerGuide container, String bookName, Component component) {
        this(container, GuideBookRegistry.INSTANCE.getBook(bookName), component);
    }

    // private GuiGuide(@Nullable GuideBook book)
    private GuiGuide(ContainerGuide container, @Nullable GuideBook book, Component component) {
        super(component);
        // Calen
        this.menu = container;

        this.book = book;
        this.bookData = book != null ? book.data : GuideManager.BOOK_ALL_DATA;
//        mc = Minecraft.getMinecraft(); // Calen: in Screen#init()
        openPage(new GuidePageContents(this));
    }

    public void openPage(GuidePageBase page) {
        if (currentPage != null && currentPage.shouldPersistHistory()) {
            pages.push(currentPage);
        }
        setPageInternal(page);
    }

    public void closePage() {
        if (pages.isEmpty()) {
            minecraft.setScreen(null);
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

    public List<GuideChapter> getChapters() {
        return chapters;
    }

    public void refreshChapters() {
        chapters.clear();
        chapters.addAll(currentPage.getChapters());
    }

    @Override
//    public void updateScreen()
    public void tick() {
//        super.updateScreen();
        super.tick();
        // Calen: from AbstractContainerScreen
        if (!(this.minecraft.player.isAlive()) || this.minecraft.player.isRemoved()) {
            this.minecraft.player.closeContainer();
            return;
        }
        // BC 1.12.2
        if (isOpen) {
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

    public boolean isSmallScreen() {
//        return new ScaledResolution(minecraft).getScaledWidth() < 590;
        return minecraft.getWindow().getWidth() < 590;
    }

    @Override
//    public void drawScreen(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        lastPartialTicks = partialTicks = minecraft.getFrameTime();
        minX = (this.width - PAGE_LEFT.width * 2) / 2;
        minY = (this.height - BOOK_COVER.height) / 2;
        mouse.setMousePosition(mouseX, mouseY);
        try {
            if (isOpen) {
                drawOpen(guiGraphics, partialTicks);
            } else if (isOpening) {
                drawOpening(partialTicks, guiGraphics);
            } else {
                drawCover(guiGraphics);
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

    public void drawTooltip(GuiGraphics guiGraphics, ItemStack stack, int x, int y) {
//        renderToolTip(stack, x, y);
        guiGraphics.renderTooltip(font, stack, x, y);
    }

    private void drawCover(GuiGraphics guiGraphics) {
        minX = (width - BOOK_COVER.width) / 2;
        minY = (height - BOOK_COVER.height) / 2;

        minecraft.textureManager.bindForSetup(COVER);
        BOOK_COVER.drawAt(guiGraphics, minX, minY);
    }

    private void drawOpening(float partialTicks, GuiGraphics guiGraphics) {
        minX = (width - BOOK_COVER.width) / 2;
        minY = (height - BOOK_COVER.height) / 2;

        float openingAngle = openingAngleLast * (1 - partialTicks) + openingAngleNext * partialTicks;
//        float sin = MathHelper.sin((float) (openingAngle * Math.PI / 180));
        float sin = Mth.sin((float) (openingAngle * Math.PI / 180));
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

            minecraft.textureManager.bindForSetup(RIGHT_PAGE);
            PAGE_RIGHT.drawAt(guiGraphics, minX + BOOK_COVER.width - PAGE_RIGHT.width, minY);

            minecraft.textureManager.bindForSetup(COVER);

            // BOOK_COVER.drawScaledInside(minX, minY, coverWidth, BOOK_COVER.height);
            // BOOK_COVER.drawCustomQuad(
            // minX, minY + BOOK_COVER.height,
            // minX + coverWidth, minY + BOOK_COVER.height,
            // minX + coverWidth, minY,
            // minX, minY
            // ); // like drawScaledInside, but using drawCustomQuad
            BOOK_COVER.drawCustomQuad(
                    guiGraphics,
                    minX, minY + BOOK_COVER.height, minX + coverWidth, minY + BOOK_COVER.height + offset, minX + coverWidth,
                    minY - offset, minX, minY
            );

            BOOK_BINDING.drawScaledInside(
                    guiGraphics,
                    (int) (minX + coverWidth - bindingWidth * 0.5), (int) (minY - offset), bindingWidth,
                    (int) (BOOK_BINDING.height + offset * 2)
            );

        } else if (openingAngle == 0) {
            minX = (width - BOOK_COVER.width) / 2;
            minY = (height - BOOK_COVER.height) / 2;

            minecraft.textureManager.bindForSetup(RIGHT_PAGE);
            PAGE_RIGHT.drawAt(guiGraphics, minX + BOOK_COVER.width - PAGE_LEFT.width, minY);

            minecraft.textureManager.bindForSetup(COVER);
            BOOK_COVER.drawAt(guiGraphics, minX, minY);
        } else if (openingAngle > 0) {
            int pageWidth = (int) (sin * PAGE_LEFT.width);
            int bindingWidth = (int) ((1 - sin) * BOOK_BINDING.width);

            float offset = (1 - sin) * 50;

            minX = (width - PAGE_LEFT.width - pageWidth) / 2;
            minY = (height - BOOK_COVER.height) / 2;

            minecraft.textureManager.bindForSetup(RIGHT_PAGE);
            PAGE_RIGHT.drawAt(guiGraphics, minX + pageWidth + bindingWidth, minY);

            minecraft.textureManager.bindForSetup(LEFT_PAGE);
            // PAGE_LEFT.drawCustomQuad(
            // minX + bindingWidth, minY + PAGE_LEFT.height + offset,
            // minX + bindingWidth + pageWidth, minY + PAGE_LEFT.height,
            // minX + bindingWidth + pageWidth, minY,
            // minX + bindingWidth, minY - offset
            // );
            PAGE_LEFT.drawCustomQuad(
                    guiGraphics,
                    minX + bindingWidth, minY + PAGE_LEFT.height + offset, minX + bindingWidth + pageWidth, minY
                            + PAGE_LEFT.height, minX + bindingWidth + pageWidth, minY, minX + bindingWidth, minY - offset
            );
            // PAGE_LEFT.drawScaledInside(minX + bindingWidth, minY, pageWidth, PAGE_LEFT.height);

            minecraft.textureManager.bindForSetup(COVER);
            BOOK_BINDING.drawScaledInside(
                    guiGraphics,
                    (int) (minX + bindingWidth * 0.5), (int) (minY - offset), bindingWidth, (int) (BOOK_BINDING.height
                            + offset * 2)
            );

            minecraft.textureManager.bindForSetup(ICONS_2);
        }
    }

    private void drawOpen(GuiGraphics guiGraphics, float partialTicks) {

        int cp = currentPage.getPage();
        int pc = currentPage.getPageCount();
        boolean isHalfPageShown = cp + 1 == pc;
        {
            (cp == 0 ? PAGE_LEFT_FIRST : PAGE_LEFT).drawAt(guiGraphics, minX, minY);
            final GuiIcon lastPageIcon;
            if (cp + 2 == pc) {
                lastPageIcon = PAGE_RIGHT_LAST;
            } else if (isHalfPageShown) {
                lastPageIcon = PAGE_RIGHT_BACK;
            } else {
                lastPageIcon = PAGE_RIGHT;
            }

            lastPageIcon.drawAt(guiGraphics, minX + PAGE_LEFT.width, minY);
        }

        // Now draw the actual contents of the book
//        String title = currentPage.getTitle();
        Component title = currentPage.getTitle();
        if (title != null) {
            final int x;
//            int titleWidth = currentFont.getStringWidth(title);
            int titleWidth = currentFont.getStringWidth(title.getString());
            if (isHalfPageShown) {
                x = (int) (minX + PAGE_LEFT_TEXT.x + (PAGE_LEFT_TEXT.width - titleWidth) / 2);
            } else {
                x = (width - titleWidth) / 2;
            }
//            currentFont.drawString(poseStack, title, x, minY + 12, 0x90816a);
            currentFont.drawString(guiGraphics, title.getString(), x, minY + 12, 0x90816a);
        }

        tooltipStack = null;
        tooltips.clear();
        setupFontRenderer();
        for (GuideChapter chapter : chapters) {
            chapter.reset();
        }

        currentPage.renderFirstPage(
                guiGraphics,
                minX + (int) PAGE_LEFT_TEXT.x, minY + (int) PAGE_LEFT_TEXT.y, (int) PAGE_LEFT_TEXT.width,
                (int) PAGE_LEFT_TEXT.height
        );
        int secondPageX = minX + PAGE_LEFT.width + (int) PAGE_RIGHT_TEXT.x;
        if (!isHalfPageShown) {
            currentPage.renderSecondPage(
                    guiGraphics,
                    secondPageX, minY + (int) PAGE_RIGHT_TEXT.y, (int) PAGE_RIGHT_TEXT.width, (int) PAGE_RIGHT_TEXT.height
            );
        }

        boolean drawContents = true;
        boolean smallScreen = isSmallScreen();
        if (smallScreen) {
            drawContents = showingContentsMenu;

            String str = LocaleUtil.localize("buildcraft.guide.chapter_list");
            if (showingContentsMenu) {
                CHAPTER_MARKER_9.draw(FLOATING_CHAPTER_MENU, guiGraphics);
                currentFont.drawString(
                        guiGraphics,
                        str,
                        (int) FLOATING_CHAPTER_MENU.getX() + 7,
                        (int) FLOATING_CHAPTER_MENU.getY() + 7,
                        0
                );
            } else {
                boolean isHovered = new GuiRectangle(secondPageX, minY, 80, 10).contains(mouse);
                int hoverOffset = isHovered ? -5 : 0;
                int y = minY + hoverOffset;

                int strWidth = currentFont.getStringWidth(str);
                try (AutoGlScissor scissor = GuiUtil.scissor(secondPageX, 0, strWidth + 20, minY + 10)) {
                    CHAPTER_MARKER_9.draw(guiGraphics, secondPageX, y, strWidth + 20, 100);
                    currentFont.drawString(guiGraphics, str, secondPageX + 10, y + 3, 0);
                }
            }
        }

        if (drawContents) {
            int chapterIndex = 0;
            for (GuideChapter chapter : chapters) {
                if (chapter.hasParent()) {
                    continue;
                }
                chapterIndex += chapter.draw(guiGraphics, chapterIndex, partialTicks, smallScreen);
            }
        }

        // Draw the back button if there are any pages on the stack
        if (!pages.isEmpty()) {
            GuiIcon icon = BACK;
            IGuiArea position = BACK_POSITION.offset(minX, minY);
            if (position.contains(mouse)) {
                icon = BACK_HOVERED;
            }
            icon.drawAt(position, guiGraphics);
        }

        // Reset the colour
//        GlStateManager.color(1, 1, 1);
        RenderUtil.color(1, 1, 1);
        if (tooltipStack != null) {
            drawTooltip(guiGraphics, tooltipStack, (int) mouse.getX(), (int) mouse.getY());
        } else if (!tooltips.isEmpty()) {
            // Calen: tooltipStack == null
            int y = (int) mouse.getY();
            for (List<Component> tooltip : tooltips) {
//                drawHoveringText(tooltip, (int) mouse.getX(), y);
                guiGraphics.renderTooltip(font, tooltip, Optional.empty(), (int) mouse.getX(), y);
                y += tooltip.size() * font.lineHeight + 10;
            }
        }
    }

    public void setupFontRenderer() {
        currentPage.setFontRenderer(currentFont);
    }

    @Override
//    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
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
                    int clickResult = chapter.handleClick();
                    if (clickResult > 0) {
                        if (showingContentsMenu && clickResult == 1) {
                            showingContentsMenu = false;
                        }
//                        return;
                        return true;
                    }
                }

                if (isSmallScreen()) {
                    if (showingContentsMenu) {
                        double menuWidth = (PAGE_LEFT_TEXT.width + PAGE_RIGHT_TEXT.width) / 2;
                        double menuHeight = PAGE_LEFT.height - 20;
                        IGuiArea menuRect = GuiUtil.moveRectangleToCentre(new GuiRectangle(menuWidth, menuHeight));
                        if (!menuRect.contains(mouse)) {
                            showingContentsMenu = false;
//                        return;
                            return true;
                        }
//                        return;
                        return true;
                    } else {
                        int secondPageX = minX + PAGE_LEFT.width + (int) PAGE_RIGHT_TEXT.x;
                        if (new GuiRectangle(secondPageX, minY, 80, 10).contains(mouse)) {
                            showingContentsMenu = true;
//                        return;
                            return true;
                        }
                    }
                }

                GuiGraphics guiGraphics = new GuiGraphics(Minecraft.getInstance(), Minecraft.getInstance().renderBuffers().bufferSource());

                current.handleMouseClick(
                        guiGraphics,
                        page0xMin, pageYMin, page0xMax - page0xMin, pageYMax - pageYMin, mouseX, mouseY, mouseButton,
                        currentPage.getPage(), isEditing
                );
                current.handleMouseClick(
                        guiGraphics,
                        page1xMin, pageYMin, page1xMax - page1xMin, pageYMax - pageYMin, mouseX, mouseY, mouseButton,
                        currentPage.getPage() + 1, isEditing
                );

                if ((!pages.isEmpty()) && BACK_POSITION.offset(minX, minY).contains(mouse)) {
                    closePage();
                }

            } else {
                if (
                        mouseX >= minX && mouseY >= minY && mouseX <= minX + BOOK_COVER.width && mouseY <= minY
                                + BOOK_COVER.height
                ) {
                    if (isOpening) {// So you can double-click to open it instantly
                        isOpen = true;
                    }
                    isOpening = true;
                }
            }
        }
        return true;
    }

    @Override
//    protected void keyTyped(char typedChar, int keyCode) throws IOException
    public boolean keyPressed(int typedChar, int keyCode, int modifiers) {
//        super.keyTyped(typedChar, keyCode);
        super.keyPressed(typedChar, keyCode, modifiers);
//        if (currentPage.keyTyped(typedChar, keyCode))
        if (currentPage.keyTyped(typedChar, keyCode, modifiers)) {
//            return;
            return true;
        }
//        if (Keyboard.isKeyDown(Keyboard.KEY_F3) && keyCode == Keyboard.KEY_T)
        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_F3) && typedChar == InputConstants.KEY_T) {
            GuideManager.INSTANCE.reload();
            while (true) {
                currentPage = currentPage.createReloaded();
                if (currentPage != null) {
                    break;
                }
                currentPage = pages.poll();
                if (currentPage == null) {
                    throw new IllegalStateException("Didn't find the contents page!");
                }
            }
            GuidePageBase[] history = pages.toArray(new GuidePageBase[0]);
            pages.clear();
            for (int i = 0; i < history.length; i++) {
                GuidePageBase page = history[i].createReloaded();
                if (page != null) {
                    pages.add(page);
                }
            }
            refreshChapters();
            ISimpleDrawable icon = null;
            if (BCLibItems.guide != null) {
                GuiStack stackIcon = new GuiStack(new ItemStack(BCLibItems.guide.get()));
                icon = (poseStack, x, y) -> stackIcon.drawAt(poseStack, x + 8, y + 8);
            }
//            minecraft.getToastGui().add(new ToastInformation("buildcraft.guide_book.reloaded", icon));
            minecraft.getToasts().addToast(new ToastInformation("buildcraft.guide_book.reloaded", icon));
        }
        if (typedChar == minecraft.options.keyLeft.getKey().getValue()) {
            currentPage.lastPage();
        } else if (typedChar == minecraft.options.keyRight.getKey().getValue()) {
            currentPage.nextPage();
        }
        return true;
    }

    public boolean charTyped(char typedChar, int keyCode) {
        super.charTyped(typedChar, keyCode);
        if (currentPage.charTyped(typedChar, keyCode)) {
//            return;
            return true;
        }
//        if (Keyboard.isKeyDown(Keyboard.KEY_F3) && keyCode == Keyboard.KEY_T)
        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_F3) && typedChar == InputConstants.KEY_T) {
            GuideManager.INSTANCE.reload();
            while (true) {
                currentPage = currentPage.createReloaded();
                if (currentPage != null) {
                    break;
                }
                currentPage = pages.poll();
                if (currentPage == null) {
                    throw new IllegalStateException("Didn't find the contents page!");
                }
            }
            GuidePageBase[] history = pages.toArray(new GuidePageBase[0]);
            pages.clear();
            for (int i = 0; i < history.length; i++) {
                GuidePageBase page = history[i].createReloaded();
                if (page != null) {
                    pages.add(page);
                }
            }
            refreshChapters();
            ISimpleDrawable icon = null;
            if (BCLibItems.guide != null) {
                GuiStack stackIcon = new GuiStack(new ItemStack(BCLibItems.guide.get()));
                icon = (poseStack, x, y) -> stackIcon.drawAt(poseStack, x + 8, y + 8);
            }
//            minecraft.getToastGui().add(new ToastInformation("buildcraft.guide_book.reloaded", icon));
            minecraft.getToasts().addToast(new ToastInformation("buildcraft.guide_book.reloaded", icon));
        }
        if (typedChar == minecraft.options.keyLeft.getKey().getValue()) {
            currentPage.lastPage();
        } else if (typedChar == minecraft.options.keyRight.getKey().getValue()) {
            currentPage.nextPage();
        }
        return true;
    }

    @Override
//    public boolean doesGuiPauseGame()
    public boolean isPauseScreen() {
        return false;
    }

    protected final ContainerGuide menu;

    @Override
    public ContainerGuide getMenu() {
        return this.menu;
    }

    @Override
    public void removed() {
        if (this.minecraft.player != null) {
            this.menu.removed(this.minecraft.player);
        }
    }

    public void onClose() {
        this.minecraft.player.closeContainer();
        super.onClose();
    }
}
