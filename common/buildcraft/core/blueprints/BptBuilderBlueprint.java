/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.blueprints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.TreeSet;

import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.api.blueprints.Schematic;
import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.api.blueprints.SchematicEntity;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.IInvSlot;
import buildcraft.api.core.StackKey;
import buildcraft.builders.TileBuilder;
import buildcraft.core.builders.BuildingSlot;
import buildcraft.core.builders.BuildingSlotBlock;
import buildcraft.core.builders.BuildingSlotBlock.Mode;
import buildcraft.core.builders.BuildingSlotEntity;
import buildcraft.core.builders.BuildingSlotIterator;
import buildcraft.core.builders.IBuildingItemsProvider;
import buildcraft.core.builders.TileAbstractBuilder;
import buildcraft.core.inventory.InventoryCopy;
import buildcraft.core.inventory.InventoryIterator;
import buildcraft.core.inventory.StackHelper;
import buildcraft.core.utils.BlockUtils;

public class BptBuilderBlueprint extends BptBuilderBase {

	public ArrayList<ItemStack> neededItems = new ArrayList<ItemStack>();

	protected TreeSet<Integer> builtEntities = new TreeSet<Integer>();

	private LinkedList<BuildingSlotBlock> buildList = new LinkedList<BuildingSlotBlock>();
	private LinkedList<BuildingSlotEntity> entityList = new LinkedList<BuildingSlotEntity>();
	private LinkedList<BuildingSlot> postProcessing = new LinkedList<BuildingSlot>();
	private BuildingSlotIterator iterator;

	public BptBuilderBlueprint(Blueprint bluePrint, World world, BlockPos pos) {
		super(bluePrint, world, pos);
	}

