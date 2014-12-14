/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.schematics;

import net.minecraft.block.properties.IProperty;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicTile;

public class SchematicRotate extends SchematicTile {
	IProperty rot;

	public SchematicRotate(IProperty rotationProperty) {
		rot = rotationProperty;
	}

	@Override
	public boolean isAlreadyBuilt(IBuilderContext context, BlockPos pos) {
		return state.getBlock() == context.world().getBlockState(pos).getBlock();
	}

	@Override
	public void rotateLeft(IBuilderContext context) {
		// TODO: Check if correct
		EnumFacing direction = ((EnumFacing) state.getValue(rot)).rotateY();
		state = state.withProperty(rot, direction);
	}
}
