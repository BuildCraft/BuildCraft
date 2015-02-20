/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.transport;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import buildcraft.api.enums.EnumColor;

public interface IPipeTile {

	public enum PipeType {

		ITEM, FLUID, POWER, STRUCTURE
	}

	PipeType getPipeType();

	/**
	 * Offers an ItemStack for addition to the pipe. Will be rejected if the
	 * pipe doesn't accept items from that side.
	 *
	 * @param stack ItemStack offered for addition. Do not manipulate this!
	 * @param doAdd If false no actual addition should take place. Implementors
	 * should simulate.
	 * @param from Orientation the ItemStack is offered from.
	 * @param color The color of the item to be added to the pipe, or null for no color.
	 * @return Amount of items used from the passed stack.
	 */
	int injectItem(ItemStack stack, boolean doAdd, EnumFacing from, EnumColor color);

	/**
	 * Same as
	 * {@link #injectItem(ItemStack, boolean, EnumFacing, EnumColor)}
	 * but with no color attribute.
	 */
	int injectItem(ItemStack stack, boolean doAdd, EnumFacing from);

	/**
	 * True if the pipe is connected to the block/pipe in the specific direction
	 * 
	 * @param with
	 * @return true if connect
	 */
	boolean isPipeConnected(EnumFacing with);

	TileEntity getAdjacentTile(EnumFacing dir);
	
	IPipe getPipe();
}
