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
import buildcraft.core.utils.BCLog;
import buildcraft.core.utils.BlockUtil;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.TreeMap;
import java.util.TreeSet;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.EnumGameType;
import net.minecraft.world.World;

public class BptBuilderBlueprint extends BptBuilderBase {

	LinkedList<BptSlot> clearList = new LinkedList<BptSlot>();
	LinkedList<BptSlot> primaryList = new LinkedList<BptSlot>();
	LinkedList<BptSlot> secondaryList = new LinkedList<BptSlot>();

	LinkedList<BptSlot> postProcessingList = new LinkedList<BptSlot>();

	public TreeSet<ItemStack> neededItems = new TreeSet<ItemStack>(new Comparator<ItemStack>() {

		@Override
		public int compare(ItemStack o1, ItemStack o2) {
			if (o1.stackSize > o2.stackSize)
				return -1;
			else if (o1.stackSize < o2.stackSize)
				return 1;
			else if (o1.itemID > o2.itemID)
				return -1;
			else if (o1.itemID < o2.itemID)
				return 1;
			else if (o1.getItemDamage() > o2.getItemDamage())
				return -1;
			else if (o1.getItemDamage() < o2.getItemDamage())
				return 1;

			return 0;
		}
	});

	public BptBuilderBlueprint(BptBlueprint bluePrint, World world, int x, int y, int z) {
		super(bluePrint, world, x, y, z);

		for (int j = bluePrint.sizeY - 1; j >= 0; --j) {
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

					if (slot.blockId != 0 && Block.blocksList[slot.blockId].isOpaqueCube()) {
						primaryList.add(slot);
					} else {
						secondaryList.add(slot);
					}

					if (slot.blockId != 0) {
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
	public BptSlot getNextBlock(World world, IBuilderInventory inv) {
		if (clearList.size() != 0) {
			BptSlot slot = internalGetNextBlock(world, inv, clearList);
			checkDone();

			if (slot != null)
				return slot;
			else
				return null;
		}

		if (primaryList.size() != 0) {
			BptSlot slot = internalGetNextBlock(world, inv, primaryList);
			checkDone();

			if (slot != null)
				return slot;
			else
				return null;
		}

		if (secondaryList.size() != 0) {
			BptSlot slot = internalGetNextBlock(world, inv, secondaryList);
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
		LinkedList<BptSlot> failSlots = new LinkedList<BptSlot>();

		BptSlot result = null;

		while (list.size() > 0) {
			BptSlot slot = list.removeFirst();

			boolean getNext = false;

			try {
				getNext = !slot.isValid(context) && !slot.ignoreBuilding();
			} catch (Throwable t) {
				// Defensive code against errors in implementers
				t.printStackTrace();
				BCLog.logger.throwing("BptBuilderBlueprint", "internalGetBlock", t);
				getNext = false;
			}

			if (getNext)
				if (slot.mode == Mode.ClearIfInvalid) {
					if (!BlockUtil.isSoftBlock(world, slot.x, slot.y, slot.z)) {
						result = slot;
						break;
					}
				} else if (world.getWorldInfo().getGameType() == EnumGameType.CREATIVE) {
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

		list.addAll(failSlots);

		return result;
	}

	public boolean checkRequirements(IBuilderInventory inv, BptSlot slot) {
		if (slot.blockId == 0)
			return true;

		LinkedList<ItemStack> tmpReq = new LinkedList<ItemStack>();
		LinkedList<ItemStack> tmpInv = new LinkedList<ItemStack>();

		try {
			for (ItemStack stk : slot.getRequirements(context))
				if (stk != null) {
					tmpReq.add(stk.copy());
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

		for (ItemStack reqStk : tmpReq) {
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
		}

		return true;
	}

	public void useRequirements(IBuilderInventory inv, BptSlot slot) {
		if (slot.blockId == 0)
			return;

		LinkedList<ItemStack> tmpReq = new LinkedList<ItemStack>();

		try {
			for (ItemStack stk : slot.getRequirements(context))
				if (stk != null) {
					tmpReq.add(stk.copy());
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

				if (invStk != null && reqStk.itemID == invStk.itemID && invStk.stackSize > 0) {

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
				}
			}

			if (reqStk.stackSize != 0)
				return;
			if (smallStack) {
				itr.set(usedStack); // set to the actual item used.
			}
		}

		return;
	}

	public void recomputeNeededItems() {
		neededItems.clear();

		TreeMap<ItemStack, Integer> computeStacks = new TreeMap<ItemStack, Integer>(new Comparator<ItemStack>() {

			@Override
			public int compare(ItemStack o1, ItemStack o2) {
				if (o1.itemID > o2.itemID)
					return 1;
				else if (o1.itemID < o2.itemID)
					return -1;
				else if (o1.getItemDamage() > o2.getItemDamage())
					return 1;
				else if (o1.getItemDamage() < o2.getItemDamage())
					return -1;

				return 0;
			}
		});

		for (BptSlot slot : primaryList) {

			LinkedList<ItemStack> stacks = new LinkedList<ItemStack>();

			try {
				stacks = slot.getRequirements(context);
			} catch (Throwable t) {
				// Defensive code against errors in implementers
				t.printStackTrace();
				BCLog.logger.throwing("BptBuilderBlueprint", "recomputeIfNeeded", t);
			}

			for (ItemStack stack : stacks) {
				if (stack == null || stack.itemID == 0) {
					continue;
				}

				if (!computeStacks.containsKey(stack)) {
					computeStacks.put(stack.copy(), stack.stackSize);
				} else {
					Integer num = computeStacks.get(stack);
					num += stack.stackSize;

					computeStacks.put(stack, num);
				}

			}
		}

		for (BptSlot slot : secondaryList) {
			LinkedList<ItemStack> stacks = slot.getRequirements(context);

			for (ItemStack stack : stacks) {
				if (stack == null || stack.itemID <= 0 || stack.itemID >= Item.itemsList.length || stack.stackSize == 0 || stack.getItem() == null) {
					continue;
				}

				if (!computeStacks.containsKey(stack)) {
					computeStacks.put(stack.copy(), stack.stackSize);
				} else {
					Integer num = computeStacks.get(stack);
					num += stack.stackSize;

					computeStacks.put(stack, num);
				}

			}
		}

		for (ItemStack stack : computeStacks.keySet())
			if (stack.isItemStackDamageable()) {
				neededItems.add(new ItemStack(stack.getItem()));
			} else {
				neededItems.add(new ItemStack(stack.itemID, computeStacks.get(stack), stack.getItemDamage()));
			}
	}

	@Override
	public void postProcessing(World world) {
		for (BptSlot s : postProcessingList) {
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
