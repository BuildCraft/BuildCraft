/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.blueprints;

import java.util.HashMap;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import buildcraft.api.blueprints.BptSlotInfo;
import buildcraft.api.blueprints.IBptContext;
import buildcraft.core.utils.Utils;

public class BptSlot extends BptSlotInfo {

	public enum Mode {
		ClearIfInvalid, Build
	};

	public Mode mode = Mode.Build;
	public ItemStack stackToUse;

	public boolean isValid(IBptContext context) {
		// the following line is just to resurect quarry building
		if (block == null && context.world().getBlock(x, y, z) == Blocks.air) {
			return true;
		} else {
			return block == context.world().getBlock(x, y, z);
		}

		//return BlueprintManager.blockBptProps[blockId].isValid(this, context);
	}

	public void rotateLeft(IBptContext context) {
		//BlueprintManager.blockBptProps[blockId].rotateLeft(this, context);
	}

	public boolean ignoreBuilding() {
		//return BlueprintManager.blockBptProps[blockId].ignoreBuilding(this);
		return false;
	}

	public void initializeFromWorld(IBptContext context, int xs, int ys, int zs) {
		//BlueprintManager.blockBptProps[blockId].initializeFromWorld(this, context, xs, ys, zs);
	}

	public void postProcessing(IBptContext context) {
		//BlueprintManager.blockBptProps[blockId].postProcessing(this, context);
	}

	public LinkedList<ItemStack> getRequirements(IBptContext context) {
		LinkedList<ItemStack> res = new LinkedList<ItemStack>();

		//BlueprintManager.blockBptProps[blockId].addRequirements(this, context, res);

		return res;
	}

	public final void buildBlock(IBptContext context) {
		// the following line is just to resurect quarry building
		context.world().setBlock (x, y, z, block);
		context.world().setBlockMetadataWithNotify(x, y, z, meta, 0);

		//BlueprintManager.blockBptProps[blockId].buildBlock(this, context);
	}

	// returns what was used
	public ItemStack useItem(IBptContext context, ItemStack req, ItemStack stack) {
		//return BlueprintManager.blockBptProps[blockId].useItem(this, context, req, stack);
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public BptSlot clone() {
		BptSlot obj = new BptSlot();

		obj.x = x;
		obj.y = y;
		obj.z = z;
		obj.block = block;
		obj.meta = meta;
		obj.cpt = (NBTTagCompound) cpt.copy();
		obj.storedRequirements = (LinkedList<ItemStack>) storedRequirements.clone();

		if (stackToUse != null) {
			obj.stackToUse = stackToUse.copy();
		}

		obj.mode = mode;

		return obj;
	}

	public void writeToNBT(NBTTagCompound nbt,
			HashMap<Block, Integer> blocksMap, HashMap<Item, Integer> itemsMap) {
		if (!blocksMap.containsKey(block)) {
			blocksMap.put(block,
					Block.blockRegistry.getIDForObject(block));
		}

		nbt.setInteger("blockId", blocksMap.get(block));
		nbt.setInteger("blockMeta", meta);
		nbt.setTag("blockCpt", cpt);

		NBTTagList rq = new NBTTagList();

		for (ItemStack stack : storedRequirements) {
			if (!itemsMap.containsKey(stack.getItem())) {
				itemsMap.put(stack.getItem(),
						Item.itemRegistry.getIDForObject(stack.getItem()));
			}

			NBTTagCompound sub = new NBTTagCompound();
			stack.writeToNBT(stack.writeToNBT(sub));
			rq.appendTag(sub);
		}

		nbt.setTag("rq", rq);
	}

	public void readFromNBT(NBTTagCompound nbt,
			HashMap<Integer, Block> blocksMap, HashMap<Integer, Integer> itemsMap) {

		block = blocksMap.get(nbt.getInteger("blockId"));
		meta = nbt.getInteger("blockMeta");
		cpt = nbt.getCompoundTag("blockCpt");

		NBTTagList rq = nbt.getTagList("rq", Utils.NBTTag_Types.NBTTagList.ordinal());

		for (int i = 0; i < rq.tagCount(); ++i) {
			NBTTagCompound sub = rq.getCompoundTagAt(i);

			// Maps the id in the blueprint to the id in the world
			sub.setInteger("id", itemsMap.get(sub.getInteger("id")));

			storedRequirements.add(ItemStack.loadItemStackFromNBT(sub));
		}
	}
}
