package buildcraft.lib.inventory.filter;

import net.minecraft.item.ItemStack;

@FunctionalInterface
public interface ISingleStackFilter {
    boolean matches(ItemStack target, ItemStack toTest);
}