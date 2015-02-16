/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.transport.stripes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.transport.IStripesHandler;
import buildcraft.api.transport.IStripesPipe;

public class StripesHandlerDefault implements IStripesHandler {

	@Override
	public StripesHandlerType getType() {
		return StripesHandlerType.ITEM_USE;
	}

	@Override
	public boolean shouldHandle(ItemStack stack) {
		return true;
	}

	@Override
	public boolean handle(World world, int x, int y, int z,
			ForgeDirection direction, ItemStack stack, EntityPlayer player,
			IStripesPipe pipe) {
		if (!world.isAirBlock(x, y, z)) {
			return false;
		}
		if (!stack.tryPlaceItemIntoWorld(player, world, x, y - 1, z, 1, 0.0f, 0.0f, 0.0f)) {
			return false;
		}
		pipe.sendItem(stack, direction.getOpposite());
		return true;
	}

}
