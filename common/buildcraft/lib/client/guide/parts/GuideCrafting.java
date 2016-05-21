package buildcraft.lib.client.guide.parts;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.GuiRectangle;

public class GuideCrafting extends GuidePart {
    public static final GuiIcon CRAFTING_GRID = new GuiIcon(GuiGuide.ICONS, 119, 0, 116, 54);
    public static final GuiRectangle[][] ITEM_POSITION = new GuiRectangle[3][3];
    public static final GuiRectangle OUT_POSITION = new GuiRectangle(95, 19, 16, 16);
    public static final GuiRectangle OFFSET = new GuiRectangle((GuiGuide.PAGE_LEFT_TEXT.width - CRAFTING_GRID.width) / 2, 0, CRAFTING_GRID.width, CRAFTING_GRID.height);

    static {
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                ITEM_POSITION[x][y] = new GuiRectangle(1 + x * 18, 1 + y * 18, 16, 16);
            }
        }
    }

    private final ChangingItemStack[][] input;
    private final ChangingItemStack output;

    GuideCrafting(GuiGuide gui, ItemStack[][] input, ItemStack output) {
        super(gui);
        this.input = new ChangingItemStack[input.length][];
        for (int x = 0; x < input.length; x++) {
            this.input[x] = new ChangingItemStack[input[x].length];
            for (int y = 0; y < input[x].length; y++) {
                this.input[x][y] = new ChangingItemStack(input[x][y]);
            }
        }
        this.output = new ChangingItemStack(output);
    }

    @Override
    public PagePart renderIntoArea(int x, int y, int width, int height, PagePart current, int index) {
        if (current.line + 4 > height / LINE_HEIGHT) {
            current = current.newPage();
        }
        x += OFFSET.x;
        y += OFFSET.y + current.line * LINE_HEIGHT;
        if (current.page == index) {
            CRAFTING_GRID.drawAt(x, y);
            // Render the item
            GlStateManager.enableRescaleNormal();
            RenderHelper.enableGUIStandardItemLighting();
            for (int itemX = 0; itemX < input.length; itemX++) {
                for (int itemY = 0; itemY < input[itemX].length; itemY++) {
                    GuiRectangle rect = ITEM_POSITION[itemX][itemY];
                    ItemStack stack = input[itemX][itemY].get();
                    if (stack != null) {
                        GlStateManager.color(1, 1, 1);
                        gui.mc.getRenderItem().renderItemIntoGUI(stack, x + rect.x, y + rect.y);
                        if (rect.offset(x, y).contains(gui.mouse)) {
                            gui.tooltipStack = stack;
                        }
                    }
                }
            }
            gui.mc.getRenderItem().renderItemIntoGUI(output.get(), x + OUT_POSITION.x, y + OUT_POSITION.y);
            if (OUT_POSITION.offset(x, y).contains(gui.mouse)) {
                gui.tooltipStack = output.get();
            }
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
        }
        current = current.nextLine(4, height / LINE_HEIGHT);
        return current;
    }
}
