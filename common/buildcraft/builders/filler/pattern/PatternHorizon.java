/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.filler.pattern;

import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.core.Box;
import buildcraft.core.blueprints.BptBuilderTemplate;

public class PatternHorizon extends FillerPattern {

	public PatternHorizon() {
		super("horizon");
	}

	/*@Override
	public boolean iteratePattern(TileEntity tile, IBox box, ItemStack stackToPlace) {
		int xMin = (int) box.pMin().x;
		int yMin = (int) box.pMin().y;
		int zMin = (int) box.pMin().z;

		int xMax = (int) box.pMax().x;
		int zMax = (int) box.pMax().z;

		if (stackToPlace != null && flatten(xMin, 1, zMin, xMax, yMin - 1, zMax, tile.getWorldObj(), stackToPlace)) {
			return false;
		}
		return !empty(xMin, yMin, zMin, xMax, tile.getWorldObj().getActualHeight(), zMax, tile.getWorldObj());
	}*/

	@Override
	public BptBuilderTemplate getBlueprint(Box box, World world,
			ForgeDirection orientation) {
		return null;
	}
}
