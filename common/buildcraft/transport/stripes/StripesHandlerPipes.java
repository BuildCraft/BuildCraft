/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
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
import buildcraft.api.transport.IStripesHandler;
import buildcraft.api.transport.IStripesPipe;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.ItemPipe;
import buildcraft.transport.Pipe;
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
			IStripesPipe pipe) {

		if (!(stack.getItem() instanceof ItemPipe) || (stack.getItem() == BuildCraftTransport.pipeItemsStripes)) {
			return false;
		}

		if (world.getBlock(x, y, z) != Blocks.air) {
			return false;
		}
		
		Position p = new Position(x, y, z, direction);
		p.moveBackwards(1.0d);
		
		TileEntity tile = world.getTileEntity((int) p.x, (int) p.y, (int) p.z);
		if (!(tile instanceof TileGenericPipe)) {
			return false;
		}
		TileGenericPipe pipeTile = (TileGenericPipe) tile;
		if (!(pipeTile.pipe.transport instanceof PipeTransportItems)) {
			return false;
		}
		
		// Checks done, start to actually do stuff
		if (!copyPipeTo(world, pipeTile, x, y, z, player)) {
			return false;
		}

		pipeTile.pipe.transport.container.initializeFromItemMetadata(stack.getItemDamage() - 1);
		Pipe<?> newPipe = BlockGenericPipe.createPipe(stack.getItem());
		newPipe.setTile(pipeTile.pipe.container);
		pipeTile.pipe.container.pipe = newPipe;
		pipeTile.updateEntity(); // Needed so that the tile does computeConnections()
		
		ItemStack transportStack = stack.copy();
		stack.stackSize = 0;
		transportStack.stackSize--;
		if (transportStack.stackSize > 0) {
			pipeTile.pipe.container.injectItem(transportStack, true, direction.getOpposite());
		}

		return true;
	}

	private boolean copyPipeTo(World world, TileGenericPipe pipeTile, int x, int y, int z, EntityPlayer player) {
		int meta = pipeTile.pipe.container.getItemMetadata();
		ItemStack stack = new ItemStack(BuildCraftTransport.pipeItemsStripes, 1, meta);
		if (!BuildCraftTransport.pipeItemsStripes.onItemUse(stack, player, world, x, y, z, 1, 0, 0, 0)) {
			return false;
		}
		return true;
	}
}
