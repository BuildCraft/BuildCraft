package buildcraft.builders.gui;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import buildcraft.core.blueprints.RequirementItemStack;
import buildcraft.core.lib.gui.AdvancedSlot;
import buildcraft.core.lib.gui.GuiAdvancedInterface;

public class SlotBuilderRequirement extends AdvancedSlot {
    public RequirementItemStack stack;

    public SlotBuilderRequirement(GuiAdvancedInterface gui, int x, int y) {
        super(gui, x, y);
    }

    @Override
    public ItemStack getItemStack() {
        return stack != null ? stack.stack : null;
    }

    @Override
    public void drawStack(ItemStack item) {
        int cornerX = (gui.width - gui.getXSize()) / 2;
        int cornerY = (gui.height - gui.getYSize()) / 2;

        GlStateManager.color(1, 1, 1, 1);

        RenderHelper.enableGUIStandardItemLighting();
        gui.drawStack(item, cornerX + x, cornerY + y);

        if (stack != null) {
            // Render real stack size
            String s = String.valueOf(stack.size > 999 ? Math.min(99, stack.size / 1000) + "K" : stack.size);
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.disableBlend();
            gui.getFontRenderer().drawStringWithShadow(s, cornerX + x + 17 - gui.getFontRenderer().getStringWidth(s), cornerY + y + 9, 16777215);
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
        }
    }
}
