/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.blueprints;

import java.util.LinkedList;

import net.minecraft.world.World;
import buildcraft.api.blueprints.Schematic;
import buildcraft.api.blueprints.SchematicToBuild;
import buildcraft.api.blueprints.SchematicToBuild.Mode;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.core.IBuilderInventory;

public class BptBuilderTemplate extends BptBuilderBase {

	LinkedList<SchematicToBuild> clearList = new LinkedList<SchematicToBuild>();
	LinkedList<SchematicToBuild> buildList = new LinkedList<SchematicToBuild>();

	public BptBuilderTemplate(BlueprintBase bluePrint, World world, int x, int y, int z) {
		super(bluePrint, world, x, y, z);

		for (int j = bluePrint.sizeY - 1; j >= 0; --j) {
			for (int i = 0; i < bluePrint.sizeX; ++i) {
				for (int k = 0; k < bluePrint.sizeZ; ++k) {
					int xCoord = i + x - bluePrint.anchorX;
					int yCoord = j + y - bluePrint.anchorY;
					int zCoord = k + z - bluePrint.anchorZ;

					Schematic slot = bluePrint.contents[i][j][k];

					if (slot == null) {
						SchematicToBuild b = new SchematicToBuild();

						b.schematic = null;
						b.x = xCoord;
						b.y = yCoord;
						b.z = zCoord;
						b.mode = Mode.ClearIfInvalid;

						clearList.add(b);
					}
				}
			}
		}

		for (int j = 0; j < bluePrint.sizeY; ++j) {
			for (int i = 0; i < bluePrint.sizeX; ++i) {
				for (int k = 0; k < bluePrint.sizeZ; ++k) {
					int xCoord = i + x - bluePrint.anchorX;
					int yCoord = j + y - bluePrint.anchorY;
					int zCoord = k + z - bluePrint.anchorZ;

					Schematic slot = bluePrint.contents[i][j][k];

					if (slot != null) {
						SchematicToBuild b = new SchematicToBuild();

						b.schematic = slot;
						b.x = xCoord;
						b.y = yCoord;
						b.z = zCoord;

						b.mode = Mode.Build;

						buildList.add(b);
					}
				}
			}
		}
	}

	private void checkDone() {
		if (clearList.size() == 0 && buildList.size() == 0) {
			done = true;
		} else {
			done = false;
		}
	}

	@Override
	public SchematicToBuild getNextBlock(World world, IBuilderInventory inv) {
		if (clearList.size() != 0) {
			SchematicToBuild slot = internalGetNextBlock(world, inv, clearList);
			checkDone();

			if (slot != null) {
				return slot;
			}
		}

		if (buildList.size() != 0) {
			SchematicToBuild slot = internalGetNextBlock(world, inv, buildList);
			checkDone();

			if (slot != null) {
				return slot;
			}
		}

		checkDone();

		return null;
	}

	public SchematicToBuild internalGetNextBlock(World world, IBuilderInventory inv, LinkedList<SchematicToBuild> list) {
		SchematicToBuild result = null;

		while (list.size() > 0) {
			SchematicToBuild slot = list.removeFirst();

			if (slot.mode == Mode.ClearIfInvalid
					&& !BuildCraftAPI.softBlocks.contains(context.world()
							.getBlock(slot.x, slot.y, slot.z))) {
				result = slot;
				break;
			} else if (slot.mode == Mode.Build
					&& BuildCraftAPI.softBlocks.contains(context.world()
							.getBlock(slot.x, slot.y, slot.z))) {
				result = slot;
				break;
			}
		}

		return result;
	}
}
