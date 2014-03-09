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

					if (slot == null || slot.block == null) {
						slot = new Schematic();
						slot.meta = 0;
						slot.block = null;


						SchematicToBuild b = new SchematicToBuild();

						b.schematic = slot;
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
						slot = slot.clone();
					} else {
						slot = new Schematic();
						slot.meta = 0;
						slot.block = null;
					}

					SchematicToBuild b = new SchematicToBuild();

					b.schematic = slot;
					b.x = xCoord;
					b.y = yCoord;
					b.z = zCoord;

					b.mode = Mode.Build;

					if (slot.block != null) {
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
			} else {
				return null;
			}
		}

		if (buildList.size() != 0) {
			SchematicToBuild slot = internalGetNextBlock(world, inv, buildList);
			checkDone();

			if (slot != null) {
				return slot;
			} else {
				return null;
			}
		}

		checkDone();

		return null;
	}

	public SchematicToBuild internalGetNextBlock(World world, IBuilderInventory inv, LinkedList<SchematicToBuild> list) {
		SchematicToBuild result = null;

		while (list.size() > 0) {
			SchematicToBuild slot = list.getFirst();

			// Note from CJ: I have no idea what this code is supposed to do, so I'm not touching it.
			/*if (slot.blockId == world.getBlockId(slot.x, slot.y, slot.z)) {
				list.removeFirst();
			} else if (slot.mode == Mode.ClearIfInvalid) {
				result = slot;
				list.removeFirst();
				break;
			} else {
				int size = inv.getSizeInventory();
				for (int i = 0; i < size; ++i) {
					if (!inv.isBuildingMaterial(i)) {
						continue;
					}

					ItemStack stack = inv.decrStackSize(i, 1);

					if (stack != null && stack.stackSize > 0) {
						result = slot.clone();
						result.stackToUse = stack;
						list.removeFirst();
						break;
					}
				}

				break;
			}*/
		}

		return result;
	}
}
