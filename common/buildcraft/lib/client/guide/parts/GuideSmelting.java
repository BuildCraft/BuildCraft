package buildcraft.lib.client.guide.parts;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.GuiRectangle;

public class GuideSmelting extends GuidePart {
    public static final GuiIcon SMELTING_ICON = new GuiIcon(GuiGuide.ICONS_2, 119, 54, 80, 54);
    public static final GuiRectangle OFFSET = new GuiRectangle((GuiGuide.PAGE_LEFT_TEXT.width - SMELTING_ICON.width) / 2, 0, SMELTING_ICON.width, SMELTING_ICON.height);
    public static final GuiRectangle IN_POS = new GuiRectangle(1, 1, 16, 16);
    public static final GuiRectangle OUT_POS = new GuiRectangle(59, 19, 16, 16);
    public static final GuiRectangle FURNACE_POS = new GuiRectangle(1, 37, 16, 16);

    private final ChangingItemStack input, output;
    private final ItemStack furnace;

    public GuideSmelting(GuiGuide gui, ItemStack input, ItemStack output) {
        super(gui);
        this.input = new ChangingItemStack(input);
        this.output = new ChangingItemStack(output);
        furnace = new ItemStack(Blocks.FURNACE);
    }

    @Override
    public PagePart renderIntoArea(int x, int y, int width, int height, PagePart current, int index) {
        if (current.line + 4 > height / LINE_HEIGHT) {
            current = current.newPage();
        }
        x += OFFSET.x;
        y += OFFSET.y + current.line * LINE_HEIGHT;
        if (current.page == index) {
            SMELTING_ICON.drawAt(x, y);
            // Render the item
            GlStateManager.enableRescaleNormal();
            RenderHelper.enableGUIStandardItemLighting();

            gui.mc.getRenderItem().renderItemIntoGUI(input.get(), x + IN_POS.x, y + IN_POS.y);
            if (IN_POS.offset(x, y).contains(gui.mouse)) {
                gui.tooltipStack = input.get();
            }

            gui.mc.getRenderItem().renderItemIntoGUI(output.get(), x + OUT_POS.x, y + OUT_POS.y);
            if (OUT_POS.offset(x, y).contains(gui.mouse)) {
                gui.tooltipStack = output.get();
            }

            gui.mc.getRenderItem().renderItemIntoGUI(furnace, x + FURNACE_POS.x, y + FURNACE_POS.y);
            if (FURNACE_POS.offset(x, y).contains(gui.mouse)) {
                gui.tooltipStack = furnace;
            }

            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
        }
        current = current.nextLine(4, height / LINE_HEIGHT);
        return current;
    }

}
