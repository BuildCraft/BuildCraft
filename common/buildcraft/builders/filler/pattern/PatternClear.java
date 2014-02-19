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

public class PatternClear extends FillerPattern {

	public PatternClear() {
		super("clear");
	}

	@Override
	public boolean iteratePattern(TileEntity tile, IBox box, ItemStack stackToPlace) {
		int xMin = (int) box.pMin().x;
		int yMin = (int) box.pMin().y;
		int zMin = (int) box.pMin().z;

		int xMax = (int) box.pMax().x;
		int yMax = (int) box.pMax().y;
		int zMax = (int) box.pMax().z;

		return !empty(xMin, yMin, zMin, xMax, yMax, zMax, tile.getWorldObj());
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

		System.out.println ("MAX = " + yMax + ", MIN = " + yMin);

		for (int y = yMax; y >= yMin; --y) {
			for (int x = xMax; x >= xMin; --x) {
				for (int z = zMax; z >= zMin; --z) {
					bpt.setSchematic(x - xMin, y - yMin, z - zMin, new MaskSchematic(false));
				}
			}
		}

		return new BlueprintBuilder(bpt, world, (int) box.xMin, (int) box.yMin, (int) box.zMin,
				ForgeDirection.NORTH);
	}
}
