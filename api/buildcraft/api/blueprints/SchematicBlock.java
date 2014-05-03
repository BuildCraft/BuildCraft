/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.blueprints;

import java.util.ArrayList;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockLiquid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.BlockFluidBase;

public class SchematicBlock extends SchematicBlockBase {

	public Block block = null;
	public int meta = 0;

	/**
	 * This field contains requirements for a given block when stored in the
	 * blueprint. Modders can either rely on this list or compute their own int
	 * Schematic.
	 */
	public ItemStack [] storedRequirements = new ItemStack [0];

	/**
	 * Returns the requirements needed to build this block. When the
	 * requirements are met, they will be removed all at once from the builder,
	 * before calling buildBlock.
	 */
	@Override
	public void addRequirements(IBuilderContext context, LinkedList<ItemStack> requirements) {
		if (block != null) {
			if (storedRequirements.length != 0) {
				for (ItemStack s : storedRequirements) {
					requirements.add(s);
				}
			} else {
				requirements.add(new ItemStack(block, 1, meta));
			}
		}
	}

	/**
	 * Return true if the block on the world correspond to the block stored in
	 * the blueprint at the location given by the slot. By default, this
	 * subprogram is permissive and doesn't take into account metadata.
	 */
	@Override
	public boolean isAlreadyBuilt(IBuilderContext context, int x, int y, int z) {
		return block == context.world().getBlock(x, y, z) && meta == context.world().getBlockMetadata(x, y, z);
	}

	/**
	 * Perform a 90 degree rotation to the slot.
	 */
	@Override
	public void rotateLeft(IBuilderContext context) {

	}

	/**
	 * Places the block in the world, at the location specified in the slot.
	 */
	@Override
	public void writeToWorld(IBuilderContext context, int x, int y, int z, LinkedList<ItemStack> stacks) {
		// Meta needs to be specified twice, depending on the block behavior
		context.world().setBlock(x, y, z, block, meta, 3);
		context.world().setBlockMetadataWithNotify(x, y, z, meta, 3);
	}

	/**
	 * Return true if the block should not be placed to the world. Requirements
	 * will not be asked on such a block, and building will not be called.
	 */
	@Override
	public boolean doNotBuild() {
		return false;
	}

	/**
	 * Initializes a slot from the blueprint according to an objet placed on {x,
	 * y, z} on the world. This typically means adding entries in slot.cpt. Note
	 * that "id" and "meta" will be set automatically, corresponding to the
	 * block id and meta.
	 *
	 * By default, if the block is a BlockContainer, tile information will be to
	 * save / load the block.
	 */
	@Override
	public void readFromWorld(IBuilderContext context, int x, int y, int z) {
		if (block != null) {
			ArrayList<ItemStack> req = block.getDrops(context.world(), x,
					y, z, context.world().getBlockMetadata(x, y, z), 0);

			if (req != null) {
				storedRequirements = new ItemStack [req.size()];
				req.toArray(storedRequirements);
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt, MappingRegistry registry) {
		nbt.setInteger("blockId", registry.getIdForBlock(block));
		nbt.setInteger("blockMeta", meta);

		NBTTagList rq = new NBTTagList();

		for (ItemStack stack : storedRequirements) {
			NBTTagCompound sub = new NBTTagCompound();
			stack.writeToNBT(stack.writeToNBT(sub));
			sub.setInteger("id", registry.getIdForItem(stack.getItem()));
			rq.appendTag(sub);
		}

		nbt.setTag("rq", rq);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt,	MappingRegistry registry) {
		block = registry.getBlockForId(nbt.getInteger("blockId"));
		meta = nbt.getInteger("blockMeta");

		NBTTagList rq = nbt.getTagList("rq", Constants.NBT.TAG_COMPOUND);

		ArrayList<ItemStack> rqs = new ArrayList<ItemStack>();

		for (int i = 0; i < rq.tagCount(); ++i) {
			try {
				NBTTagCompound sub = rq.getCompoundTagAt(i);

				if (sub.getInteger("id") >= 0) {
					// Maps the id in the blueprint to the id in the world
					sub.setInteger("id", Item.itemRegistry
							.getIDForObject(registry.getItemForId(sub
									.getInteger("id"))));

					rqs.add(ItemStack.loadItemStackFromNBT(sub));
				} else {
					// TODO: requirement can't be retreived, this blueprint is
					// only useable in creative
				}
			} catch (Throwable t) {
				t.printStackTrace();
				// TODO: requirement can't be retreived, this blueprint is
				// only useable in creative
			}
		}

		storedRequirements = rqs.toArray(new ItemStack [rqs.size()]);
	}

	@Override
	public BuildingStage getBuildStage () {
		if (block instanceof BlockFalling) {
			return BuildingStage.SUPPORTED;
		} else if (block instanceof BlockFluidBase || block instanceof BlockLiquid) {
			return BuildingStage.EXPANDING;
		} else if (block.isOpaqueCube()) {
			return BuildingStage.STANDALONE;
		} else {
			return BuildingStage.SUPPORTED;
		}
	}
}
