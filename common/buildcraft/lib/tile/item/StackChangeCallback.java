package buildcraft.lib.tile.item;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import net.minecraftforge.items.IItemHandlerModifiable;

@FunctionalInterface
public interface StackChangeCallback {
    void onStackChange(IItemHandlerModifiable itemHandler, int slot, @Nonnull ItemStack before, @Nonnull ItemStack after);
}
