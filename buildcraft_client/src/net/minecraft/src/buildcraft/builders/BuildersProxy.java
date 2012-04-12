/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.Block;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;

public class BuildersProxy {
	
	public static boolean canPlaceTorch (World w, int i, int j, int k) {
		Block block = Block.blocksList [w.getBlockId(i, j, k)];
		
		if (block == null || !block.renderAsNormalBlock()) {
			return false;
		} else {
			return true;
		}
	}

}
