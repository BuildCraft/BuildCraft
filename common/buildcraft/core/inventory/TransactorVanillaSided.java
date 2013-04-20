package buildcraft.core.inventory;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;

public class TransactorVanillaSided extends TransactorSimple {

    private ISidedInventory sided;

    public TransactorVanillaSided(ISidedInventory inventory)
    {
        super(inventory);
        this.sided = inventory;
    }

    @Override
    protected int getPartialSlot(ItemStack stack, ForgeDirection orientation, int slotIndex)
    {
        return getSlotOnSideForStack(stack, orientation, slotIndex);
    }

    @Override
    protected int getEmptySlot(ItemStack stack, ForgeDirection orientation, int slotIndex)
    {
        return getSlotOnSideForStack(stack, orientation, slotIndex);
    }

    private int getSlotOnSideForStack(ItemStack stack, ForgeDirection orientation, int slotIndex)
    {
        int[] sideSlots = sided.getSizeInventorySide(orientation.ordinal());
        if (slotIndex >= sideSlots.length)
            return -1;
        int targetSlot = sideSlots[slotIndex];
        return sided.func_102007_a(targetSlot, stack, orientation.ordinal()) ? targetSlot : -1;
    }

}
