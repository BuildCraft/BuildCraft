// STUB(R.Chen): Minecraft 1.12 IItemColor → bridges to Fabric 1.20.1 ItemColorProvider.
package net.minecraft.client.renderer.color;

import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.item.ItemStack;

public interface IItemColor extends ItemColorProvider {
    int colorMultiplier(ItemStack stack, int tintIndex);

    @Override
    default int getColor(ItemStack stack, int tintIndex) {
        return colorMultiplier(stack, tintIndex);
    }
}
