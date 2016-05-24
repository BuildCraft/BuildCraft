package buildcraft.lib.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class GuiStack implements ISimpleDrawable {
    private final ItemStack stack;

    public GuiStack(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public void drawAt(int x, int y) {
        Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(stack, x, y);
    }
}
