/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.schematics;

import net.minecraft.item.Item;

import net.minecraft.util.EnumFacing;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicTile;
import buildcraft.core.inventory.SimpleInventory;

public class BptItemPipeFilters extends BptPipeExtension {

	public BptItemPipeFilters(Item i) {
		super (i);
	}

	@Override
	public void rotateLeft(SchematicTile slot, IBuilderContext context) {
		SimpleInventory inv = new SimpleInventory(54, "Filters", 1);
		SimpleInventory newInv = new SimpleInventory(54, "Filters", 1);
		inv.readFromNBT(slot.tileNBT);

		for (int dir = 0; dir <= 5; ++dir) {
			EnumFacing r = EnumFacing.values()[dir].rotateY();

			for (int s = 0; s < 9; ++s) {
				newInv.setInventorySlotContents(r.ordinal() * 9 + s, inv.getStackInSlot(dir * 9 + s));
			}
		}

		newInv.writeToNBT(slot.tileNBT);
	}
}
