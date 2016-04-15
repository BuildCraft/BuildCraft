package buildcraft.lib.guide;

import java.io.IOException;
import java.util.Deque;

import com.google.common.base.Throwables;
import com.google.common.collect.Queues;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import buildcraft.core.lib.gui.GuiTexture.GuiIcon;
import buildcraft.core.lib.gui.GuiTexture.Rectangle;
import buildcraft.lib.guide.font.FontManager;
import buildcraft.lib.guide.font.IFontRenderer;
import buildcraft.lib.guide.parts.GuidePageBase;

public class GuiGuide extends GuiScreen {
    public static final ResourceLocation ICONS = new ResourceLocation("buildcraftcore:textures/gui/guide/icons.png");
    public static final ResourceLocation COVER = new ResourceLocation("buildcraftcore:textures/gui/guide/cover.png");
    public static final ResourceLocation LEFT_PAGE = new ResourceLocation("buildcraftcore:textures/gui/guide/left_page.png");
    public static final ResourceLocation RIGHT_PAGE = new ResourceLocation("buildcraftcore:textures/gui/guide/right_page.png");

    public static final GuiIcon BOOK_COVER = new GuiIcon(COVER, 0, 0, 202, 248);
    public static final GuiIcon BOOK_BINDING = new GuiIcon(COVER, 204, 0, 11, 248);

    public static final GuiIcon PAGE_LEFT = new GuiIcon(LEFT_PAGE, 0, 0, 193, 248);
    public static final GuiIcon PAGE_RIGHT = new GuiIcon(RIGHT_PAGE, 0, 0, 193, 248);

    public static final Rectangle PAGE_LEFT_TEXT = new Rectangle(31, 22, 141, 193);
    public static final Rectangle PAGE_RIGHT_TEXT = new Rectangle(20, 22, 141, 193);

    public static final GuiIcon PEN_UP = new GuiIcon(ICONS, 0, 0, 17, 135);
    public static final GuiIcon PEN_ANGLED = new GuiIcon(ICONS, 17, 0, 100, 100);

    public static final GuiIcon PEN_HIDDEN_MIN = new GuiIcon(ICONS, 0, 4, 10, 5);
    public static final GuiIcon PEN_HIDDEN_MAX = new GuiIcon(ICONS, 0, 4, 10, 15);

    public static final GuiIcon TURN_BACK = new GuiIcon(ICONS, 0, 152, 18, 10);
    public static final GuiIcon TURN_BACK_HOVERED = new GuiIcon(ICONS, 23, 152, 18, 10);

    public static final GuiIcon TURN_FORWARDS = new GuiIcon(ICONS, 0, 139, 18, 10);
    public static final GuiIcon TURN_FORWARDS_HOVERED = new GuiIcon(ICONS, 23, 139, 18, 10);

    public static final GuiIcon BACK = new GuiIcon(ICONS, 48, 139, 17, 9);
    public static final GuiIcon BACK_HOVERED = new GuiIcon(ICONS, 48, 152, 17, 9);

    public static final GuiIcon BOX_EMPTY = new GuiIcon(ICONS, 0, 164, 16, 16);
    public static final GuiIcon BOX_MINUS = new GuiIcon(ICONS, 16, 164, 16, 16);
    public static final GuiIcon BOX_PLUS = new GuiIcon(ICONS, 32, 164, 16, 16);
    public static final GuiIcon BOX_TICKED = new GuiIcon(ICONS, 48, 164, 16, 16);

    public static final GuiIcon BOX_SELECTED_EMPTY = new GuiIcon(ICONS, 0, 180, 16, 16);
    public static final GuiIcon BOX_SELECTED_MINUS = new GuiIcon(ICONS, 16, 180, 16, 16);
    public static final GuiIcon BOX_SELECTED_PLUS = new GuiIcon(ICONS, 32, 180, 16, 16);
    public static final GuiIcon BOX_SELECTED_TICKED = new GuiIcon(ICONS, 48, 180, 16, 16);

