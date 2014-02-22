/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.blueprints;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Use the template system to describe fillers
 */
public class Template extends BlueprintBase {

	public Template() {
	}

	public Template(int sizeX, int sizeY, int sizeZ) {
		super(sizeX, sizeY, sizeZ);
	}

	@Override
	public void saveContents(NBTTagCompound nbt) {
		/*writer.write("mask:");

		boolean first = true;

		for (int x = 0; x < sizeX; ++x) {
			for (int y = 0; y < sizeY; ++y) {
				for (int z = 0; z < sizeZ; ++z) {
					if (first) {
						first = false;
					} else {
						writer.write(",");
					}

					writer.write(contents[x][y][z].blockId + "");
				}
			}
		}*/
	}

	@Override
	public void loadContents(NBTTagCompound nbt) throws BptError {
		/*if (attr.equals("mask")) {
			contents = new BptSlot[sizeX][sizeY][sizeZ];

			String[] mask = val.split(",");
			int maskIndex = 0;

			/*for (int x = 0; x < sizeX; ++x) {
				for (int y = 0; y < sizeY; ++y) {
					for (int z = 0; z < sizeZ; ++z) {
						contents[x][y][z] = new BptSlot();
						contents[x][y][z].x = x;
						contents[x][y][z].y = y;
						contents[x][y][z].z = z;
						contents[x][y][z].blockId = Integer.parseInt(mask[maskIndex]);

						maskIndex++;
					}
				}
			}
		}*/

	}

}
