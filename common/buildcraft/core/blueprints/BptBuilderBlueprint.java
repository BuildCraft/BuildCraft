/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.blueprints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;

import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings.GameType;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.api.blueprints.Schematic;
import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.api.blueprints.SchematicEntity;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.BlockIndex;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.IInvSlot;
import buildcraft.api.core.StackKey;
import buildcraft.core.builders.BuilderItemMetaPair;
import buildcraft.core.builders.BuildingSlot;
import buildcraft.core.builders.BuildingSlotBlock;
import buildcraft.core.builders.BuildingSlotBlock.Mode;
import buildcraft.core.builders.BuildingSlotEntity;
import buildcraft.core.builders.IBuildingItemsProvider;
import buildcraft.core.builders.TileAbstractBuilder;
import buildcraft.core.lib.inventory.InventoryCopy;
import buildcraft.core.lib.inventory.InventoryIterator;
import buildcraft.core.lib.utils.BlockUtils;

public class BptBuilderBlueprint extends BptBuilderBase {
	protected HashSet<Integer> builtEntities = new HashSet<Integer>();
	protected HashMap<BuilderItemMetaPair, List<BuildingSlotBlock>> buildList = new HashMap<BuilderItemMetaPair, List<BuildingSlotBlock>>();
	protected int[] buildStageOccurences;

	private ArrayList<RequirementItemStack> neededItems = new ArrayList<RequirementItemStack>();

	private LinkedList<BuildingSlotEntity> entityList = new LinkedList<BuildingSlotEntity>();
	private LinkedList<BuildingSlot> postProcessing = new LinkedList<BuildingSlot>();
	private BuildingSlotMapIterator iterator;
	private IndexRequirementMap requirementMap = new IndexRequirementMap();

	public BptBuilderBlueprint(Blueprint bluePrint, World world, int x, int y, int z) {
		super(bluePrint, world, x, y, z);
	}

