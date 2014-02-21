/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.blueprints;

import java.util.HashMap;

import net.minecraft.block.Block;

public class BlueprintManager {

	private static final HashMap <Block, BptBlock> bptBlockRegistry = new HashMap<Block, BptBlock>();

	public static void registerBptBlock (Block block, BptBlock bptBlock) {
		bptBlockRegistry.put(block, bptBlock);
	}

	public static BptBlock getBptBlock (Block block) {
		if (!bptBlockRegistry.containsKey(block)) {
			registerBptBlock(block, new BptBlock(block));
		}

		return bptBlockRegistry.get(block);
	}
}
