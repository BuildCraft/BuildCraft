/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.lib.gui.slot;

import net.minecraft.item.ItemStack;

import net.minecraftforge.items.SlotItemHandler;

import buildcraft.lib.tile.item.IItemHandlerAdv;

import javax.annotation.Nonnull;

public class SlotBase extends SlotItemHandler {
    public final int handlerIndex;
    public final IItemHandlerAdv itemHandler;

    public SlotBase(IItemHandlerAdv itemHandler, int slotIndex, int posX, int posY) {
        super(itemHandler, slotIndex, posX, posY);
        this.handlerIndex = slotIndex;
        this.itemHandler = itemHandler;
    }

    public boolean canShift() {
        return true;
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return itemHandler.canSet(handlerIndex, stack);
    }

    /** @param stack
     * @param simulate
     * @return The left over. */
    public ItemStack insert(ItemStack stack, boolean simulate) {
        return getItemHandler().insertItem(handlerIndex, stack, simulate);
    }
}
