package buildcraft.lib.client.guide.parts;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.GuiRectangle;

public class GuideSmelting extends GuidePartItem {
    public static final GuiIcon SMELTING_ICON = new GuiIcon(GuiGuide.ICONS_2, 119, 54, 80, 54);
    public static final GuiRectangle OFFSET = new GuiRectangle((GuiGuide.PAGE_LEFT_TEXT.width - SMELTING_ICON.width) / 2, 0, SMELTING_ICON.width, SMELTING_ICON.height);
    public static final GuiRectangle IN_POS = new GuiRectangle(1, 1, 16, 16);
    public static final GuiRectangle OUT_POS = new GuiRectangle(59, 19, 16, 16);
    public static final GuiRectangle FURNACE_POS = new GuiRectangle(1, 37, 16, 16);
    public static final int PIXEL_HEIGHT = 60;

    private final ChangingItemStack input, output;
    private final ItemStack furnace;

    public GuideSmelting(GuiGuide gui, ItemStack input, ItemStack output) {
        super(gui);
        this.input = new ChangingItemStack(input);
        this.output = new ChangingItemStack(output);
        furnace = new ItemStack(Blocks.FURNACE);
    }

    @Override
    public PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition current, int index) {
        if (current.pixel + PIXEL_HEIGHT > height) {
            current = current.newPage();
        }
        x += OFFSET.x;
        y += OFFSET.y + current.pixel;
        if (current.page == index) {
            SMELTING_ICON.drawAt(x, y);
            // Render the item
            GlStateManager.enableRescaleNormal();
            RenderHelper.enableGUIStandardItemLighting();

            drawItemStack(input.get(), x + IN_POS.x, y + IN_POS.y);
            drawItemStack(output.get(), x + OUT_POS.x, y + OUT_POS.y);
            drawItemStack(furnace, x + FURNACE_POS.x, y + FURNACE_POS.y);

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

            testClickItemStack(input.get(), x + IN_POS.x, y + IN_POS.y);
            testClickItemStack(output.get(), x + OUT_POS.x, y + OUT_POS.y);
            testClickItemStack(furnace, x + FURNACE_POS.x, y + FURNACE_POS.y);

        }
        current = current.nextLine(PIXEL_HEIGHT, height);
        return current;
    }
}
