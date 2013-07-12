/**
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core.blueprints;

import buildcraft.core.IBuilderInventory;
import buildcraft.core.blueprints.BptSlot.Mode;
import java.util.LinkedList;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class BptBuilderTemplate extends BptBuilderBase {

	LinkedList<BptSlot> clearList = new LinkedList<BptSlot>();
	LinkedList<BptSlot> buildList = new LinkedList<BptSlot>();

	public BptBuilderTemplate(BptBase bluePrint, World world, int x, int y, int z) {
		super(bluePrint, world, x, y, z);

		for (int j = bluePrint.sizeY - 1; j >= 0; --j) {
			for (int i = 0; i < bluePrint.sizeX; ++i) {
				for (int k = 0; k < bluePrint.sizeZ; ++k) {
					int xCoord = i + x - bluePrint.anchorX;
					int yCoord = j + y - bluePrint.anchorY;
					int zCoord = k + z - bluePrint.anchorZ;

					BptSlot slot = bluePrint.contents[i][j][k];

					if (slot == null || slot.blockId == 0) {
						slot = new BptSlot();
						slot.meta = 0;
						slot.blockId = 0;
						slot.x = xCoord;
						slot.y = yCoord;
						slot.z = zCoord;

						slot.mode = Mode.ClearIfInvalid;

						clearList.add(slot);
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

					BptSlot slot = bluePrint.contents[i][j][k];

					if (slot != null) {
						slot = slot.clone();
					} else {
						slot = new BptSlot();
						slot.meta = 0;
						slot.blockId = 0;
					}

					slot.x = xCoord;
					slot.y = yCoord;
					slot.z = zCoord;

					slot.mode = Mode.Build;

					if (slot.blockId != 0) {
						buildList.add(slot);
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
	public BptSlot getNextBlock(World world, IBuilderInventory inv) {
		if (clearList.size() != 0) {
			BptSlot slot = internalGetNextBlock(world, inv, clearList);
			checkDone();

			if (slot != null)
				return slot;
			else
				return null;
		}

		if (buildList.size() != 0) {
			BptSlot slot = internalGetNextBlock(world, inv, buildList);
			checkDone();

			if (slot != null)
				return slot;
			else
				return null;
		}

		checkDone();

		return null;
	}

	public BptSlot internalGetNextBlock(World world, IBuilderInventory inv, LinkedList<BptSlot> list) {
		BptSlot result = null;

		while (list.size() > 0) {
			BptSlot slot = list.getFirst();

			// Note from CJ: I have no idea what this code is supposed to do, so I'm not touching it.
			if (slot.blockId == world.getBlockId(slot.x, slot.y, slot.z)) {
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
			}
		}

		return result;
	}
}
