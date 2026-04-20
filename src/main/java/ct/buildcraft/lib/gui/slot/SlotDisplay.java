/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.gui.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SlotDisplay extends SlotItemHandler {
    //private static Container emptyInventory = new SimpleContainer(0);
//    private final IntFunction<ItemStack> getter;

	/**
	 * @see ct.buildcraft.lib.gui.ItemProvider
	 * */
    public SlotDisplay(/*IntFunction<ItemStack> getter,*/IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        //this.getter = getter;
    }

    @Override
	public boolean mayPlace(ItemStack p_40231_) {
		return false;
	}

	@Override
	public boolean mayPickup(Player p_40228_) {
		return false;
	}

	@Override
	public ItemStack safeTake(int p_150648_, int p_150649_, Player p_150650_) {
		return ItemStack.EMPTY;
	}

	@Override
	public void set(ItemStack p_40240_) {
		super.set(p_40240_);
	}

	@Override
	public boolean allowModification(Player p_150652_) {
		return false;
	}
	
/*	@Override
	public ItemStack getItem() {
		return getter.apply(getSlotIndex()).copy();
	}*/

	@Override
	public ItemStack remove(int p_40227_) {
		return getItem();
	}

	@Override
	public int getMaxStackSize(ItemStack p_40238_) {
		return getItem().getCount();
	}
	
	@Override
	public int getMaxStackSize() {
		return getItem().getCount();
	}

}
