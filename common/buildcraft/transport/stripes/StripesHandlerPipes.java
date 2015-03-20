/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.stripes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.Position;
import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandler;
import buildcraft.transport.ItemPipe;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;

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

		TileEntity tile = world.getTileEntity((int) p.x, (int) p.y, (int) p.z);
		if (!(tile instanceof TileGenericPipe)) {
			return false;
		}
		TileGenericPipe pipeTile = (TileGenericPipe) tile;
		if (!(pipeTile.pipe.transport instanceof PipeTransportItems)) {
			return false;
		}

		// Checks done, request extension
		BuildCraftTransport.pipeExtensionListener.requestPipeExtension(stack, world, (int) p.x, (int) p.y, (int) p.z, direction, activator);
		return true;
	}
}
