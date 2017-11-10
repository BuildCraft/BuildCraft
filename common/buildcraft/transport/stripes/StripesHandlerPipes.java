/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.stripes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.Position;
import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandler;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.ItemPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;

public class StripesHandlerPipes implements IStripesHandler {

	@Override
	public StripesHandlerType getType() {
		return StripesHandlerType.ITEM_USE;
	}

	@Override
	public boolean shouldHandle(ItemStack stack) {
		return stack.getItem() instanceof ItemPipe;
	}

	@Override
	public boolean handle(World world, int x, int y, int z,
						  ForgeDirection direction, ItemStack stack, EntityPlayer player,
						  IStripesActivator activator) {

		if (!(stack.getItem() instanceof ItemPipe) || (stack.getItem() == BuildCraftTransport.pipeItemsStripes)) {
			return false;
		}

		Position p = new Position(x, y, z, direction);
		p.moveBackwards(1.0D);

		Pipe<?> pipe = BlockGenericPipe.createPipe(stack.getItem());

		if (pipe.transport instanceof PipeTransportItems) {
			// Item pipe: request extending on end of tick
			BuildCraftTransport.pipeExtensionListener.requestPipeExtension(stack, world, (int) p.x, (int) p.y, (int) p.z, direction, activator);
		} else {
			// Non-item pipe: place in front of stripes (item) pipe
			stack.getItem().onItemUse(stack,
					CoreProxy.proxy.getBuildCraftPlayer((WorldServer) world, (int) p.x, (int) p.y, (int) p.z).get(),
					world, x, y, z, 1, 0, 0, 0);
		}
		return true;
	}
}
