/*
 * Copyright (c) CovertJaguar, 2011 http://railcraft.info
 *
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at railcraft.wikispaces.com.
 */
package buildcraft.core.gui.slots;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 *
 * @author SandGrainOne
 */
public class SlotValidated extends Slot {

	public SlotValidated(IInventory inv, int id, int x, int y) {
		super(inv, id, x, y);
	}

	@Override
	public boolean isItemValid(ItemStack itemStack) {
		return inventory.isItemValidForSlot(this.getSlotIndex(), itemStack);
	}
}
