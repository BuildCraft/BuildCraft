package buildcraft.lib.gui.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.function.IntFunction;

public class SlotDisplay extends Slot {
    private static IInventory emptyInventory = new InventoryBasic("[Null]", true, 0);
    private final IntFunction<ItemStack> getter;

    public SlotDisplay(IntFunction<ItemStack> getter, int index, int xPosition, int yPosition) {
        super(emptyInventory, index, xPosition, yPosition);
        this.getter = getter;
    }

    @Override
    public ItemStack onTake(EntityPlayer player, ItemStack stack) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack getStack() {
        return getter.apply(getSlotIndex());
    }

    @Override
    public void putStack(ItemStack stack) {
    }
}
