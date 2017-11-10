/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.schematics;

import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;

public class SchematicSilverfish extends SchematicBlock {
	private Block getRealBlock() {
		if (meta == 0) {
			return Blocks.stone;
		} else if (meta == 1) {
			return Blocks.cobblestone;
		} else if (meta <= 5) {
			return Blocks.stonebrick;
		} else {
			return Blocks.stone;
		}
	}

	private int getRealMetadata() {
		if (meta >= 2 && meta <= 5) {
			return meta - 2;
		}
		return 0;
	}

	@Override
	public void getRequirementsForPlacement(IBuilderContext context, LinkedList<ItemStack> requirements) {
		requirements.add(new ItemStack(getRealBlock(), 0, getRealMetadata()));
	}

	@Override
	public void storeRequirements(IBuilderContext context, int x, int y, int z) {

	}

	@Override
	public void placeInWorld(IBuilderContext context, int x, int y, int z, LinkedList<ItemStack> stacks) {
		context.world().setBlock(x, y, z, getRealBlock(), getRealMetadata(), 3);
	}

	@Override
	public boolean isAlreadyBuilt(IBuilderContext context, int x, int y, int z) {
		return context.world().getBlock(x, y, z) == getRealBlock() && context.world().getBlockMetadata(x, y, z) == getRealMetadata();
	}
}