	@Override
	protected void internalInit () {
		for (int j = blueprint.sizeY - 1; j >= 0; --j) {
			for (int i = 0; i < blueprint.sizeX; ++i) {
				for (int k = 0; k < blueprint.sizeZ; ++k) {
					int xCoord = i + x - blueprint.anchorX;
					int yCoord = j + y - blueprint.anchorY;
					int zCoord = k + z - blueprint.anchorZ;
					BlockPos pos = new BlockPos(xCoord, yCoord, zCoord);

					if (yCoord < 0 || yCoord >= context.world.getHeight()) {
						continue;
					}

					if (!clearedLocations.contains(pos)) {
						SchematicBlock slot = (SchematicBlock) blueprint.contents[i][j][k];

						if (slot == null && !blueprint.excavate) {
							continue;
						}

						if (slot == null) {
							slot = new SchematicBlock();
							slot.state = Blocks.air.getDefaultState();
						}

						if (!SchematicRegistry.INSTANCE.isAllowedForBuilding(slot.state)) {
							continue;
						}

						BuildingSlotBlock b = new BuildingSlotBlock();
						b.schematic = slot;
						b.pos = pos;
						b.mode = Mode.ClearIfInvalid;
						b.buildStage = 0;

						buildList.add(b);
					}

				}
			}
		}

		LinkedList<BuildingSlotBlock> tmpStandalone = new LinkedList<BuildingSlotBlock>();
		LinkedList<BuildingSlotBlock> tmpSupported = new LinkedList<BuildingSlotBlock>();
		LinkedList<BuildingSlotBlock> tmpExpanding = new LinkedList<BuildingSlotBlock>();

		for (int j = 0; j < blueprint.sizeY; ++j) {
			for (int i = 0; i < blueprint.sizeX; ++i) {
				for (int k = 0; k < blueprint.sizeZ; ++k) {
					int xCoord = i + x - blueprint.anchorX;
					int yCoord = j + y - blueprint.anchorY;
					int zCoord = k + z - blueprint.anchorZ;
					BlockPos pos = new BlockPos(xCoord, yCoord, zCoord);

					SchematicBlock slot = (SchematicBlock) blueprint.contents[i][j][k];

					if (slot == null || yCoord < 0 || yCoord >= context.world.getHeight()) {
						continue;
					}

					if (!SchematicRegistry.INSTANCE.isAllowedForBuilding(slot.state)) {
						continue;
					}

					BuildingSlotBlock b = new BuildingSlotBlock();
					b.schematic = slot;
					b.pos = pos;
					b.mode = Mode.Build;

					if (!builtLocations.contains(pos)) {
						switch (slot.getBuildStage()) {
						case STANDALONE:
							tmpStandalone.add(b);
							b.buildStage = 1;
							break;
						case SUPPORTED:
							tmpSupported.add(b);
							b.buildStage = 2;
							break;
						case EXPANDING:
							tmpExpanding.add(b);
							b.buildStage = 3;
							break;
						}
					} else {
						postProcessing.add(b);
					}
				}
			}
		}

		buildList.addAll(tmpStandalone);
		buildList.addAll(tmpSupported);
		buildList.addAll(tmpExpanding);

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

	public void deploy () {
		initialize();

		for (BuildingSlotBlock b : buildList) {
			if (b.mode == Mode.ClearIfInvalid) {
				context.world.setBlockToAir(b.pos);
			} else if (!b.schematic.doNotBuild()) {
				b.stackConsumed = new LinkedList<ItemStack>();

				try {
					for (ItemStack stk : b.getRequirements(context)) {
						if (stk != null) {
							b.stackConsumed.add(stk.copy());
						}
					}
				} catch (Throwable t) {
					// Defensive code against errors in implementers
					t.printStackTrace();
					BCLog.logger.throwing(t);
				}

				b.writeToWorld(context);
			}
		}

		for (BuildingSlotEntity e : entityList) {
			e.stackConsumed = new LinkedList<ItemStack>();

			try {
				for (ItemStack stk : e.getRequirements(context)) {
					if (stk != null) {
						e.stackConsumed.add(stk.copy());
					}
				}
			} catch (Throwable t) {
				// Defensive code against errors in implementers
				t.printStackTrace();
				BCLog.logger.throwing(t);
			}

			e.writeToWorld(context);
		}

		for (BuildingSlotBlock b : buildList) {
			if (b.mode != Mode.ClearIfInvalid) {
				b.postProcessing(context);
			}
		}

		for (BuildingSlotEntity e : entityList) {
			e.postProcessing(context);
		}
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
	public BuildingSlot reserveNextBlock(World world) {
		if (buildList.size() != 0) {
			BuildingSlot slot = internalGetNextBlock(world, null);

			if (slot != null) {
				slot.reserved = true;
			}

			return slot;
		}

		return null;
	}

	@Override
	public BuildingSlot getNextBlock(World world, TileAbstractBuilder inv) {
		if (buildList.size() != 0) {
			BuildingSlot slot = internalGetNextBlock(world, inv);
			checkDone();

			if (slot != null) {
				return slot;
			} else {
				return null;
			}
		}

		if (entityList.size() != 0) {
			BuildingSlot slot = internalGetNextEntity(world, inv);
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

	/**
	 * Gets the next available block. If builder is not null, then building will
	 * be verified and performed. Otherwise, the next possible building slot is
	 * returned, possibly for reservation, with no building.
	 */
	private BuildingSlot internalGetNextBlock(World world, TileAbstractBuilder builder) {
		if (builder != null && builder.energyAvailable() < BuilderAPI.BREAK_ENERGY) {
			// If there's no more energy available, then set reset the list and
			// quit. This will avoid situations where energy is given at a
			// random point in time, and therefore builder doesn't start from
			// the expected point.

			iterator.reset();
			return null;
		}

		iterator.startIteration();

		while (iterator.hasNext()) {
			BuildingSlotBlock slot = iterator.next();

			if (slot.buildStage > buildList.getFirst().buildStage) {
				iterator.reset();
				return null;
			}

			if (slot.built) {
				iterator.remove();

				if (slot.mode == Mode.ClearIfInvalid) {
					clearedLocations.add(slot.pos);
				} else {
					builtLocations.add(slot.pos);
				}

				postProcessing.add(slot);

				continue;
			}

			if (slot.reserved) {
				continue;
			}

			try {
				if (BlockUtils.isUnbreakableBlock(world, slot.pos)) {
					// if the block can't be broken, just forget this iterator
					iterator.remove();

					if (slot.mode == Mode.ClearIfInvalid) {
						clearedLocations.add(slot.pos);
					} else {
						builtLocations.add(slot.pos);
					}
				} else if (!slot.isAlreadyBuilt(context)) {
					if (slot.mode == Mode.ClearIfInvalid) {
						if (BuildCraftAPI.isSoftBlock(world, slot.pos)) {
							iterator.remove();
							clearedLocations.add(slot.pos);
						} else {
							if (builder == null) {
								createDestroyItems(slot);
								return slot;
							} else if (canDestroy(builder, context, slot)) {
								consumeEnergyToDestroy(builder, slot);
								createDestroyItems(slot);

								iterator.remove();
								clearedLocations.add(slot.pos);
								return slot;
							}
						}
					} else if (!slot.schematic.doNotBuild()) {
						if (builder == null) {
							return slot;
						} else if (checkRequirements(builder, slot.schematic)) {
							// At this stage, regardless of the fact that the
							// block can actually be built or not, we'll try.
							// When the item reaches the actual block, we'll
							// verify that the location is indeed clear, and
							// avoid building otherwise.
							builder.consumeEnergy(slot.getEnergyRequirement());
							useRequirements(builder, slot);

							iterator.remove();
							postProcessing.add(slot);
							builtLocations.add(slot.pos);
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
				BCLog.logger.throwing(t);
				iterator.remove();
			}
		}

		return null;
	}

	private BuildingSlot internalGetNextEntity(World world, TileAbstractBuilder builder) {
		Iterator<BuildingSlotEntity> it = entityList.iterator();

		while (it.hasNext()) {
			BuildingSlotEntity slot = it.next();

			if (slot.isAlreadyBuilt(context)) {
				it.remove();
			} else {
				if (checkRequirements(builder, slot.schematic)) {
					builder.consumeEnergy(slot.getEnergyRequirement());
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
		LinkedList<ItemStack> tmpReq = new LinkedList<ItemStack>();

		try {
			LinkedList<ItemStack> req = new LinkedList<ItemStack>();

			slot.getRequirementsForPlacement(context, req);

			for (ItemStack stk : req) {
				if (stk != null) {
					tmpReq.add(stk.copy());
				}
			}
		} catch (Throwable t) {
			// Defensive code against errors in implementers
			t.printStackTrace();
			BCLog.logger.throwing(t);
		}

		LinkedList<ItemStack> stacksUsed = new LinkedList<ItemStack>();

		if (context.world().getWorldInfo().getGameType() == GameType.CREATIVE) {
			for (ItemStack s : tmpReq) {
				stacksUsed.add(s);
			}

			return !(builder.energyAvailable() < slot.getEnergyRequirement(stacksUsed));
		}

		for (ItemStack reqStk : tmpReq) {
			boolean itemBlock = reqStk.getItem() instanceof ItemBlock;
			Fluid fluid = itemBlock ? FluidRegistry.lookupFluidForBlock(((ItemBlock) reqStk.getItem()).getBlock()) : null;

			if (fluid != null && builder instanceof TileBuilder && ((TileBuilder) builder).drainBuild(new FluidStack(fluid, FluidContainerRegistry.BUCKET_VOLUME), true)) {
				continue;
			}

			for (IInvSlot slotInv : InventoryIterator.getIterable(new InventoryCopy(builder), null)) {
				if (!builder.isBuildingMaterialSlot(slotInv.getIndex())) {
					continue;
				}

				ItemStack invStk = slotInv.getStackInSlot();
				if (invStk == null || invStk.stackSize == 0) {
					continue;
				}

				FluidStack fluidStack = fluid != null ? FluidContainerRegistry.getFluidForFilledItem(invStk) : null;
				boolean compatibleContainer = fluidStack != null && fluidStack.getFluid() == fluid && fluidStack.amount >= FluidContainerRegistry.BUCKET_VOLUME;

				if (invStk.hasTagCompound() == reqStk.hasTagCompound() &&
						(!invStk.hasTagCompound() || invStk.getTagCompound().equals(reqStk.getTagCompound())) &&
						(StackHelper.isCraftingEquivalent(reqStk, invStk, true) || compatibleContainer)) {
					try {
						stacksUsed.add(slot.useItem(context, reqStk, slotInv));
					} catch (Throwable t) {
						// Defensive code against errors in implementers
						t.printStackTrace();
						BCLog.logger.throwing(t);
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

		return !(builder.energyAvailable() < slot.getEnergyRequirement(stacksUsed));
	}

	@Override
	public void useRequirements(IInventory inv, BuildingSlot slot) {
		if (slot instanceof BuildingSlotBlock && ((BuildingSlotBlock) slot).mode == Mode.ClearIfInvalid) {
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
			BCLog.logger.throwing(t);

		}

		if (context.world ().getWorldInfo().getGameType() == GameType.CREATIVE) {
			for (ItemStack s : tmpReq) {
				slot.addStackConsumed(s);
			}

			return;
		}

		ListIterator<ItemStack> itr = tmpReq.listIterator();

		while (itr.hasNext()) {
			ItemStack reqStk = itr.next();
			boolean smallStack = reqStk.stackSize == 1;
			ItemStack usedStack = reqStk;

			boolean itemBlock = reqStk.getItem() instanceof ItemBlock;
			Fluid fluid = itemBlock ? FluidRegistry.lookupFluidForBlock(((ItemBlock) reqStk.getItem()).getBlock()) : null;

			if (fluid != null
					&& inv instanceof TileBuilder
					&& ((TileBuilder) inv)
							.drainBuild(new FluidStack(fluid, FluidContainerRegistry.BUCKET_VOLUME), true)) {
				continue;
			}

			for (IInvSlot slotInv : InventoryIterator.getIterable(inv, null)) {
				if (inv instanceof TileAbstractBuilder &&
						!((TileAbstractBuilder) inv).isBuildingMaterialSlot(slotInv.getIndex())) {
					continue;
				}

				ItemStack invStk = slotInv.getStackInSlot();

				if (invStk == null || invStk.stackSize == 0) {
					continue;
				}

				FluidStack fluidStack = fluid != null ? FluidContainerRegistry.getFluidForFilledItem(invStk) : null;
				boolean fluidFound = fluidStack != null && fluidStack.getFluid() == fluid && fluidStack.amount >= FluidContainerRegistry.BUCKET_VOLUME;

				if (fluidFound || StackHelper.isCraftingEquivalent(reqStk, invStk, true)) {
					try {
						usedStack = slot.getSchematic().useItem(context, reqStk, slotInv);
						slot.addStackConsumed (usedStack);
					} catch (Throwable t) {
						// Defensive code against errors in implementers
						t.printStackTrace();
						BCLog.logger.throwing(t);
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
	}

	public void recomputeNeededItems() {
		neededItems.clear();

		HashMap<StackKey, Integer> computeStacks = new HashMap<StackKey, Integer>();

		for (BuildingSlot slot : buildList) {
			LinkedList<ItemStack> stacks = new LinkedList<ItemStack>();

			try {
				stacks = slot.getRequirements(context);
			} catch (Throwable t) {
				// Defensive code against errors in implementers
				t.printStackTrace();
				BCLog.logger.throwing(t);
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
				BCLog.logger.throwing(t);
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
				BCLog.logger.throwing(t);
			}
		}
	}

	@Override
	public void saveBuildStateToNBT(NBTTagCompound nbt, IBuildingItemsProvider builder) {
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
	public void loadBuildStateToNBT(NBTTagCompound nbt, IBuildingItemsProvider builder) {
		super.loadBuildStateToNBT(nbt, builder);

		int [] entitiesBuiltArr = nbt.getIntArray("builtEntities");

		for (int i = 0; i < entitiesBuiltArr.length; ++i) {
			builtEntities.add(i);
		}
	}

}