    public static final GuiIcon BORDER_TOP_LEFT = new GuiIcon(ICONS, 0, 196, 13, 13);
    public static final GuiIcon BORDER_TOP_RIGHT = new GuiIcon(ICONS, 13, 196, 13, 13);
    public static final GuiIcon BORDER_BOTTOM_LEFT = new GuiIcon(ICONS, 0, 209, 13, 13);
    public static final GuiIcon BORDER_BOTTOM_RIGHT = new GuiIcon(ICONS, 13, 209, 13, 13);

    public static final Rectangle BACK_POSITION = new Rectangle(PAGE_LEFT.x + PAGE_LEFT.width - BACK.width / 2, PAGE_LEFT.y + PAGE_LEFT.height - BACK.height - 2, BACK.width, BACK.height);

    // REMOVE FROM HERE...
    // TODO: Book cover texture
    private static final int BOOK_DOUBLE_WIDTH = 386, BOOK_DOUBLE_HEIGHT = 248;

    private static final int PEN_HIDDEN_Y = 0, PEN_HIDDEN_X = 4, PEN_HIDDEN_WIDTH = 10;
    private static final int PEN_HIDDEN_HEIGHT_MIN = 5, PEN_HIDDEN_HEIGHT_MAX = 15;

    // TO HERE

    private static final int PEN_HIDDEN_BOX_X_MIN = PAGE_LEFT.width - PEN_HIDDEN_WIDTH / 2;
    private static final int PEN_HIDDEN_BOX_Y_MIN = -PEN_HIDDEN_HEIGHT_MAX;
    private static final int PEN_HIDDEN_BOX_X_MAX = PAGE_LEFT.width + PEN_HIDDEN_WIDTH / 2;
    private static final int PEN_HIDDEN_BOX_Y_MAX = 0;

    private static final float PEN_HOVER_TIME = 0.5f;
    private static final float BOOK_OPEN_TIME = 2f;

    private boolean isOpen = false, isEditing = false;
    private boolean isOpening = false;

    /** Float between -90 and 90} */
    private float openingAngle = -90;

    /** Float between {@link #PEN_HIDDEN_HEIGHT_MIN} and {@link #PEN_HIDDEN_HEIGHT_MAX} */
    private float hoverStage = 0;
    private boolean isOverHover = false;

    /** How long since the last {@link #drawScreen(int, int, float)} was called in seconds */
    private float diff = 0;
    private int minX, minY;
    /** The current mouse positions. Used by the GuideFontRenderer */
    public int mouseX, mouseY;
    public ItemStack tooltipStack = null;

    private Deque<GuidePageBase> pages = Queues.newArrayDeque();
    private GuidePageBase currentPage;
    private IFontRenderer currentFont = FontManager.INSTANCE.getOrLoadFont("DejaVu:13");

    public GuiGuide() {
        openPage(new GuideMenu(this));
    }

    public void openPage(GuidePageBase page) {
        if (currentPage != null) {
            pages.push(currentPage);
        }
        currentPage = page;
    }

