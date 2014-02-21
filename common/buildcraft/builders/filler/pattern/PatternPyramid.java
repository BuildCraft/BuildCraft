/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.filler.pattern;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.core.IBox;
import buildcraft.builders.blueprints.Blueprint;
import buildcraft.builders.blueprints.BlueprintBuilder;
import buildcraft.builders.blueprints.MaskSchematic;
import buildcraft.core.Box;

public class PatternPyramid extends FillerPattern {

	public PatternPyramid() {
		super("pyramid");
	}

	@Override
	public boolean iteratePattern(TileEntity tile, IBox box, ItemStack stackToPlace) {
		int xMin = (int) box.pMin().x;
		int yMin = (int) box.pMin().y;
		int zMin = (int) box.pMin().z;

		int xMax = (int) box.pMax().x;
		int yMax = (int) box.pMax().y;
		int zMax = (int) box.pMax().z;

		int xSize = xMax - xMin + 1;
		int zSize = zMax - zMin + 1;

		int step = 0;
		int height;

		int stepY;

		if (tile.yCoord <= yMin) {
			stepY = 1;
		} else {
			stepY = -1;
		}

		if (stepY == 1) {
			height = yMin;
		} else {
			height = yMax;
		}

		while (step <= xSize / 2 && step <= zSize / 2 && height >= yMin && height <= yMax) {
			if (fill(xMin + step, height, zMin + step, xMax - step, height, zMax - step, stackToPlace, tile.getWorldObj()))
				return false;

			step++;
			height += stepY;
		}

		return true;
	}

	@Override
	public BlueprintBuilder getBlueprint (Box box, World world) {
		int xMin = (int) box.pMin().x;
		int yMin = (int) box.pMin().y;
		int zMin = (int) box.pMin().z;

		int xMax = (int) box.pMax().x;
		int yMax = (int) box.pMax().y;
		int zMax = (int) box.pMax().z;

		Blueprint bpt = new Blueprint(xMax - xMin + 1, yMax - yMin + 1, zMax - zMin + 1);

		int xSize = xMax - xMin + 1;
		int zSize = zMax - zMin + 1;

		int step = 0;
		int height;

		int stepY = 1;

		//if (tile.yCoord <= yMin) {
		//	stepY = 1;
		//} else {
		//	stepY = -1;
		//}

		if (stepY == 1) {
			height = yMin;
		} else {
			height = yMax;
		}

		while (step <= xSize / 2 && step <= zSize / 2 && height >= yMin && height <= yMax) {
			for (int x = xMin + step; x <= xMax - step; ++x) {
				for (int z = zMin + step; z <= zMax - step; ++z) {
					bpt.setSchematic(x - xMin, height - yMin, z - zMin, new MaskSchematic(true));
				}
			}

			step++;
			height += stepY;
		}

		return new BlueprintBuilder(bpt, world, box.xMin, box.yMin, box.zMin,
				ForgeDirection.NORTH);
	}
}
