package buildcraft.api.core;

import net.minecraft.item.ItemStack;

public interface IInvSlot {
    /**
     * Returns the slot number of the underlying Inventory.
     *
     * @return the slot number
     */
    int getIndex();

    boolean canPutStackInSlot(ItemStack stack);

    boolean canTakeStackFromSlot(ItemStack stack);

    ItemStack decreaseStackInSlot();

    ItemStack getStackInSlot();

    void setStackInSlot(ItemStack stack);
}
