/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.transport.stripes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.Position;
import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandler;

public class StripesHandlerPlaceBlock implements IStripesHandler {

	@Override
	public StripesHandlerType getType() {
		return StripesHandlerType.ITEM_USE;
	}
	
	@Override
	public boolean shouldHandle(ItemStack stack) {
		return stack.getItem() instanceof ItemBlock;
	}

	@Override
	public boolean handle(World world, int x, int y, int z,
			ForgeDirection direction, ItemStack stack, EntityPlayer player,
			IStripesActivator activator) {
		if (!world.isAirBlock(x, y, z) && stack.tryPlaceItemIntoWorld(player, world, x, y, z, 1, 0.0f, 0.0f, 0.0f)) {
			return true;
		} else if (world.isAirBlock(x, y, z)) {
			Position src = new Position(x, y, z);
			src.orientation = direction;
			src.moveBackwards(1.0D);
			if (stack.tryPlaceItemIntoWorld(player, world, (int) src.x, (int) src.y, (int) src.z, direction.ordinal(), 0.0f, 0.0f, 0.0f)) {
				if (stack.stackSize > 0) {
					activator.sendItem(stack, direction.getOpposite());
				}

				return true;
			}
		}
		return false;
	}
}
