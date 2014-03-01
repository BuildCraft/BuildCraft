/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.blueprints;

import java.util.ArrayList;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import buildcraft.api.blueprints.BptBlock;
import buildcraft.api.blueprints.BptSlotInfo;
import buildcraft.api.blueprints.IBptContext;
import buildcraft.core.utils.Utils;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.Pipe;

public class BptBlockPipe extends BptBlock {

	public BptBlockPipe(Block block) {
		super(block);
	}

	@Override
	public void addRequirements(BptSlotInfo slot, IBptContext context, LinkedList<ItemStack> requirements) {
		Item pipeItem = context.getMappingRegistry().getItemForId (slot.cpt.getInteger("pipeId"));

		requirements.add(new ItemStack(pipeItem));

		NBTTagList nbtDrops = slot.cpt.getTagList("drops", Utils.NBTTag_Types.NBTTagCompound.ordinal());

		for (int i = 0; i < nbtDrops.tagCount(); ++i) {
			NBTTagCompound nbt = nbtDrops.getCompoundTagAt(i);
			requirements.add(ItemStack.loadItemStackFromNBT(nbt));
		}
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
		int pipeId = slot.cpt.getInteger("pipeId");

		/*if (BuildCraftCore.itemBptProps[pipeId] != null) {
			BuildCraftCore.itemBptProps[pipeId].rotateLeft(slot, context);
		}*/
	}

	@Override
	public void buildBlock(BptSlotInfo slot, IBptContext context) {
		int pipeId = slot.cpt.getInteger("pipeId");

		Pipe pipe = BlockGenericPipe.createPipe(context.getMappingRegistry()
				.getItemForId(pipeId));

		for (int i = 0; i < pipe.wireSet.length; ++i) {
			if (slot.cpt.hasKey("wire" + i)) {
				pipe.wireSet[i] = true;
			}
		}



		BlockGenericPipe.placePipe(pipe, context.world(), slot.x, slot.y,
				slot.z, slot.block, slot.meta);

		/*if (BuildCraftCore.itemBptProps[pipeId] != null) {
			BuildCraftCore.itemBptProps[pipeId].buildBlock(slot, context);
		}*/
	}

	@Override
	public void initializeFromWorld(BptSlotInfo bptSlot, IBptContext context, int x, int y, int z) {
		Pipe pipe = BlockGenericPipe.getPipe(context.world(), x, y, z);

		if (BlockGenericPipe.isValid(pipe)) {
			context.getMappingRegistry().setIdForItem(pipe.item, Item.getIdFromItem(pipe.item));
			bptSlot.cpt.setInteger("pipeId", context.getMappingRegistry()
					.getIdForItem(pipe.item));

			NBTTagList nbtDrops = new NBTTagList();

			ArrayList<ItemStack> drops = pipe.computeItemDrop ();

			for (ItemStack s : drops) {
				NBTTagCompound nbtStack = new NBTTagCompound();
				s.writeToNBT(nbtStack);
				nbtDrops.appendTag(nbtStack);
			}

			bptSlot.cpt.setTag("drops", nbtDrops);

			NBTTagCompound worldNbt = new NBTTagCompound();
			pipe.writeToNBT(worldNbt);

			bptSlot.cpt.setTag("worldNBT", worldNbt);

			// TODO: store the pipe nbt on disk as well...
		}
	}

	@Override
	public void postProcessing(BptSlotInfo slot, IBptContext context) {
		int pipeId = slot.cpt.getInteger("pipeId");

		//if (BuildCraftCore.itemBptProps[pipeId] != null) {
		//	BuildCraftCore.itemBptProps[pipeId].postProcessing(slot, context);
		//}
	}

}