    public void closePage() {
        if (pages.isEmpty()) {
            mc.displayGuiScreen(null);
        } else {
            currentPage = pages.pop();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        diff = partialTicks / 20f;
        minX = (width - BOOK_DOUBLE_WIDTH) / 2;
        minY = (height - BOOK_DOUBLE_HEIGHT) / 2;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        try {
            if (isOpen) {
                drawOpen();
            } else if (isOpening) {
                drawOpening();
            } else {
                drawCover();
            }
        } catch (Throwable t) {
            // Temporary fix for crash report classes crashing so we can see the ACTUAL error
            t.printStackTrace();
            throw Throwables.propagate(t);
        }
    }

    public void drawTooltip(ItemStack stack, int x, int y) {
        renderToolTip(stack, x, y);
    }

    private void drawCover() {
        minX = (width - BOOK_COVER.width) / 2;
        minY = (height - BOOK_COVER.height) / 2;

        mc.renderEngine.bindTexture(COVER);
        BOOK_COVER.draw(minX, minY);
    }

    private void drawOpening() {
        minX = (width - BOOK_COVER.width) / 2;
        minY = (height - BOOK_COVER.height) / 2;

        openingAngle += (diff / BOOK_OPEN_TIME) * 180;
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
            int bindingWidth = (int) (sin * BOOK_BINDING.width);

            mc.renderEngine.bindTexture(RIGHT_PAGE);
            PAGE_RIGHT.draw(minX + BOOK_COVER.width - PAGE_RIGHT.width, minY);

            mc.renderEngine.bindTexture(COVER);
            BOOK_COVER.drawScaled(minX, minY, coverWidth, BOOK_COVER.height);

            BOOK_BINDING.drawScaled(minX + coverWidth, minY, bindingWidth, BOOK_BINDING.height);

        } else if (openingAngle == 0) {
            minX = (width - BOOK_COVER.width) / 2;
            minY = (height - BOOK_COVER.height) / 2;

            mc.renderEngine.bindTexture(RIGHT_PAGE);
            PAGE_RIGHT.draw(minX + BOOK_COVER.width - PAGE_LEFT.width, minY);

            mc.renderEngine.bindTexture(COVER);
            BOOK_COVER.draw(minX, minY);
        } else if (openingAngle > 0) {
            int pageWidth = (int) (sin * PAGE_LEFT.width);
            int bindingWidth = (int) ((1 - sin) * BOOK_BINDING.width);

            int penHeight = (int) (sin * PEN_HIDDEN_HEIGHT_MIN);

            minX = (width - BOOK_COVER.width - pageWidth) / 2;
            minY = (height - BOOK_COVER.height) / 2;

            mc.renderEngine.bindTexture(RIGHT_PAGE);
            PAGE_RIGHT.draw(minX + pageWidth + bindingWidth, minY);

            mc.renderEngine.bindTexture(LEFT_PAGE);
            PAGE_LEFT.drawScaled(minX + bindingWidth, minY, pageWidth, PAGE_LEFT.height);

            mc.renderEngine.bindTexture(COVER);
            BOOK_BINDING.drawScaled(minX, minY, bindingWidth, BOOK_BINDING.height);

            mc.renderEngine.bindTexture(ICONS);
            drawTexturedModalRect(minX + pageWidth + bindingWidth - (PEN_HIDDEN_WIDTH / 2), minY - penHeight, PEN_HIDDEN_X, PEN_HIDDEN_Y, PEN_HIDDEN_WIDTH, penHeight);
        }
    }

