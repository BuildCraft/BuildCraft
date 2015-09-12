/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.builders.schematics;

import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.api.core.BlockIndex;

public class SchematicWallSide extends SchematicBlock {
	@Override
	public Set<BlockIndex> getPrerequisiteBlocks(IBuilderContext context) {
		final int yPos = 0;
		final int yNeg = 5;
		final int xPos = 2;
		final int xNeg = 1;
		final int zPos = 4;
		final int zNeg = 3;

		switch (meta & 7) {
			case xPos:
				return Sets.newHashSet(RELATIVE_INDEXES[ForgeDirection.EAST.ordinal()]);
			case xNeg:
				return Sets.newHashSet(RELATIVE_INDEXES[ForgeDirection.WEST.ordinal()]);
			case yPos:
				return Sets.newHashSet(RELATIVE_INDEXES[ForgeDirection.UP.ordinal()]);
			case yNeg:
				return Sets.newHashSet(RELATIVE_INDEXES[ForgeDirection.DOWN.ordinal()]);
			case zPos:
				return Sets.newHashSet(RELATIVE_INDEXES[ForgeDirection.SOUTH.ordinal()]);
			case zNeg:
				return Sets.newHashSet(RELATIVE_INDEXES[ForgeDirection.NORTH.ordinal()]);
		}
		return null;
	}

	@Override
	public void rotateLeft(IBuilderContext context) {
		final int xPos = 2;
		final int xNeg = 1;
		final int zPos = 4;
		final int zNeg = 3;

		switch (meta) {
		case xPos:
			meta = zPos;
			break;
		case zNeg:
			meta = xPos;
			break;
		case xNeg:
			meta = zNeg;
			break;
		case zPos:
			meta = xNeg;
			break;
		}
	}
}
