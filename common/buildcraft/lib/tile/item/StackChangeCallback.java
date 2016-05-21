package buildcraft.lib.tile.item;

import net.minecraft.item.ItemStack;

import net.minecraftforge.items.IItemHandlerModifiable;

@FunctionalInterface
public interface StackChangeCallback {
    void onStackChange(IItemHandlerModifiable itemHandler, int slot, ItemStack before, ItemStack after);
}
