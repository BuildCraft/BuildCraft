/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.filler.pattern;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.blueprints.SchematicRegistry;
import buildcraft.core.Box;
import buildcraft.core.blueprints.BptBuilderTemplate;
import buildcraft.core.blueprints.Template;

public class PatternFlatten extends FillerPattern {

	public PatternFlatten() {
		super("flatten");
	}

	/*@Override
	public boolean iteratePattern(TileEntity tile, IBox box, ItemStack stackToPlace) {
		int xMin = (int) box.pMin().x;
		int yMin = (int) box.pMin().y;
		int zMin = (int) box.pMin().z;

		int xMax = (int) box.pMax().x;
		int yMax = (int) box.pMax().y;
		int zMax = (int) box.pMax().z;

		if (flatten(xMin, 1, zMin, xMax, yMin - 1, zMax, tile.getWorldObj(), stackToPlace)) {
			return false;
		}
		return !empty(xMin, yMin, zMin, xMax, yMax, zMax, tile.getWorldObj());
	}*/

	@Override
	public BptBuilderTemplate getBlueprint (Box box, World world, ForgeDirection orientation) {
		int xMin = (int) box.pMin().x;
		int yMin = 1;
		int zMin = (int) box.pMin().z;

		int xMax = (int) box.pMax().x;
		int yMax = (int) box.pMax().y;
		int zMax = (int) box.pMax().z;

		Template bpt = new Template(xMax - xMin + 1, yMax - yMin + 1, zMax - zMin + 1);

		boolean found = false;
		for (int y = yMax; y >= yMin; --y) {
			for (int x = xMin; x <= xMax && !found; ++x) {
				for (int z = zMin; z <= zMax && !found; ++z) {
					bpt.contents[x - xMin][y - yMin][z - zMin] = SchematicRegistry
							.newSchematic(Blocks.stone);
				}
			}
		}

		return new BptBuilderTemplate(bpt, world, box.xMin, 1, box.zMin);
	}

	/*@Override
	public BptBuilderTemplate getBlueprint(Box box, World world,
			) {
		// TODO Auto-generated method stub
		return null;
	}*/
}
