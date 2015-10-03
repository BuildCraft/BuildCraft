package buildcraft.core.lib.inventory;

import net.minecraft.item.ItemStack;

public interface IInventoryListener {
    /** WARNING: This can be called at anytime when the inventory changes, including when a tile entity is loading so
     * before it has been added to the world (So TileEntity#worldObj could be null) */
    public void onChange(int slot, ItemStack before, ItemStack after);
}
