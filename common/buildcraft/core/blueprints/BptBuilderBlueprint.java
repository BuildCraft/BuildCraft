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

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings.GameType;
import buildcraft.api.blueprints.BptBlock;
import buildcraft.api.blueprints.BptBlock.Mode;
import buildcraft.api.core.StackKey;
import buildcraft.core.IBuilderInventory;
import buildcraft.core.utils.BCLog;
import buildcraft.core.utils.BlockUtil;

public class BptBuilderBlueprint extends BptBuilderBase {

	LinkedList<BptBlock> clearList = new LinkedList<BptBlock>();
	LinkedList<BptBlock> primaryList = new LinkedList<BptBlock>();
	LinkedList<BptBlock> secondaryList = new LinkedList<BptBlock>();

	LinkedList<BptBlock> postProcessingList = new LinkedList<BptBlock>();

	public LinkedList <ItemStack> neededItems = new LinkedList <ItemStack> ();

	public BptBuilderBlueprint(Blueprint bluePrint, World world, int x, int y, int z) {
		super(bluePrint, world, x, y, z);

		for (int j = bluePrint.sizeY - 1; j >= 0; --j) {
			for (int i = 0; i < bluePrint.sizeX; ++i) {
				for (int k = 0; k < bluePrint.sizeZ; ++k) {
					int xCoord = i + x - bluePrint.anchorX;
					int yCoord = j + y - bluePrint.anchorY;
					int zCoord = k + z - bluePrint.anchorZ;

					BptBlock slot = bluePrint.contents[i][j][k];

					if (slot != null) {
						slot = slot.clone();
					} else {
						slot = new BptBlock();
						slot.meta = 0;
						slot.block = null;
					}

					slot.x = xCoord;
					slot.y = yCoord;
					slot.z = zCoord;

					slot.mode = Mode.ClearIfInvalid;

					clearList.add(slot);

				}
			}
		}

		for (int j = 0; j < bluePrint.sizeY; ++j) {
			for (int i = 0; i < bluePrint.sizeX; ++i) {
				for (int k = 0; k < bluePrint.sizeZ; ++k) {
					int xCoord = i + x - bluePrint.anchorX;
					int yCoord = j + y - bluePrint.anchorY;
					int zCoord = k + z - bluePrint.anchorZ;

					BptBlock slot = bluePrint.contents[i][j][k];

					if (slot != null) {
						slot = slot.clone();
					} else {
						slot = new BptBlock();
						slot.meta = 0;
						slot.block = null;
					}

					slot.x = xCoord;
					slot.y = yCoord;
					slot.z = zCoord;

					slot.mode = Mode.Build;

					if (slot.block != null && slot.block.isOpaqueCube()) {
						primaryList.add(slot);
					} else {
						secondaryList.add(slot);
					}

					if (slot.block != null) {
						postProcessingList.add(slot.clone());
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
	public BptBlock getNextBlock(World world, IBuilderInventory inv) {
		if (clearList.size() != 0) {
			BptBlock slot = internalGetNextBlock(world, inv, clearList);
			checkDone();

			if (slot != null) {
				return slot;
			}
		}

		if (primaryList.size() != 0) {
			BptBlock slot = internalGetNextBlock(world, inv, primaryList);
			checkDone();

			if (slot != null) {
				return slot;
			}
		}

		if (secondaryList.size() != 0) {
			BptBlock slot = internalGetNextBlock(world, inv, secondaryList);
			checkDone();

			if (slot != null) {
				return slot;
			}
		}

		checkDone();

		return null;
	}

	public BptBlock internalGetNextBlock(World world, IBuilderInventory inv, LinkedList<BptBlock> list) {
		LinkedList<BptBlock> failSlots = new LinkedList<BptBlock>();

		BptBlock result = null;

		while (list.size() > 0) {
			BptBlock slot = list.removeFirst();

			boolean getNext = false;

			try {
				getNext = !slot.isValid(context) && !slot.ignoreBuilding();
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
				} else if (checkRequirements(inv, slot)) {
					useRequirements(inv, slot);

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

	public boolean checkRequirements(IBuilderInventory inv, BptBlock slot) {
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

	public void useRequirements(IBuilderInventory inv, BptBlock slot) {
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

		for (BptBlock slot : primaryList) {

			LinkedList<ItemStack> stacks = new LinkedList<ItemStack>();

			try {
				stacks = slot.getRequirements(context);
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

		for (BptBlock slot : secondaryList) {
			LinkedList<ItemStack> stacks = slot.getRequirements(context);

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
		for (BptBlock s : postProcessingList) {
			try {
				s.postProcessing(context);
			} catch (Throwable t) {
				// Defensive code against errors in implementers
				t.printStackTrace();
				BCLog.logger.throwing("BptBuilderBlueprint", "postProcessing", t);
			}
		}
	}

}
