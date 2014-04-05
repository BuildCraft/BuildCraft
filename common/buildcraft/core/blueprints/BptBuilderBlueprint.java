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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings.GameType;
import buildcraft.api.blueprints.CoordTransformation;
import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.api.blueprints.SchematicEntity;
import buildcraft.api.core.StackKey;
import buildcraft.builders.TileAbstractBuilder;
import buildcraft.core.blueprints.BuildingSlotBlock.Mode;
import buildcraft.core.utils.BCLog;
import buildcraft.core.utils.BlockUtil;

public class BptBuilderBlueprint extends BptBuilderBase {

	private LinkedList<BuildingSlotBlock> buildList = new LinkedList<BuildingSlotBlock>();
	private LinkedList<BuildingSlotEntity> entityList = new LinkedList<BuildingSlotEntity>();
	private LinkedList<BuildingSlot> postProcessing = new LinkedList<BuildingSlot>();

	private BuildingSlotIterator iterator;

	public LinkedList <ItemStack> neededItems = new LinkedList <ItemStack> ();

	public BptBuilderBlueprint(Blueprint bluePrint, World world, int x, int y, int z) {
		super(bluePrint, world, x, y, z);

		for (int j = bluePrint.sizeY - 1; j >= 0; --j) {
			for (int i = 0; i < bluePrint.sizeX; ++i) {
				for (int k = 0; k < bluePrint.sizeZ; ++k) {
					int xCoord = i + x - blueprint.anchorX;
					int yCoord = j + y - blueprint.anchorY;
					int zCoord = k + z - blueprint.anchorZ;

					SchematicBlock slot = (SchematicBlock) bluePrint.contents[i][j][k];

					if (slot == null) {
						slot = new SchematicBlock();
						slot.meta = 0;
						slot.block = Blocks.air;
					}

					BuildingSlotBlock b = new BuildingSlotBlock ();
					b.schematic = slot;
					b.x = xCoord;
					b.y = yCoord;
					b.z = zCoord;
					b.mode = Mode.ClearIfInvalid;

					buildList.add(b);

				}
			}
		}

		for (int j = 0; j < bluePrint.sizeY; ++j) {
			for (int i = 0; i < bluePrint.sizeX; ++i) {
				for (int k = 0; k < bluePrint.sizeZ; ++k) {
					int xCoord = i + x - blueprint.anchorX;
					int yCoord = j + y - blueprint.anchorY;
					int zCoord = k + z - blueprint.anchorZ;

					SchematicBlock slot = (SchematicBlock) bluePrint.contents[i][j][k];

					if (slot == null) {
						slot = new SchematicBlock();
						slot.meta = 0;
						slot.block = Blocks.air;
					}

					BuildingSlotBlock b = new BuildingSlotBlock ();
					b.schematic = slot;
					b.x = xCoord;
					b.y = yCoord;
					b.z = zCoord;
					b.mode = Mode.Build;

					buildList.add (b);
				}
			}
		}

		Collections.sort(buildList);

		iterator = new BuildingSlotIterator(buildList);

		CoordTransformation transform = new CoordTransformation();

		transform.x = x - blueprint.anchorX;
		transform.y = y - blueprint.anchorY;
		transform.z = z - blueprint.anchorZ;

		for (SchematicEntity e : bluePrint.entities) {
			BuildingSlotEntity b = new BuildingSlotEntity();
			b.schematic = e;
			b.transform = transform;

			entityList.add(b);
		}

		recomputeNeededItems();
	}

	private void checkDone() {
		recomputeNeededItems();

		if (buildList.size() == 0 && entityList.size() == 0) {
			done = true;
		} else {
			done = false;
		}
	}

	@Override
	public BuildingSlot getNextBlock(World world, TileAbstractBuilder inv) {
		if (buildList.size() != 0) {
			BuildingSlot slot = internalGetNextBlock(world, inv, buildList);
			checkDone();

			if (slot != null) {
				return slot;
			}
		}

		if (entityList.size() != 0) {
			BuildingSlot slot = entityList.removeFirst();
			checkDone ();

			if (slot != null) {
				return slot;
			}
		}

		checkDone();

		return null;
	}

	private BuildingSlot internalGetNextBlock(World world, TileAbstractBuilder builder, LinkedList<BuildingSlotBlock> list) {
		iterator.startIteration();

		while (iterator.hasNext()) {
			BuildingSlotBlock slot = iterator.next();

			boolean getNext = false;

			try {
				getNext = !slot.schematic.ignoreBuilding()
						&& !slot.schematic.isValid(context, slot.x, slot.y,
								slot.z);
			} catch (Throwable t) {
				// Defensive code against errors in implementers
				t.printStackTrace();
				BCLog.logger.throwing("BptBuilderBlueprint", "internalGetBlock", t);
				getNext = false;
			}

			if (getNext) {
				if (slot.mode == Mode.ClearIfInvalid) {
					if (!BlockUtil.isSoftBlock(world, slot.x, slot.y, slot.z)) {
						if (setupForDestroy(builder, context, slot)) {
							iterator.remove();
							postProcessing.add(slot);
							return slot;
						}
					}
				} else if (world.getWorldInfo().getGameType() == GameType.CREATIVE) {
					// In creative, we don't use blocks or energy from the builder

					iterator.remove();
					postProcessing.add(slot);
					return slot;
				} else if (checkRequirements(builder, (SchematicBlock) slot.schematic)) {
					useRequirements(builder, slot);

					iterator.remove();
					postProcessing.add(slot);
					return slot;
				}
			} else {
				iterator.remove();
			}
		}

		return null;
	}

