/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.blueprints;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map.Entry;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings.GameType;
import buildcraft.api.blueprints.Schematic;
import buildcraft.api.blueprints.SchematicToBuild;
import buildcraft.api.blueprints.SchematicToBuild.Mode;
import buildcraft.api.core.StackKey;
import buildcraft.core.IBuilderInventory;
import buildcraft.core.utils.BCLog;
import buildcraft.core.utils.BlockUtil;

public class BptBuilderBlueprint extends BptBuilderBase {

	LinkedList<SchematicToBuild> clearList = new LinkedList<SchematicToBuild>();
	LinkedList<SchematicToBuild> primaryList = new LinkedList<SchematicToBuild>();
	LinkedList<SchematicToBuild> secondaryList = new LinkedList<SchematicToBuild>();

	LinkedList<SchematicToBuild> postProcessingList = new LinkedList<SchematicToBuild>();

	public LinkedList <ItemStack> neededItems = new LinkedList <ItemStack> ();

	public BptBuilderBlueprint(Blueprint bluePrint, World world, int x, int y, int z) {
		super(bluePrint, world, x, y, z);

		for (int j = bluePrint.sizeY - 1; j >= 0; --j) {
			for (int i = 0; i < bluePrint.sizeX; ++i) {
				for (int k = 0; k < bluePrint.sizeZ; ++k) {
					int xCoord = i + x - bluePrint.anchorX;
					int yCoord = j + y - bluePrint.anchorY;
					int zCoord = k + z - bluePrint.anchorZ;

					Schematic slot = bluePrint.contents[i][j][k];

					if (slot == null) {
						slot = new Schematic();
						slot.meta = 0;
						slot.block = Blocks.air;
					}

					SchematicToBuild b = new SchematicToBuild ();
					b.schematic = slot;
					b.x = xCoord;
					b.y = yCoord;
					b.z = zCoord;
					b.mode = Mode.ClearIfInvalid;

					clearList.add(b);

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

					if (slot == null) {
						slot = new Schematic();
						slot.meta = 0;
						slot.block = Blocks.air;
					}

					SchematicToBuild b = new SchematicToBuild ();
					b.schematic = slot;
					b.x = xCoord;
					b.y = yCoord;
					b.z = zCoord;
					b.mode = Mode.Build;

					if (slot.block != null && slot.block.isOpaqueCube()) {
						primaryList.add(b);
					} else {
						secondaryList.add(b);
					}

					if (slot.block != null) {
						postProcessingList.add(b);
					}
				}
			}
		}

		recomputeNeededItems();
	}