	@Override
	protected void internalInit() {
		for (int j = blueprint.sizeY - 1; j >= 0; --j) {
			int yCoord = j + y - blueprint.anchorY;

			if (yCoord < 0 || yCoord >= context.world.getHeight()) {
				continue;
			}

			for (int i = 0; i < blueprint.sizeX; ++i) {
				int xCoord = i + x - blueprint.anchorX;

				for (int k = 0; k < blueprint.sizeZ; ++k) {
					int zCoord = k + z - blueprint.anchorZ;

					if (!isLocationUsed(xCoord, yCoord, zCoord)) {
						SchematicBlock slot = (SchematicBlock) blueprint.get(i, j, k);

						if (slot == null && !blueprint.excavate) {
							continue;
						}

						if (slot == null) {
							slot = new SchematicBlock();
							slot.meta = 0;
							slot.block = Blocks.air;
						}

						if (!SchematicRegistry.INSTANCE.isAllowedForBuilding(slot.block, slot.meta)) {
							continue;
						}

						BuildingSlotBlock b = new BuildingSlotBlock();
						b.schematic = slot;
						b.x = xCoord;
						b.y = yCoord;
						b.z = zCoord;
						b.mode = Mode.ClearIfInvalid;
						b.buildStage = 0;

						addToBuildList(b);
					}
				}
			}
		}

		LinkedList<BuildingSlotBlock> tmpStandalone = new LinkedList<BuildingSlotBlock>();
		LinkedList<BuildingSlotBlock> tmpExpanding = new LinkedList<BuildingSlotBlock>();

		for (int j = 0; j < blueprint.sizeY; ++j) {
			int yCoord = j + y - blueprint.anchorY;

			if (yCoord < 0 || yCoord >= context.world.getHeight()) {
				continue;
			}

			for (int i = 0; i < blueprint.sizeX; ++i) {
				int xCoord = i + x - blueprint.anchorX;

				for (int k = 0; k < blueprint.sizeZ; ++k) {
					int zCoord = k + z - blueprint.anchorZ;

					SchematicBlock slot = (SchematicBlock) blueprint.get(i, j, k);

					if (slot == null) {
						continue;
					}

					if (!SchematicRegistry.INSTANCE.isAllowedForBuilding(slot.block, slot.meta)) {
						continue;
					}

					BuildingSlotBlock b = new BuildingSlotBlock();
					b.schematic = slot;
					b.x = xCoord;
					b.y = yCoord;
					b.z = zCoord;
					b.mode = Mode.Build;

					if (!isLocationUsed(xCoord, yCoord, zCoord)) {
						switch (slot.getBuildStage()) {
							case STANDALONE:
								tmpStandalone.add(b);
								b.buildStage = 1;
								break;
							case EXPANDING:
								tmpExpanding.add(b);
								b.buildStage = 2;
								break;
						}
					} else {
						postProcessing.add(b);
					}
				}
			}
		}

		for (BuildingSlotBlock b : tmpStandalone) {
			addToBuildList(b);
		}
		for (BuildingSlotBlock b : tmpExpanding) {
			addToBuildList(b);
		}

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

	public void deploy() {
		initialize();

		for (List<BuildingSlotBlock> lb : buildList.values()) {
			for (BuildingSlotBlock b : lb) {
				if (b.mode == Mode.ClearIfInvalid) {
					context.world.setBlockToAir(b.x, b.y, b.z);
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

		for (List<BuildingSlotBlock> lb : buildList.values()) {
			for (BuildingSlotBlock b : lb) {
				if (b.mode != Mode.ClearIfInvalid) {
					b.postProcessing(context);
				}
			}
		}

		for (BuildingSlotEntity e : entityList) {
			e.postProcessing(context);
		}
	}

	private void checkDone() {
		if (getBuildListCount() == 0 && entityList.size() == 0) {
			done = true;
		} else {
			done = false;
		}
	}

	private int getBuildListCount() {
		int out = 0;
		if (buildStageOccurences != null) {
			for (int i = 0; i < buildStageOccurences.length; i++) {
				out += buildStageOccurences[i];
			}
		}
		return out;
	}

	@Override
	public BuildingSlot reserveNextBlock(World world) {
		if (getBuildListCount() != 0) {
			BuildingSlot slot = internalGetNextBlock(world, null);
			checkDone();

			if (slot != null) {
				slot.reserved = true;
			}

			return slot;
		}

		return null;
	}

	private void addToBuildList(BuildingSlotBlock b) {
		if (b != null) {
			BuilderItemMetaPair imp = new BuilderItemMetaPair(context, b);
			if (!buildList.containsKey(imp)) {
				buildList.put(imp, new ArrayList<BuildingSlotBlock>());
			}
			buildList.get(imp).add(b);

			if (buildStageOccurences == null) {
				buildStageOccurences = new int[Math.max(3, b.buildStage + 1)];
			} else if (buildStageOccurences.length <= b.buildStage) {
				int[] newBSO = new int[b.buildStage + 1];
				System.arraycopy(buildStageOccurences, 0, newBSO, 0, buildStageOccurences.length);
				buildStageOccurences = newBSO;
			}
			buildStageOccurences[b.buildStage]++;

			if (b.mode == Mode.Build) {
				requirementMap.add(b, context);
				b.internalRequirementRemovalListener = requirementMap;
			}
		}
	}

	@Override
	public BuildingSlot getNextBlock(World world, TileAbstractBuilder inv) {
		if (getBuildListCount() != 0) {
			BuildingSlot slot = internalGetNextBlock(world, inv);
			checkDone();
			return slot;
		}

		if (entityList.size() != 0) {
			BuildingSlot slot = internalGetNextEntity(world, inv);
			checkDone();
			return slot;
		}

		checkDone();
		return null;
	}

	protected boolean readyForSlotLookup(TileAbstractBuilder builder) {
		return builder == null || builder.energyAvailable() >= BuilderAPI.BREAK_ENERGY;
	}

	/**
	 * Gets the next available block. If builder is not null, then building will
	 * be verified and performed. Otherwise, the next possible building slot is
	 * returned, possibly for reservation, with no building.
	 */
	private BuildingSlot internalGetNextBlock(World world, TileAbstractBuilder builder) {
		if (!readyForSlotLookup(builder)) {
			return null;
		}

		if (iterator == null) {
			iterator = new BuildingSlotMapIterator(this, builder);
		}

		BuildingSlotBlock slot;
		iterator.refresh(builder);

		while (readyForSlotLookup(builder) && (slot = iterator.next()) != null) {
			if (!world.blockExists(slot.x, slot.y, slot.z)) {
				continue;
			}

			boolean skipped = false;

			for (int i = 0; i < slot.buildStage; i++) {
				if (buildStageOccurences[i] > 0) {
					iterator.skipKey();
					skipped = true;
					break;
				}
			}

			if (skipped) {
				continue;
			}

			if (slot.built) {
				iterator.remove();
				markLocationUsed(slot.x, slot.y, slot.z);
				postProcessing.add(slot);

				continue;
			}

			if (slot.reserved) {
				continue;
			}

			try {
				if (slot.isAlreadyBuilt(context)) {
					if (slot.mode == Mode.Build) {
						requirementMap.remove(slot);

						// Even slots that considered already built may need
						// post processing calls. For example, flowing water
						// may need to be adjusted, engines may need to be
						// turned to the right direction, etc.
						postProcessing.add(slot);
					}

					iterator.remove();
					continue;
				}

				if (BlockUtils.isUnbreakableBlock(world, slot.x, slot.y, slot.z)) {
					// if the block can't be broken, just forget this iterator
					iterator.remove();
					markLocationUsed(slot.x, slot.y, slot.z);
					requirementMap.remove(slot);
				} else {
					if (slot.mode == Mode.ClearIfInvalid) {
						if (BuildCraftAPI.isSoftBlock(world, slot.x, slot.y,
								slot.z)
								|| isBlockBreakCanceled(world, slot.x, slot.y, slot.z)) {
							iterator.remove();
							markLocationUsed(slot.x, slot.y, slot.z);
						} else {
							if (builder == null) {
								createDestroyItems(slot);
								return slot;
							} else if (canDestroy(builder, context, slot)) {
								consumeEnergyToDestroy(builder, slot);
								createDestroyItems(slot);

								iterator.remove();
								markLocationUsed(slot.x, slot.y, slot.z);
								return slot;
							}
						}
					} else if (!slot.schematic.doNotBuild()) {
						if (builder == null) {
							return slot;
						} else if (checkRequirements(builder, slot.schematic)) {
							if (!BuildCraftAPI.isSoftBlock(world, slot.x, slot.y,
									slot.z) || requirementMap.contains(new BlockIndex(slot.x, slot.y, slot.z))) {
								continue; // Can't build yet, wait (#2751)
							} else if (isBlockPlaceCanceled(world, slot.x, slot.y, slot.z, slot.schematic)) {
								// Forge does not allow us to place a block in
								// this position.
								iterator.remove();
								requirementMap.remove(slot);
								markLocationUsed(slot.x, slot.y, slot.z);
								continue;
							}

							// At this stage, regardless of the fact that the
							// block can actually be built or not, we'll try.
							// When the item reaches the actual block, we'll
							// verify that the location is indeed clear, and
							// avoid building otherwise.
							builder.consumeEnergy(slot.getEnergyRequirement());
							useRequirements(builder, slot);

							iterator.remove();
							markLocationUsed(slot.x, slot.y, slot.z);
							postProcessing.add(slot);
							return slot;
						}
					} else {
						// Even slots that don't need to be build may need
						// post processing, see above for the argument.
						postProcessing.add(slot);
						requirementMap.remove(slot);
						iterator.remove();
					}
				}
			} catch (Throwable t) {
				// Defensive code against errors in implementers
				t.printStackTrace();
				BCLog.logger.throwing(t);
				iterator.remove();
				requirementMap.remove(slot);
			}
		}

		return null;
	}

	// TODO: Remove recomputeNeededItems() and replace with something more efficient
	private BuildingSlot internalGetNextEntity(World world, TileAbstractBuilder builder) {
		Iterator<BuildingSlotEntity> it = entityList.iterator();

		while (it.hasNext()) {
			BuildingSlotEntity slot = it.next();

			if (slot.isAlreadyBuilt(context)) {
				it.remove();
				recomputeNeededItems();
			} else {
				if (checkRequirements(builder, slot.schematic)) {
					builder.consumeEnergy(slot.getEnergyRequirement());
					useRequirements(builder, slot);

					it.remove();
					recomputeNeededItems();
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

		IInventory invCopy = new InventoryCopy(builder);

		for (ItemStack reqStk : tmpReq) {
			boolean itemBlock = reqStk.getItem() instanceof ItemBlock;
			Fluid fluid = itemBlock ? FluidRegistry.lookupFluidForBlock(((ItemBlock) reqStk.getItem()).field_150939_a) : null;

			if (fluid != null && builder.drainBuild(new FluidStack(fluid, FluidContainerRegistry.BUCKET_VOLUME), true)) {
				continue;
			}

			for (IInvSlot slotInv : InventoryIterator.getIterable(invCopy, ForgeDirection.UNKNOWN)) {
				if (!builder.isBuildingMaterialSlot(slotInv.getIndex())) {
					continue;
				}

				ItemStack invStk = slotInv.getStackInSlot();
				if (invStk == null || invStk.stackSize == 0) {
					continue;
				}

				FluidStack fluidStack = fluid != null ? FluidContainerRegistry.getFluidForFilledItem(invStk) : null;
				boolean compatibleContainer = fluidStack != null && fluidStack.getFluid() == fluid && fluidStack.amount >= FluidContainerRegistry.BUCKET_VOLUME;

				if (slot.isItemMatchingRequirement(invStk, reqStk) || compatibleContainer) {
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

		return builder.energyAvailable() >= slot.getEnergyRequirement(stacksUsed);
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

		if (context.world().getWorldInfo().getGameType() == GameType.CREATIVE) {
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
			Fluid fluid = itemBlock ? FluidRegistry.lookupFluidForBlock(((ItemBlock) reqStk.getItem()).field_150939_a) : null;

			if (fluid != null
					&& inv instanceof TileAbstractBuilder
					&& ((TileAbstractBuilder) inv).drainBuild(new FluidStack(fluid, FluidContainerRegistry.BUCKET_VOLUME), true)) {
				continue;
			}

			for (IInvSlot slotInv : InventoryIterator.getIterable(inv, ForgeDirection.UNKNOWN)) {
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

				if (fluidFound || slot.getSchematic().isItemMatchingRequirement(invStk, reqStk)) {
					try {
						usedStack = slot.getSchematic().useItem(context, reqStk, slotInv);
						slot.addStackConsumed(usedStack);
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

	public List<RequirementItemStack> getNeededItems() {
		return neededItems;
	}

	protected void onRemoveBuildingSlotBlock(BuildingSlotBlock slot) {
		buildStageOccurences[slot.buildStage]--;
		LinkedList<ItemStack> stacks = new LinkedList<ItemStack>();

		try {
			stacks = slot.getRequirements(context);
		} catch (Throwable t) {
			// Defensive code against errors in implementers
			t.printStackTrace();
			BCLog.logger.throwing(t);
		}

		HashMap<StackKey, Integer> computeStacks = new HashMap<StackKey, Integer>();

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

		for (RequirementItemStack ris : neededItems) {
			StackKey stackKey = new StackKey(ris.stack);
			if (computeStacks.containsKey(stackKey)) {
				Integer num = computeStacks.get(stackKey);
				if (ris.size <= num) {
					recomputeNeededItems();
					return;
				} else {
					neededItems.set(neededItems.indexOf(ris), new RequirementItemStack(ris.stack, ris.size - num));
				}
			}
		}

		sortNeededItems();
	}

	private void sortNeededItems() {
		Collections.sort(neededItems, new Comparator<RequirementItemStack>() {
			@Override
			public int compare(RequirementItemStack o1, RequirementItemStack o2) {
				if (o1.size != o2.size) {
					return o1.size < o2.size ? 1 : -1;
				} else {
					ItemStack os1 = o1.stack;
					ItemStack os2 = o2.stack;
					if (Item.getIdFromItem(os1.getItem()) > Item.getIdFromItem(os2.getItem())) {
						return -1;
					} else if (Item.getIdFromItem(os1.getItem()) < Item.getIdFromItem(os2.getItem())) {
						return 1;
					} else if (os1.getItemDamage() > os2.getItemDamage()) {
						return -1;
					} else if (os1.getItemDamage() < os2.getItemDamage()) {
						return 1;
					} else {
						return 0;
					}
				}
			}
		});
	}

	private void recomputeNeededItems() {
		neededItems.clear();

		HashMap<StackKey, Integer> computeStacks = new HashMap<StackKey, Integer>();

		for (List<BuildingSlotBlock> lb : buildList.values()) {
			for (BuildingSlotBlock slot : lb) {
				if (slot == null) {
					continue;
				}

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
			neededItems.add(new RequirementItemStack(e.getKey().stack.copy(), e.getValue()));
		}

		sortNeededItems();
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

		int[] entitiesBuiltArr = new int[builtEntities.size()];

		int id = 0;

		for (Integer i : builtEntities) {
			entitiesBuiltArr[id] = i;
			id++;
		}

		nbt.setIntArray("builtEntities", entitiesBuiltArr);
	}

	@Override
	public void loadBuildStateToNBT(NBTTagCompound nbt, IBuildingItemsProvider builder) {
		super.loadBuildStateToNBT(nbt, builder);

		int[] entitiesBuiltArr = nbt.getIntArray("builtEntities");

		for (int i = 0; i < entitiesBuiltArr.length; ++i) {
			builtEntities.add(i);
		}
	}
}
