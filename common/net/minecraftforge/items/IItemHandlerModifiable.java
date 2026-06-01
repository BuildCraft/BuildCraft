// STUB(R.Chen): Forge IItemHandlerModifiable — compile shim.
package net.minecraftforge.items;

import net.minecraft.item.ItemStack;

public interface IItemHandlerModifiable extends IItemHandler {
    void setStackInSlot(int slot, ItemStack stack);
}
