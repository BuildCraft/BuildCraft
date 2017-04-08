package buildcraft.lib.client.guide.parts.recipe;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuidePartItem;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.recipe.ChangingItemStack;
import buildcraft.lib.recipe.ChangingObject;

public class GuideAssembly extends GuidePartItem {
    public static final GuiIcon INPUT_LIST = new GuiIcon(GuiGuide.ICONS_2, 119, 108, 98, 54);
    public static final GuiRectangle[] ITEM_POSITION = new GuiRectangle[6];
    public static final GuiRectangle OUT_POSITION = new GuiRectangle(77, 19, 16, 16);
    public static final GuiRectangle MJ_POSITION = new GuiRectangle(50, 4, 6, 46);
    public static final GuiRectangle OFFSET = new GuiRectangle((GuiGuide.PAGE_LEFT_TEXT.width - INPUT_LIST.width) / 2, 0, INPUT_LIST.width, INPUT_LIST.height);
    public static final int PIXEL_HEIGHT = 60;

    static {
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 3; y++) {
                ITEM_POSITION[x + y * 2] = new GuiRectangle(1 + x * 18, 1 + y * 18, 16, 16);
            }
        }
    }

    private final ChangingItemStack[] input;
    private final ChangingItemStack output;
    private final ChangingObject<Long> mjCost;

    GuideAssembly(GuiGuide gui, ChangingItemStack[] input, ChangingItemStack output, ChangingObject<Long> mjCost) {
        super(gui);
        this.input = input;
        this.output = output;
        this.mjCost = mjCost;
    }

    @Override
    public PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition current, int index) {
        if (current.pixel + PIXEL_HEIGHT > height) {
            current = current.newPage();
        }
        x += OFFSET.x;
        y += OFFSET.y + current.pixel;
        if (current.page == index) {
            INPUT_LIST.drawAt(x, y);
            // Render the item
            GlStateManager.enableRescaleNormal();
            RenderHelper.enableGUIStandardItemLighting();
            for (int i = 0; i < input.length; i++) {
                GuiRectangle rect = ITEM_POSITION[i];
                ItemStack stack = input[i].get();
                drawItemStack(stack, x + rect.x, y + rect.y);
            }

            drawItemStack(output.get(), x + OUT_POSITION.x, y + OUT_POSITION.y);

            if (MJ_POSITION.offset(x, y).contains(gui.mouse)) {
                gui.tooltip.add(LocaleUtil.localizeMj(mjCost.get()));
            }

            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
        }
        current = current.nextLine(PIXEL_HEIGHT, height);
        return current;
    }

    @Override
    public PagePosition handleMouseClick(int x, int y, int width, int height, PagePosition current, int index, int mouseX, int mouseY) {
        if (current.pixel + PIXEL_HEIGHT > height) {
            current = current.newPage();
        }
        x += OFFSET.x;
        y += OFFSET.y + current.pixel;
        if (current.page == index) {
            for (int i = 0; i < input.length; i++) {
                GuiRectangle rect = ITEM_POSITION[i];
                ItemStack stack = input[i].get();
                testClickItemStack(stack, x + rect.x, y + rect.y);
            }

            testClickItemStack(output.get(), x + OUT_POSITION.x, y + OUT_POSITION.y);
        }
        current = current.nextLine(PIXEL_HEIGHT, height);
        return current;
    }
}
