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

import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.blueprints.BlueprintDeployer;
import buildcraft.builders.blueprints.BlueprintDatabase;
import buildcraft.builders.blueprints.BlueprintId;
import buildcraft.builders.blueprints.BlueprintId.Kind;

public class RealBlueprintDeployer extends BlueprintDeployer {

	@Override
	public void deployBlueprint(World world, int x, int y, int z,
			ForgeDirection dir, File file) {

		Blueprint bpt = (Blueprint) BlueprintDatabase.load(file);
		bpt.id = new BlueprintId();
		bpt.id.kind = Kind.Blueprint;

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

		new BptBuilderBlueprint(bpt, world, x, y, z).deploy ();
	}

}