	public boolean checkRequirements(TileAbstractBuilder builder, SchematicBlock slot) {
		if (slot.block == null) {
			return true;
		}

		double energyRequired = 0;

		LinkedList<ItemStack> tmpReq = new LinkedList<ItemStack>();
		LinkedList<ItemStack> tmpInv = new LinkedList<ItemStack>();

		try {
			for (ItemStack stk : slot.getRequirements(context)) {
				if (stk != null) {
					tmpReq.add(stk.copy());
					energyRequired += stk.stackSize * TileAbstractBuilder.BUILD_ENERGY;
				}
			}
		} catch (Throwable t) {
			// Defensive code against errors in implementers
			t.printStackTrace();
			BCLog.logger.throwing("BptBuilderBlueprint", "checkRequirements", t);
		}

		if (builder.energyAvailable() < energyRequired) {
			return false;
		}

		int size = builder.getSizeInventory();
		for (int i = 0; i < size; ++i) {
			if (!builder.isBuildingMaterialSlot(i)) {
				continue;
			}

			if (builder.getStackInSlot(i) != null) {
				tmpInv.add(builder.getStackInSlot(i).copy());
			}
		}

		for (ItemStack reqStk : tmpReq) {
			for (ItemStack invStk : tmpInv) {
				if (invStk != null && invStk.stackSize > 0 && isEqual (reqStk, invStk)) {
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

			if (reqStk.stackSize != 0) {
				return false;
			}
		}

		return true;
	}

	public void useRequirements(TileAbstractBuilder builder, BuildingSlotBlock slot) {
		LinkedList<ItemStack> tmpReq = new LinkedList<ItemStack>();

		double energyRequired = 0;

		try {
			for (ItemStack stk : slot.getRequirements(context)) {
				if (stk != null) {
					tmpReq.add(stk.copy());
					energyRequired += stk.stackSize * TileAbstractBuilder.BUILD_ENERGY;
				}
			}
		} catch (Throwable t) {
			// Defensive code against errors in implementers
			t.printStackTrace();
			BCLog.logger.throwing("BptBuilderBlueprint", "useRequirements", t);

		}

		builder.consumeEnergy(energyRequired);

		ListIterator<ItemStack> itr = tmpReq.listIterator();

		while (itr.hasNext()) {
			ItemStack reqStk = itr.next();
			boolean smallStack = reqStk.stackSize == 1;
			ItemStack usedStack = reqStk;
			int size = builder.getSizeInventory();
			for (int i = 0; i < size; ++i) {
				if (!builder.isBuildingMaterialSlot(i)) {

				}

				ItemStack invStk = builder.getStackInSlot(i);

				if (invStk != null && invStk.stackSize > 0 && isEqual (reqStk, invStk)) {
					try {
						usedStack = slot.getSchematic().useItem(context, reqStk, invStk);
						slot.addStackConsumed (usedStack);
					} catch (Throwable t) {
						// Defensive code against errors in implementers
						t.printStackTrace();
						BCLog.logger.throwing("BptBuilderBlueprint", "useRequirements", t);
					}

					if (invStk.stackSize == 0) {
						builder.setInventorySlotContents(i, null);
					} else {
						builder.setInventorySlotContents(i, invStk);
					}

					if (reqStk.stackSize == 0) {
						break;
					}
				}
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

	private boolean isEqual(ItemStack reqStk, ItemStack invStk) {
		if (reqStk.getItem() != invStk.getItem()) {
			return false;
		} else if (!invStk.isItemStackDamageable() && (reqStk.getItemDamage() != invStk.getItemDamage())) {
			return false;
		} else if (reqStk.stackTagCompound != null && invStk.stackTagCompound == null) {
			return false;
		} else if (reqStk.stackTagCompound == null && invStk.stackTagCompound != null) {
			return false;
		} else if (reqStk.stackTagCompound != null && !reqStk.stackTagCompound.equals(invStk.stackTagCompound)) {
			return false;
		} else {
			return true;
		}
	}


	public void recomputeNeededItems() {
		neededItems.clear();

		HashMap <StackKey, Integer> computeStacks = new HashMap <StackKey, Integer> ();

		for (BuildingSlot slot : buildList) {
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

		for (Entry<StackKey, Integer> e : computeStacks.entrySet()) {
			ItemStack newStack = e.getKey().stack.copy();
			newStack.stackSize = e.getValue();
			neededItems.add(newStack);
		}


		LinkedList <ItemStack> sortedList = new LinkedList <ItemStack> ();

		for (ItemStack toInsert : neededItems) {
			int index = 0;
			boolean didInsert = false;

			for (ItemStack inserted : sortedList) {
				if (inserted.stackSize < toInsert.stackSize) {
					sortedList.add(index, toInsert);
					didInsert = true;
					break;
				}

				index++;
			}

			if (!didInsert) {
				sortedList.addLast(toInsert);
			}
		}


		Collections.sort (neededItems, new Comparator<ItemStack>() {
			@Override
			public int compare(ItemStack o1, ItemStack o2) {
				if (o1.stackSize > o2.stackSize) {
					return -1;
				} else if (o1.stackSize < o2.stackSize) {
					return 1;
				} else if (Item.getIdFromItem(o1.getItem()) > Item.getIdFromItem(o2.getItem())) {
					return -1;
				}  else if (Item.getIdFromItem(o1.getItem()) < Item.getIdFromItem(o2.getItem())) {
					return 1;
				}  else if (o1.getItemDamage() > o2.getItemDamage()) {
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
		for (BuildingSlot s : postProcessing) {
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
