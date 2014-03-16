/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.blueprints;

import net.minecraft.init.Blocks;
import buildcraft.core.utils.BlockUtil;

public class SchematicMask extends Schematic {

	public boolean isConcrete = true;

	public SchematicMask (boolean isConcrete) {
		this.isConcrete = isConcrete;
	}

	@Override
	public Schematic clone() {
		return new SchematicMask(isConcrete);
	}

	@Override
	public void writeToWorld(IBuilderContext context, int x, int y, int z) {
		if (isConcrete) {
			context.world().setBlock(x, y, z, Blocks.brick_block, 0, 3);
		} else {
			context.world().setBlock(x, y, z, Blocks.air, 0, 3);
		}
	}

	@Override
	public boolean isValid(IBuilderContext context, int x, int y, int z) {
		if (isConcrete) {
			return !BlockUtil.isSoftBlock(context.world(), x, y, z);
		} else {
			return BlockUtil.isSoftBlock(context.world(), x, y, z);
		}
	}

}
