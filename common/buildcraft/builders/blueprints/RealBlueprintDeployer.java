/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.blueprints;

import java.io.File;

import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.blueprints.BlueprintDeployer;
import buildcraft.api.blueprints.Translation;
import buildcraft.builders.LibraryDatabase;
import buildcraft.core.blueprints.Blueprint;
import buildcraft.core.blueprints.BlueprintBase;
import buildcraft.core.blueprints.BptBuilderBlueprint;
import buildcraft.core.blueprints.BptContext;
import buildcraft.core.blueprints.LibraryId;
import buildcraft.core.lib.utils.NBTUtils;

public class RealBlueprintDeployer extends BlueprintDeployer {

	@Override
	public void deployBlueprint(World world, int x, int y, int z,
								ForgeDirection dir, File file) {

		deployBlueprint(world, x, y, z, dir, (Blueprint) BlueprintBase.loadBluePrint(LibraryDatabase.load(file)));
	}

	@Override
	public void deployBlueprintFromFileStream(World world, int x, int y, int z,
											  ForgeDirection dir, byte[] data) {

		deployBlueprint(world, x, y, z, dir, (Blueprint) BlueprintBase.loadBluePrint(NBTUtils.load(data)));
	}

	private void deployBlueprint(World world, int x, int y, int z, ForgeDirection dir, Blueprint bpt) {
		bpt.id = new LibraryId();
		bpt.id.extension = "bpt";

		BptContext context = bpt.getContext(world, bpt.getBoxForPos(x, y, z));

		if (bpt.rotate) {
			if (dir == ForgeDirection.EAST) {
				// Do nothing
			} else if (dir == ForgeDirection.SOUTH) {
				bpt.rotateLeft(context);
			} else if (dir == ForgeDirection.WEST) {
				bpt.rotateLeft(context);
				bpt.rotateLeft(context);
			} else if (dir == ForgeDirection.NORTH) {
				bpt.rotateLeft(context);
				bpt.rotateLeft(context);
				bpt.rotateLeft(context);
			}
		}

		Translation transform = new Translation();

		transform.x = x - bpt.anchorX;
		transform.y = y - bpt.anchorY;
		transform.z = z - bpt.anchorZ;

		bpt.translateToWorld(transform);

		new BptBuilderBlueprint(bpt, world, x, y, z).deploy();
	}
}

