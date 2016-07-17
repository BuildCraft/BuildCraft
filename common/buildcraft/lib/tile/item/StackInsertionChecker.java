package buildcraft.lib.tile.item;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

@FunctionalInterface
public interface StackInsertionChecker {
    boolean canSet(int slot, @Nonnull ItemStack stack);
}
