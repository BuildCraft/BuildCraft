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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.TreeSet;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.blueprints.Schematic;
import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.api.blueprints.SchematicEntity;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.IInvSlot;
import buildcraft.api.core.StackKey;
import buildcraft.builders.TileAbstractBuilder;
import buildcraft.core.BlockIndex;
import buildcraft.core.blueprints.BuildingSlotBlock.Mode;
import buildcraft.core.inventory.InventoryCopy;
import buildcraft.core.inventory.InventoryIterator;
import buildcraft.core.inventory.StackHelper;
import buildcraft.core.utils.BlockUtil;

public class BptBuilderBlueprint extends BptBuilderBase {

	private LinkedList<BuildingSlotBlock> buildList = new LinkedList<BuildingSlotBlock>();
	private LinkedList<BuildingSlotEntity> entityList = new LinkedList<BuildingSlotEntity>();
	private LinkedList<BuildingSlot> postProcessing = new LinkedList<BuildingSlot>();

	protected TreeSet <Integer> builtEntities = new TreeSet <Integer> ();

	private BuildingSlotIterator iterator;

	public LinkedList <ItemStack> neededItems = new LinkedList <ItemStack> ();

	public BptBuilderBlueprint(Blueprint bluePrint, World world, int x, int y, int z) {
		super(bluePrint, world, x, y, z);
	}

	@Override
	protected void initialize () {
		for (int j = blueprint.sizeY - 1; j >= 0; --j) {
			for (int i = 0; i < blueprint.sizeX; ++i) {
				for (int k = 0; k < blueprint.sizeZ; ++k) {
					int xCoord = i + x - blueprint.anchorX;
					int yCoord = j + y - blueprint.anchorY;
					int zCoord = k + z - blueprint.anchorZ;

					if (yCoord < 0 || yCoord >= context.world.getHeight()) {
						continue;
					}

					if (!clearedLocations.contains(new BlockIndex(
									xCoord, yCoord, zCoord))) {
						SchematicBlock slot = (SchematicBlock) blueprint.contents[i][j][k];

						if (slot == null && !blueprint.excavate) {
							continue;
						}

						if (slot == null) {
							slot = new SchematicBlock();
							slot.meta = 0;
							slot.block = Blocks.air;
						}

						BuildingSlotBlock b = new BuildingSlotBlock();
						b.schematic = slot;
						b.x = xCoord;
						b.y = yCoord;
						b.z = zCoord;
						b.mode = Mode.ClearIfInvalid;
						b.buildStage = 0;

						buildList.add(b);
					}

				}
			}
		}

		LinkedList<BuildingSlotBlock> tmpStandalone = new LinkedList<BuildingSlotBlock>();
		LinkedList<BuildingSlotBlock> tmpLastBlocks = new LinkedList<BuildingSlotBlock>();

		for (int j = 0; j < blueprint.sizeY; ++j) {
			for (int i = 0; i < blueprint.sizeX; ++i) {
				for (int k = 0; k < blueprint.sizeZ; ++k) {
					int xCoord = i + x - blueprint.anchorX;
					int yCoord = j + y - blueprint.anchorY;
					int zCoord = k + z - blueprint.anchorZ;

					SchematicBlock slot = (SchematicBlock) blueprint.contents[i][j][k];

					if (slot == null || yCoord < 0 || yCoord >= context.world.getHeight()) {
						continue;
					}

					BuildingSlotBlock b = new BuildingSlotBlock();
					b.schematic = slot;
					b.x = xCoord;
					b.y = yCoord;
					b.z = zCoord;
					b.mode = Mode.Build;

					if (!builtLocations.contains(new BlockIndex(xCoord, yCoord,
								zCoord))) {

						if (slot.isStandalone()) {
							tmpStandalone.add(b);
							b.buildStage = 1;
						} else {
							tmpLastBlocks.add(b);
							b.buildStage = 2;
						}
					} else {
						postProcessing.add(b);
					}
				}
			}
		}

		buildList.addAll(tmpStandalone);
		buildList.addAll(tmpLastBlocks);

		iterator = new BuildingSlotIterator(buildList);

		int seqId = 0;

		for (SchematicEntity e : ((Blueprint) blueprint).entities) {

			BuildingSlotEntity b = new BuildingSlotEntity();
			b.schematic = e;
			b.sequenceNumber = seqId;

			if (!builtEntities.contains(seqId)) {
				entityList.add(b);
			} else {
				postProcessing.add(b);
			}

			seqId++;
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
			} else {
				return null;
			}
		}

		if (entityList.size() != 0) {
			BuildingSlot slot = internalGetNextEntity(world, inv, entityList);
			checkDone ();

			if (slot != null) {
				return slot;
			} else {
				return null;
			}
		}

		checkDone();

