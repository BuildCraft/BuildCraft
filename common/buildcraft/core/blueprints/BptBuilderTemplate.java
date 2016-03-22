/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.blueprints;

import java.util.LinkedList;

import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.world.World;

import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.api.blueprints.SchematicBlockBase;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.IInvSlot;
import buildcraft.core.builders.BuildingSlot;
import buildcraft.core.builders.BuildingSlotBlock;
import buildcraft.core.builders.BuildingSlotBlock.Mode;
import buildcraft.core.builders.BuildingSlotIterator;
import buildcraft.core.builders.TileAbstractBuilder;
import buildcraft.core.lib.inventory.InventoryIterator;
import buildcraft.core.lib.utils.BlockUtils;
import buildcraft.core.lib.utils.Utils;

public class BptBuilderTemplate extends BptBuilderBase {

    private LinkedList<BuildingSlotBlock> clearList = new LinkedList<>();
    private LinkedList<BuildingSlotBlock> buildList = new LinkedList<>();
    private BuildingSlotIterator iteratorBuild, iteratorClear;

    public BptBuilderTemplate(BlueprintBase bluePrint, World world, BlockPos pos) {
        super(bluePrint, world, pos);
    }

    @Override
    protected void internalInit() {
        BlockPos worldOffset = pos.subtract(blueprint.anchor);
        BlockPos bptMin = BlockPos.ORIGIN;
        if (worldOffset.getY() < 0) bptMin = Utils.withValue(bptMin, Axis.Y, -worldOffset.getY());

        BlockPos bptMax = blueprint.size.subtract(Utils.POS_ONE);
        if (worldOffset.add(bptMax).getY() >= context.world().getHeight()) {
            bptMax = Utils.withValue(bptMax, Axis.Y, context.world().getHeight() - worldOffset.getY());
        }
        /* Check to make sure the max is bigger than the min- if its not it means that the size was 0 for one of the
         * axis */
        if (Utils.min(bptMin, bptMax).equals(bptMin) && Utils.max(bptMin, bptMax).equals(bptMax)) {
            if (blueprint.excavate) {
                for (BlockPos bptOffset : BlockPos.getAllInBox(bptMin, bptMax)) {
                    BlockPos pointWorldOffset = worldOffset.add(bptOffset);

                    SchematicBlockBase slot = blueprint.get(bptOffset);

                    if (slot == null && !isLocationUsed(pointWorldOffset)) {
                        BuildingSlotBlock b = new BuildingSlotBlock();

                        b.schematic = null;
                        b.pos = pointWorldOffset;
                        b.mode = Mode.ClearIfInvalid;
                        b.buildStage = 0;

                        clearList.add(b);
                    }
                }
            }
            for (BlockPos bptOffset : BlockPos.getAllInBox(bptMin, bptMax)) {
                BlockPos pointWorldOffset = worldOffset.add(bptOffset);

                SchematicBlockBase slot = blueprint.get(bptOffset);

                if (slot != null && !isLocationUsed(pointWorldOffset)) {
                    BuildingSlotBlock b = new BuildingSlotBlock();

                    b.schematic = slot;
                    b.pos = pointWorldOffset;

                    b.mode = Mode.Build;
                    b.buildStage = 1;

                    buildList.add(b);
                }
            }
        }

        iteratorBuild = new BuildingSlotIterator(buildList);
        iteratorClear = new BuildingSlotIterator(clearList);
    }

    private void checkDone() {
        if (buildList.size() == 0 && clearList.size() == 0) {
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
        if (buildList.size() != 0 || clearList.size() != 0) {
            BuildingSlotBlock slot = internalGetNextBlock(world, inv);
            checkDone();

            if (slot != null) {
                return slot;
            }
        } else {
            checkDone();
        }

        return null;
    }

    private BuildingSlotBlock internalGetNextBlock(World world, TileAbstractBuilder builder) {
        BuildingSlotBlock result = null;

        IInvSlot firstSlotToConsume = null;

        for (IInvSlot invSlot : InventoryIterator.getIterable(builder)) {
            if (!builder.isBuildingMaterialSlot(invSlot.getIndex())) {
                continue;
            }

            ItemStack stack = invSlot.getStackInSlot();

            if (stack != null && stack.stackSize > 0) {
                firstSlotToConsume = invSlot;
                break;
            }
        }

        // Step 1: Check the cleared
        iteratorClear.startIteration();
        while (iteratorClear.hasNext()) {
            BuildingSlotBlock slot = iteratorClear.next();

            if (slot.buildStage > clearList.getFirst().buildStage) {
                iteratorClear.reset();
                break;
            }

            // if (world.isAirBlock(slot.pos)) {
            // continue;
            // }

            if (canDestroy(builder, context, slot)) {
                if (BlockUtils.isUnbreakableBlock(world, slot.pos) || isBlockBreakCanceled(world, slot.pos) || BuildCraftAPI.isSoftBlock(world,
                        slot.pos)) {
                    iteratorClear.remove();
                    markLocationUsed(slot.pos);
                } else {
                    consumeEnergyToDestroy(builder, slot);
                    createDestroyItems(slot);

                    result = slot;
                    iteratorClear.remove();
                    markLocationUsed(slot.pos);
                    break;
                }
            }
        }

        if (result != null) {
            return result;
        }

        // Step 2: Check the built, but only if we have anything to place and enough energy
        if (firstSlotToConsume == null) {
            return null;
        }

        iteratorBuild.startIteration();

        while (iteratorBuild.hasNext()) {
            BuildingSlotBlock slot = iteratorBuild.next();

            if (slot.buildStage > buildList.getFirst().buildStage) {
                iteratorBuild.reset();
                break;
            }

            if (BlockUtils.isUnbreakableBlock(world, slot.pos) || isBlockPlaceCanceled(world, slot.pos, slot.schematic) || !BuildCraftAPI.isSoftBlock(
                    world, slot.pos)) {
                iteratorBuild.remove();
                markLocationUsed(slot.pos);
            } else if (builder.consumeEnergy(BuilderAPI.BUILD_ENERGY)) {
                slot.addStackConsumed(firstSlotToConsume.decreaseStackInSlot(1));
                result = slot;
                iteratorBuild.remove();
                markLocationUsed(slot.pos);
                break;
            }
        }

        return result;
    }
}
