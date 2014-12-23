/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.blueprints;

import java.io.File;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import net.minecraft.util.EnumFacing;

import buildcraft.api.blueprints.BlueprintDeployer;
import buildcraft.api.blueprints.Translation;
import buildcraft.builders.blueprints.BlueprintDatabase;
import buildcraft.builders.blueprints.BlueprintId;
import buildcraft.builders.blueprints.BlueprintId.Kind;

public class RealBlueprintDeployer extends BlueprintDeployer {

	@Override
	public void deployBlueprint(World world, BlockPos pos,
			EnumFacing dir, File file) {

		deployBlueprint(world, pos, dir, (Blueprint) BlueprintDatabase.load(file));
	}

	@Override
	public void deployBlueprintFromFileStream(World world, BlockPos pos,
			EnumFacing dir, byte [] data) {

		deployBlueprint(world, pos, dir, (Blueprint) BlueprintDatabase.load(data));
	}

	private void deployBlueprint(World world, BlockPos pos, EnumFacing dir, Blueprint bpt) {
		bpt.id = new BlueprintId();
		bpt.id.kind = Kind.Blueprint;

		BptContext context = bpt.getContext(world, bpt.getBoxForPos(pos));

		if (bpt.rotate) {
			if (dir == EnumFacing.EAST) {
				// Do nothing
			} else if (dir == EnumFacing.SOUTH) {
				bpt.rotateLeft(context);
			} else if (dir == EnumFacing.WEST) {
				bpt.rotateLeft(context);
				bpt.rotateLeft(context);
			} else if (dir == EnumFacing.NORTH) {
				bpt.rotateLeft(context);
				bpt.rotateLeft(context);
				bpt.rotateLeft(context);
			}
		}

		Translation transform = new Translation();

		transform.x = pos.getX() - bpt.anchorX;
		transform.y = pos.getY() - bpt.anchorY;
		transform.z = pos.getZ() - bpt.anchorZ;

		bpt.translateToWorld(transform);

		new BptBuilderBlueprint(bpt, world, pos).deploy();
	}
}