		return null;
	}

	private BuildingSlot internalGetNextBlock(World world, TileAbstractBuilder builder, LinkedList<BuildingSlotBlock> list) {
		iterator.startIteration();

		while (iterator.hasNext()) {
			BuildingSlotBlock slot = iterator.next();

			if (slot.buildStage > buildList.getFirst().buildStage) {
				iterator.reset ();
				return null;
			}

			try {
				if (!slot.isAlreadyBuilt(context)) {
					if (slot.mode == Mode.ClearIfInvalid) {
						if (BuildCraftAPI.isSoftBlock(world, slot.x, slot.y,
								slot.z)
								|| BlockUtil.isUnbreakableBlock(world, slot.x,
										slot.y, slot.z)) {
							iterator.remove();
						} else {
							if (setupForDestroy(builder, context, slot)) {
								iterator.remove();
								clearedLocations.add(new BlockIndex(slot.x,
										slot.y, slot.z));
								return slot;
							}
						}
					} else if (!slot.schematic.doNotBuild()) {
						if (checkRequirements(builder, slot.schematic)) {
							// At this stage, regardless of the fact that the
							// block can actually be built or not, we'll try.
							// When the item reaches the actual block, we'll
							// verify that the location is indeed clear, and
							// avoid building otherwise.
							useRequirements(builder, slot);

							iterator.remove();
							postProcessing.add(slot);
							builtLocations.add(new BlockIndex(slot.x,
									slot.y, slot.z));
							return slot;
						}
					} else {
						// Even slots that don't need to be build may need
						// post processing, see below for the argument.
						postProcessing.add(slot);
						iterator.remove();
					}
				} else {
					if (slot.mode == Mode.Build) {
						// Even slots that considered already built may need
						// post processing calls. For example, flowing water
						// may need to be adjusted, engines may need to be
						// turned to the right direction, etc.
						postProcessing.add(slot);
					}

					iterator.remove();
				}
			} catch (Throwable t) {
				// Defensive code against errors in implementers
				t.printStackTrace();
				BCLog.logger.throwing("BptBuilderBlueprint",
						"internalGetBlock", t);
				iterator.remove();
			}
		}

		return null;
	}

	private BuildingSlot internalGetNextEntity(World world,
			TileAbstractBuilder builder, LinkedList<BuildingSlotEntity> list) {
		Iterator<BuildingSlotEntity> it = list.iterator();

		while (it.hasNext()) {
			BuildingSlotEntity slot = it.next();

			if (slot.isAlreadyBuilt(context)) {
				it.remove();
			} else {
				if (checkRequirements(builder, slot.schematic)) {
					useRequirements(builder, slot);

					it.remove();
					postProcessing.add(slot);
					builtEntities.add(slot.sequenceNumber);
					return slot;
				}
			}
		}

		return null;
	}

	public boolean checkRequirements(TileAbstractBuilder builder, Schematic slot) {
		double energyRequired = 0;

		LinkedList<ItemStack> tmpReq = new LinkedList<ItemStack>();

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

		if (context.world().getWorldInfo().getGameType() == GameType.CREATIVE) {
			return true;
		}

		for (ItemStack reqStk : tmpReq) {
			for (IInvSlot slotInv : InventoryIterator.getIterable(new InventoryCopy(builder), ForgeDirection.UNKNOWN)) {
				if (!builder.isBuildingMaterialSlot(slotInv.getIndex())) {

				}

				ItemStack invStk = slotInv.getStackInSlot();

				if (invStk != null && invStk.stackSize > 0 && StackHelper.isCraftingEquivalent(reqStk, invStk, true)) {
					try {
						slot.useItem(context, reqStk, slotInv);
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

	public void useRequirements(TileAbstractBuilder builder, BuildingSlot slot) {
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

		if (context.world ().getWorldInfo().getGameType() == GameType.CREATIVE) {
			for (ItemStack s : slot.getRequirements(context)) {
				slot.addStackConsumed(s);
			}

			return;
		}

		ListIterator<ItemStack> itr = tmpReq.listIterator();

		while (itr.hasNext()) {
			ItemStack reqStk = itr.next();
			boolean smallStack = reqStk.stackSize == 1;
			ItemStack usedStack = reqStk;

			for (IInvSlot slotInv : InventoryIterator.getIterable(builder, ForgeDirection.UNKNOWN)) {
				if (!builder.isBuildingMaterialSlot(slotInv.getIndex())) {

				}

				ItemStack invStk = slotInv.getStackInSlot();

				if (invStk != null && invStk.stackSize > 0 && StackHelper.isCraftingEquivalent(reqStk, invStk, true)) {
					try {
						usedStack = slot.getSchematic().useItem(context, reqStk, slotInv);
						slot.addStackConsumed (usedStack);
					} catch (Throwable t) {
						// Defensive code against errors in implementers
						t.printStackTrace();
						BCLog.logger.throwing("BptBuilderBlueprint", "useRequirements", t);
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

		for (BuildingSlotEntity slot : entityList) {
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

	@Override
	public void saveBuildStateToNBT (NBTTagCompound nbt, TileAbstractBuilder builder) {
		super.saveBuildStateToNBT(nbt, builder);

		int [] entitiesBuiltArr = new int [builtEntities.size()];

		int id = 0;

		for (Integer i : builtEntities) {
			entitiesBuiltArr [id] = i;
			id++;
		}

		nbt.setIntArray("builtEntities", entitiesBuiltArr);
	}

	@Override
	public void loadBuildStateToNBT (NBTTagCompound nbt, TileAbstractBuilder builder) {
		super.loadBuildStateToNBT(nbt, builder);

		int [] entitiesBuiltArr = nbt.getIntArray("builtEntities");

		for (int i = 0; i < entitiesBuiltArr.length; ++i) {
			builtEntities.add(i);
		}
	}

}
