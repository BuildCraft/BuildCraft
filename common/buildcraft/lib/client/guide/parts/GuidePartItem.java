package buildcraft.lib.client.guide.parts;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.gui.GuiRectangle;

public abstract class GuidePartItem extends GuidePart {
    public static final GuiRectangle STACK_RECT = new GuiRectangle(0, 0, 16, 16);

    public GuidePartItem(GuiGuide gui) {
        super(gui);
    }

    protected void drawItemStack(ItemStack stack, int x, int y) {
        if (stack != null) {
            GlStateManager.color(1, 1, 1);
            FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
            gui.mc.getRenderItem().renderItemIntoGUI(stack, x, y);
            gui.mc.getRenderItem().renderItemOverlays(fr, stack, x, y);
            if (STACK_RECT.offset(x, y).contains(gui.mouse)) {
                gui.tooltipStack = stack;
            }
        }
    }

    protected void testClickItemStack(ItemStack stack, int x, int y) {
        if (stack != null && STACK_RECT.offset(x, y).contains(gui.mouse)) {
            // BCLog.logger.info("Clicked on " + stack);
            gui.openPage(GuideManager.getPageFor(stack).createNew(gui));
        }
    }
}
