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

import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.api.blueprints.SchematicBlockBase;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.IInvSlot;
import buildcraft.core.builders.BuildingSlot;
import buildcraft.core.builders.BuildingSlotBlock;
import buildcraft.core.builders.BuildingSlotBlock.Mode;
import buildcraft.core.builders.BuildingSlotIterator;
import buildcraft.core.builders.TileAbstractBuilder;
import buildcraft.core.inventory.InventoryIterator;
import buildcraft.core.utils.BlockUtils;

public class BptBuilderTemplate extends BptBuilderBase {

	private LinkedList<BuildingSlotBlock> buildList = new LinkedList<BuildingSlotBlock>();
	private BuildingSlotIterator iterator;

	public BptBuilderTemplate(BlueprintBase bluePrint, World world, BlockPos pos) {
		super(bluePrint, world, pos);
	}

	@Override
	protected void internalInit () {
		if (blueprint.excavate) {
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

						SchematicBlockBase slot = blueprint.contents[i][j][k];

						if (slot == null
								&& !clearedLocations.contains(pos)) {
							BuildingSlotBlock b = new BuildingSlotBlock();

							b.schematic = null;
							b.pos = pos;
							b.mode = Mode.ClearIfInvalid;
							b.buildStage = 0;

							buildList.add(b);
						}
					}
				}
			}
		}

		for (int j = 0; j < blueprint.sizeY; ++j) {
			for (int i = 0; i < blueprint.sizeX; ++i) {
				for (int k = 0; k < blueprint.sizeZ; ++k) {
					int xCoord = i + x - blueprint.anchorX;
					int yCoord = j + y - blueprint.anchorY;
					int zCoord = k + z - blueprint.anchorZ;
					BlockPos pos = new BlockPos(xCoord, yCoord, zCoord);

					if (yCoord < 0 || yCoord >= context.world.getHeight()) {
						continue;
					}

					SchematicBlockBase slot = blueprint.contents[i][j][k];

					if (slot != null && !builtLocations.contains(pos)) {
						BuildingSlotBlock b = new BuildingSlotBlock();

						b.schematic = slot;
						b.pos = pos;

						b.mode = Mode.Build;
						b.buildStage = 1;

						buildList.add(b);
					}
				}
			}
		}

		iterator = new BuildingSlotIterator(buildList);
	}

	private void checkDone() {
		if (buildList.size() == 0) {
			done = true;
		} else {
			done = false;
		}
	}

	@Override
	public BuildingSlot reserveNextBlock(World world) {
		return null;
	}

	@Override
	public BuildingSlot getNextBlock(World world, TileAbstractBuilder inv) {
		if (buildList.size() != 0) {
			BuildingSlotBlock slot = internalGetNextBlock(world, inv);
			checkDone();

			if (slot != null) {
				return slot;
			}
		}

		checkDone();

		return null;
	}

	private BuildingSlotBlock internalGetNextBlock(World world, TileAbstractBuilder builder) {
		BuildingSlotBlock result = null;

		IInvSlot firstSlotToConsume = null;

		for (IInvSlot invSlot : InventoryIterator.getIterable(builder, null)) {
			if (!builder.isBuildingMaterialSlot(invSlot.getIndex())) {
				continue;
			}

			ItemStack stack = invSlot.getStackInSlot();

			if (stack != null && stack.stackSize > 0) {
				firstSlotToConsume = invSlot;
				break;
			}
		}

		iterator.startIteration();

		while (iterator.hasNext()) {
			BuildingSlotBlock slot = iterator.next();

			if (slot.buildStage > buildList.getFirst().buildStage) {
				iterator.reset ();
				return null;
			}

			if (BlockUtils.isUnbreakableBlock(world, slot.pos)) {
				iterator.remove();
				if (slot.mode == Mode.ClearIfInvalid) {
					clearedLocations.add(slot.pos);
				} else {
					builtLocations.add(slot.pos);
				}
			} else if (slot.mode == Mode.ClearIfInvalid) {
				if (BuildCraftAPI.isSoftBlock(world, slot.pos)) {
					iterator.remove();
					clearedLocations.add(slot.pos);
				} else {
					if (canDestroy(builder, context, slot)) {
						consumeEnergyToDestroy(builder, slot);
						createDestroyItems(slot);

						result = slot;
						iterator.remove();
						clearedLocations.add(slot.pos);

						break;
					}
				}
			} else if (slot.mode == Mode.Build) {
				if (!BuildCraftAPI.isSoftBlock(world, slot.pos)) {
					iterator.remove();
					builtLocations.add(slot.pos);
				} else {
					if (builder.consumeEnergy(BuilderAPI.BUILD_ENERGY) && firstSlotToConsume != null) {
						slot.addStackConsumed(firstSlotToConsume.decreaseStackInSlot(1));
						result = slot;
						iterator.remove();
						builtLocations.add(slot.pos);

						break;
					}
				}
			}
		}

		return result;
	}
}
