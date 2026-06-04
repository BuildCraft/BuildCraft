// STUB(R.Chen): Minecraft 1.12 RenderItem — removed. TODO: inject ItemRenderer via MinecraftClient.
package net.minecraft.client.renderer;

import net.minecraft.item.ItemStack;

public class RenderItem {
    public void renderItemAndEffectIntoGUI(ItemStack stack, int x, int y) {
        // TODO(R.Chen): use MinecraftClient.getInstance().getItemRenderer()
    }
    public void renderItemOverlayIntoGUI(Object fr, ItemStack stack, int x, int y) {
        // TODO(R.Chen): use ItemRenderer
    }
}
