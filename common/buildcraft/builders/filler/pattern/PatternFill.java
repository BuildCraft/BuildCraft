/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.filler.pattern;

import buildcraft.api.core.IBox;
import buildcraft.builders.blueprints.Blueprint;
import buildcraft.builders.blueprints.MaskSchematic;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class PatternFill extends FillerPattern {

	public static final PatternFill INSTANCE = new PatternFill();

	private PatternFill() {
		super("fill");
	}

	@Override
	public boolean iteratePattern(TileEntity tile, IBox box, ItemStack stackToPlace) {
		int xMin = (int) box.pMin().x;
		int yMin = (int) box.pMin().y;
		int zMin = (int) box.pMin().z;

		int xMax = (int) box.pMax().x;
		int yMax = (int) box.pMax().y;
		int zMax = (int) box.pMax().z;

		return !fill(xMin, yMin, zMin, xMax, yMax, zMax, stackToPlace, tile.worldObj);
	}

	@Override
	public Blueprint getBlueprint (IBox box) {
		int xMin = (int) box.pMin().x;
		int yMin = (int) box.pMin().y;
		int zMin = (int) box.pMin().z;

		int xMax = (int) box.pMax().x;
		int yMax = (int) box.pMax().y;
		int zMax = (int) box.pMax().z;

		Blueprint bpt = new Blueprint(xMax - xMin + 1, yMax - yMin + 1, zMax - zMin + 1);

		for (int y = yMin; y <= yMax; ++y) {
			for (int x = xMin; x <= xMax; ++x) {
				for (int z = zMin; z <= zMax; ++z) {
					bpt.schematics [x - xMin][y - yMin][z - zMin] = new MaskSchematic(true);
				}
			}
		}

		return bpt;
	}
}
