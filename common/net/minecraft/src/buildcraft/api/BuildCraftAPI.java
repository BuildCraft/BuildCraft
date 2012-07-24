/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.api;

import java.util.HashMap;
import java.util.LinkedList;

import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.gates.Action;
import net.minecraft.src.buildcraft.api.gates.IAction;
import net.minecraft.src.buildcraft.api.gates.IActionProvider;
import net.minecraft.src.buildcraft.api.gates.ITrigger;
import net.minecraft.src.buildcraft.api.gates.ITriggerProvider;
import net.minecraft.src.buildcraft.api.gates.Trigger;

public class BuildCraftAPI {

	public static final int BUCKET_VOLUME = 1000;
	public static final int LAST_ORIGINAL_BLOCK = 122;
	public static final int LAST_ORIGINAL_ITEM = 126;

	// BuildCraft additional block and item data

	public static boolean[] softBlocks = new boolean[Block.blocksList.length];
	public static BptBlock[] blockBptProps = new BptBlock[Block.blocksList.length];

	// Other BuildCraft global data
	private static EntityPlayer buildCraftPlayer;

	/**
	 * Return true if the block given in parameter is pass through (e.g. air,
	 * water...)
	 */
	public static boolean softBlock(int blockId) {
		return blockId == 0 || softBlocks[blockId] || Block.blocksList[blockId] == null;
	}

	/**
	 * Return true if the block cannot be broken, typically bedrock and lava
	 */
	public static boolean unbreakableBlock(int blockId) {
		return blockId == Block.bedrock.blockID || blockId == Block.lavaStill.blockID || blockId == Block.lavaMoving.blockID;
	}

	@Deprecated
	// To be removed
	public static void breakBlock(World world, int x, int y, int z) {
		int blockId = world.getBlockId(x, y, z);

		if (blockId != 0) {
			Block.blocksList[blockId].dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
		}

		world.setBlockWithNotify(x, y, z, 0);
	}

	public static EntityPlayer getBuildCraftPlayer(World world) {
		if (buildCraftPlayer == null) {
			buildCraftPlayer = APIProxy.createNewPlayer(world);
		}

		return buildCraftPlayer;
	}

	public static BlockSignature getBlockSignature(Block block) {
		return blockBptProps[0].getSignature(block);
	}

	public static ItemSignature getItemSignature(Item item) {
		ItemSignature sig = new ItemSignature();

		if (item.shiftedIndex >= Block.blocksList.length + BuildCraftAPI.LAST_ORIGINAL_ITEM) {
			sig.itemClassName = item.getClass().getSimpleName();
		}

		sig.itemName = item.getItemNameIS(new ItemStack(item));

		return sig;
	}
	
	static {
		for (int i = 0; i < softBlocks.length; ++i) {
			softBlocks[i] = false;
		}

		// Initialize defaults for block properties.
		for (int i = 0; i < blockBptProps.length; ++i) {
			new BptBlock(i);
		}
	}
}
