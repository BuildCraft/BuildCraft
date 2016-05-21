package buildcraft.lib.tile.item;

import net.minecraft.item.ItemStack;

@FunctionalInterface
public interface StackInsertionChecker {
    boolean canSet(int slot, ItemStack stack);
}