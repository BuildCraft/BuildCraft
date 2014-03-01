/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.blueprints;

import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import buildcraft.api.blueprints.BptBlock;
import buildcraft.api.blueprints.BptSlotInfo;
import buildcraft.api.blueprints.IBptContext;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.TileGenericPipe.SideProperties;

public class BptBlockPipe extends BptBlock {

	public BptBlockPipe(Block block) {
		super(block);
	}

	@Override
	public void addRequirements(BptSlotInfo slot, IBptContext context, LinkedList<ItemStack> requirements) {
		Item pipeItem = context.getMappingRegistry().getItemForId (slot.cpt.getInteger("pipeId"));

		requirements.add(new ItemStack(pipeItem));

		requirements.addAll(slot.storedRequirements);
	}

	@Override
	public boolean isValid(BptSlotInfo slot, IBptContext context) {
		Pipe pipe = BlockGenericPipe.getPipe(context.world(), slot.x, slot.y, slot.z);

		if (BlockGenericPipe.isValid(pipe)) {
			return pipe.item == Item.itemRegistry.getObjectById(slot.cpt
					.getInteger("pipeId"));
		} else {
			return false;
		}
	}

	@Override
	public void rotateLeft(BptSlotInfo slot, IBptContext context) {
		SideProperties props = new SideProperties ();

		props.readFromNBT(slot.cpt);
		props.rotateLeft();
		props.writeToNBT(slot.cpt);

		Item pipeItem = context.getMappingRegistry().getItemForId(slot.cpt.getInteger("pipeId"));

		if (BptPipeExtension.contains(pipeItem)) {
			BptPipeExtension.get(pipeItem).rotateLeft(slot, context);
		}
	}

	@Override
	public void buildBlock(BptSlotInfo slot, IBptContext context) {
		slot.cpt.setInteger("x", slot.x);
		slot.cpt.setInteger("y", slot.y);
		slot.cpt.setInteger("z", slot.z);

		context.world().setBlock(slot.x, slot.y, slot.z, slot.block);
		context.world().setBlockMetadataWithNotify(slot.x, slot.y, slot.z, slot.meta, 0);

		TileEntity tile = context.world().getTileEntity(slot.x, slot.y, slot.z);
		tile.readFromNBT(slot.cpt);
	}

	@Override
	public void initializeFromWorld(BptSlotInfo slot, IBptContext context, int x, int y, int z) {
		TileEntity tile = context.world().getTileEntity(x, y, z);
		Pipe pipe = BlockGenericPipe.getPipe(context.world(), x, y, z);

		if (BlockGenericPipe.isValid(pipe)) {
			slot.cpt.setInteger("pipeId", context.getMappingRegistry()
					.getIdForItem(pipe.item));

			slot.storedRequirements.addAll(pipe.computeItemDrop ());

			tile.writeToNBT(slot.cpt);
		}
	}

	@Override
	public void postProcessing(BptSlotInfo slot, IBptContext context) {
		Item pipeItem = context.getMappingRegistry().getItemForId(slot.cpt.getInteger("pipeId"));

		if (BptPipeExtension.contains(pipeItem)) {
			BptPipeExtension.get(pipeItem).postProcessing(slot, context);
		}
	}
}