	private void checkDone() {
		recomputeNeededItems();

		if (clearList.size() == 0 && primaryList.size() == 0 && secondaryList.size() == 0) {
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

		if (primaryList.size() != 0) {
			SchematicToBuild slot = internalGetNextBlock(world, inv, primaryList);
			checkDone();

			if (slot != null) {
				return slot;
			}
		}

		if (secondaryList.size() != 0) {
			SchematicToBuild slot = internalGetNextBlock(world, inv, secondaryList);
			checkDone();

			if (slot != null) {
				return slot;
			}
		}

		checkDone();

		return null;
	}

	public SchematicToBuild internalGetNextBlock(World world, IBuilderInventory inv, LinkedList<SchematicToBuild> list) {
		LinkedList<SchematicToBuild> failSlots = new LinkedList<SchematicToBuild>();

		SchematicToBuild result = null;

		while (list.size() > 0) {
			SchematicToBuild slot = list.removeFirst();

			boolean getNext = false;

			try {
				getNext = !slot.schematic.isValid(context, slot.x, slot.y,
						slot.z) && !slot.schematic.ignoreBuilding();
			} catch (Throwable t) {
				// Defensive code against errors in implementers
				t.printStackTrace();
				BCLog.logger.throwing("BptBuilderBlueprint", "internalGetBlock", t);
				getNext = false;
			}

			if (getNext) {
				if (slot.mode == Mode.ClearIfInvalid) {
					if (!BlockUtil.isSoftBlock(world, slot.x, slot.y, slot.z)) {
						result = slot;
						break;
					}
				} else if (world.getWorldInfo().getGameType() == GameType.CREATIVE) {
					// In creative, we don't use blocks given in the builder

					result = slot;

					break;
				} else if (checkRequirements(inv, slot.schematic)) {
					useRequirements(inv, slot.schematic);

					result = slot;
					break;
				} else {
					failSlots.add(slot);
				}
			}
		}

		list.addAll(failSlots);

		return result;
	}

	public boolean checkRequirements(IBuilderInventory inv, Schematic slot) {
		if (slot.block == null) {
			return true;
		}

		LinkedList<ItemStack> tmpReq = new LinkedList<ItemStack>();
		LinkedList<ItemStack> tmpInv = new LinkedList<ItemStack>();

		try {
			for (ItemStack stk : slot.getRequirements(context)) {
				if (stk != null) {
					tmpReq.add(stk.copy());
				}
			}
		} catch (Throwable t) {
			// Defensive code against errors in implementers
			t.printStackTrace();
			BCLog.logger.throwing("BptBuilderBlueprint", "checkRequirements", t);
		}

		int size = inv.getSizeInventory();
		for (int i = 0; i < size; ++i) {
			if (!inv.isBuildingMaterial(i)) {
				continue;
			}

			if (inv.getStackInSlot(i) != null) {
				tmpInv.add(inv.getStackInSlot(i).copy());
			}
		}

		/*for (ItemStack reqStk : tmpReq) {
			for (ItemStack invStk : tmpInv) {
				if (invStk != null && reqStk.itemID == invStk.itemID && invStk.stackSize > 0) {

					if (!invStk.isItemStackDamageable() && (reqStk.getItemDamage() != invStk.getItemDamage())) {
						continue; // it doesn't match, try again
					}

					try {

						slot.useItem(context, reqStk, invStk);
					} catch (Throwable t) {
						// Defensive code against errors in implementers
						t.printStackTrace();
						BCLog.logger.throwing("BptBuilderBlueprint", "checkRequirements", t);
					}

					if (reqStk.stackSize == 0) {
						break;
					}
				}
			}

			if (reqStk.stackSize != 0)
				return false;
		}*/

		return true;
	}

	public void useRequirements(IBuilderInventory inv, Schematic slot) {
		if (slot.block == null) {
			return;
		}

		LinkedList<ItemStack> tmpReq = new LinkedList<ItemStack>();

		try {
			for (ItemStack stk : slot.getRequirements(context)) {
				if (stk != null) {
					tmpReq.add(stk.copy());
				}
			}
		} catch (Throwable t) {
			// Defensive code against errors in implementers
			t.printStackTrace();
			BCLog.logger.throwing("BptBuilderBlueprint", "useRequirements", t);

		}

		ListIterator<ItemStack> itr = tmpReq.listIterator();

		while (itr.hasNext()) {
			ItemStack reqStk = itr.next();
			boolean smallStack = reqStk.stackSize == 1;
			ItemStack usedStack = reqStk;
			int size = inv.getSizeInventory();
			for (int i = 0; i <= size; ++i) {
				if (!inv.isBuildingMaterial(i)) {

				}

				ItemStack invStk = inv.getStackInSlot(i);

				/*if (invStk != null && reqStk.itemID == invStk.itemID && invStk.stackSize > 0) {

					if (!invStk.isItemStackDamageable() && (reqStk.getItemDamage() != invStk.getItemDamage())) {
						continue;
					}

					try {
						usedStack = slot.useItem(context, reqStk, invStk);
					} catch (Throwable t) {
						// Defensive code against errors in implementers
						t.printStackTrace();
						BCLog.logger.throwing("BptBuilderBlueprint", "useRequirements", t);
					}

					if (invStk.stackSize == 0) {
						inv.setInventorySlotContents(i, null);
					} else {
						inv.setInventorySlotContents(i, invStk);
					}

					if (reqStk.stackSize == 0) {
						break;
					}
				}*/
			}

			if (reqStk.stackSize != 0) {
				return;
			}
			if (smallStack) {
				itr.set(usedStack); // set to the actual item used.
			}
		}

		return;
	}

	public void recomputeNeededItems() {
		neededItems.clear();

		HashMap <StackKey, Integer> computeStacks = new HashMap <StackKey, Integer> ();

		for (SchematicToBuild slot : primaryList) {

			LinkedList<ItemStack> stacks = new LinkedList<ItemStack>();

			try {
				stacks = slot.schematic.getRequirements(context);
			} catch (Throwable t) {
				// Defensive code against errors in implementers
				t.printStackTrace();
				BCLog.logger.throwing("BptBuilderBlueprint", "recomputeIfNeeded", t);
			}

			for (ItemStack stack : stacks) {
				if (stack == null || stack.getItem() == null || stack.stackSize == 0) {
					continue;
				}

				StackKey key = new StackKey(stack);

				if (!computeStacks.containsKey(key)) {
					computeStacks.put(key, stack.stackSize);
				} else {
					Integer num = computeStacks.get(key);
					num += stack.stackSize;

					computeStacks.put(key, num);
				}

			}
		}

		for (SchematicToBuild slot : secondaryList) {
			LinkedList<ItemStack> stacks = slot.schematic.getRequirements(context);

			for (ItemStack stack : stacks) {
				if (stack == null || stack.getItem() == null || stack.stackSize == 0) {
					continue;
				}

				StackKey key = new StackKey(stack);

				if (!computeStacks.containsKey(key)) {
					computeStacks.put(key, stack.stackSize);
				} else {
					Integer num = computeStacks.get(key);
					num += stack.stackSize;

					computeStacks.put(key, num);
				}

			}
		}

		for (Entry<StackKey, Integer> e : computeStacks.entrySet()) {
			ItemStack newStack = e.getKey().stack.copy();
			newStack.stackSize = e.getValue();
			neededItems.add(newStack);
		}

		Collections.sort (neededItems, new Comparator<ItemStack>() {
			@Override
			public int compare(ItemStack o1, ItemStack o2) {
				if (o1.stackSize > o2.stackSize) {
					return -1;
				} else if (o1.stackSize < o2.stackSize) {
					return 1;
				} else if (o1.getItemDamage() > o2.getItemDamage()) {
					return -1;
				} else if (o1.getItemDamage() < o2.getItemDamage()) {
					return 1;
				} else {
					return 0;
				}
			}
		});

	}

	@Override
	public void postProcessing(World world) {
		for (SchematicToBuild s : postProcessingList) {
			try {
				s.schematic.postProcessing(context);
			} catch (Throwable t) {
				// Defensive code against errors in implementers
				t.printStackTrace();
				BCLog.logger.throwing("BptBuilderBlueprint", "postProcessing", t);
			}
		}
	}

}
