/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.gui;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

/**
 * More dynamic slot displaying an inventory fluid at specified position in
 * the passed IInventory
 */
public class IInventorySlot extends AdvancedSlot {

	private IInventory tile;
	private int slot;

	public IInventorySlot(GuiAdvancedInterface gui, int x, int y, IInventory tile, int slot) {
		super(gui, x, y);
		this.tile = tile;
		this.slot = slot;
	}

	@Override
	public ItemStack getItemStack() {
		return tile.getStackInSlot(slot);
	}
}