    private void drawOpen() {
        // Draw the pages
        mc.renderEngine.bindTexture(LEFT_PAGE);
        PAGE_LEFT.draw(minX, minY);

        mc.renderEngine.bindTexture(RIGHT_PAGE);
        PAGE_RIGHT.draw(minX + PAGE_LEFT.width, minY);

        isOverHover = mouseX >= minX + PEN_HIDDEN_BOX_X_MIN && mouseX <= minX + PEN_HIDDEN_BOX_X_MAX && mouseY >= minY + PEN_HIDDEN_BOX_Y_MIN && mouseY <= minY + PEN_HIDDEN_BOX_Y_MAX;

        // Now draw the actual contents of the book
        tooltipStack = null;
        currentPage.setSpecifics(currentFont, mouseX, mouseY);
        currentPage.tick(diff);
        currentPage.renderFirstPage(minX + PAGE_LEFT_TEXT.x, minY + PAGE_LEFT_TEXT.y, PAGE_LEFT_TEXT.width, PAGE_LEFT_TEXT.height);
        currentPage.renderSecondPage(minX + PAGE_LEFT.width + PAGE_RIGHT_TEXT.x, minY + PAGE_RIGHT_TEXT.y, PAGE_RIGHT_TEXT.width, PAGE_RIGHT_TEXT.height);

        // Draw the back button if there are any pages on the stack
        if (!pages.isEmpty()) {
            GuiIcon icon = BACK;
            int xStart = minX + BACK_POSITION.x;
            int yStart = minY + BACK_POSITION.y;
            if (icon.isMouseInside(xStart, yStart, mouseX, mouseY)) {
                icon = BACK_HOVERED;
            }
            icon.draw(xStart, yStart);
        }

        // Reset the colour for the pen
        GlStateManager.color(1, 1, 1);

        // Draw the pen
        if (isEditing) {
            mc.renderEngine.bindTexture(ICONS);

            if (isOverHover) {
                PEN_UP.draw(mouseX - PEN_UP.width / 2, mouseY - PEN_UP.height);
            } else {
                PEN_ANGLED.draw(mouseX - 2, mouseY - PEN_ANGLED.height - 2);
            }
        } else {
            // Calculate pen hover position
            float hoverDiff = (diff / PEN_HOVER_TIME) * (PEN_HIDDEN_HEIGHT_MAX - PEN_HIDDEN_HEIGHT_MIN);
            if (hoverStage > PEN_HIDDEN_HEIGHT_MAX) {
                hoverStage -= hoverDiff * 5;
            } else if (isOverHover) {
                hoverStage += hoverDiff;
                if (hoverStage > PEN_HIDDEN_HEIGHT_MAX) {
                    hoverStage = PEN_HIDDEN_HEIGHT_MAX;
                }
            } else {
                if (hoverStage > PEN_HIDDEN_HEIGHT_MIN) {
                    hoverStage -= hoverDiff;
                }
                if (hoverStage < PEN_HIDDEN_HEIGHT_MIN) {
                    hoverStage = PEN_HIDDEN_HEIGHT_MIN;
                }
            }
            int height = (int) hoverStage;

            // Draw pen
            mc.renderEngine.bindTexture(ICONS);
            drawTexturedModalRect(minX + PAGE_LEFT.width - PEN_HIDDEN_WIDTH / 2, minY - height, PEN_HIDDEN_X, PEN_HIDDEN_Y, PEN_HIDDEN_WIDTH, height);

            if (tooltipStack != null) {
                renderToolTip(tooltipStack, mouseX, mouseY);
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        // Primary mouse button
        if (mouseButton == 0) {
            if (isOpen) {
                int page0xMin = this.minX + PAGE_LEFT_TEXT.x;
                int page0xMax = page0xMin + PAGE_LEFT_TEXT.width;
                int page1xMin = this.minX + PAGE_LEFT.width + PAGE_RIGHT_TEXT.x;
                int page1xMax = page1xMin + PAGE_RIGHT_TEXT.width;
                int pageYMin = this.minY + PAGE_RIGHT_TEXT.y;
                int pageYMax = pageYMin + PAGE_RIGHT_TEXT.height;

                currentPage.handleMouseClick(page0xMin, pageYMin, page0xMax - page0xMin, pageYMax - pageYMin, mouseX, mouseY, mouseButton, currentPage.getPage(), isEditing);
                currentPage.handleMouseClick(page1xMin, pageYMin, page1xMax - page1xMin, pageYMax - pageYMin, mouseX, mouseY, mouseButton, currentPage.getPage() + 1, isEditing);

                if ((!pages.isEmpty()) && BACK_POSITION.isMouseInside(minX + BACK_POSITION.x, minY + BACK_POSITION.y, mouseX, mouseY)) {
                    closePage();
                }

                if (isOverHover) {
                    isEditing = !isEditing;
                    if (!isEditing) {
                        hoverStage = PEN_UP.height;
                    }
                }
            } else {
                if (mouseX >= minX && mouseY >= minY && mouseX <= minX + BOOK_COVER.width && mouseY <= minY + BOOK_COVER.height) {
                    if (isOpening) {// So you can double-click to open it instantly
                        isOpen = true;
                    }
                    isOpening = true;
                }
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}
