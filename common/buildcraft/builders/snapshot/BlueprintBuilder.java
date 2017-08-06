/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.schematics.ISchematicEntity;

import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.net.PacketBufferBC;

public class BlueprintBuilder extends SnapshotBuilder<ITileForBlueprintBuilder> {
    private static final double MAX_ENTITY_DISTANCE = 0.1D;
    public List<ItemStack> remainingDisplayRequired = new ArrayList<>();

    public BlueprintBuilder(ITileForBlueprintBuilder tile) {
        super(tile);
    }

    private Blueprint.BuildingInfo getBuildingInfo() {
        return tile.getBlueprintBuildingInfo();
    }

    private Stream<ItemStack> getDisplayRequired(List<ItemStack> requiredItems, List<FluidStack> requiredFluids) {
        return Stream.concat(
            requiredItems == null ? Stream.empty() : requiredItems.stream(),
            requiredFluids == null ? Stream.empty() : requiredFluids.stream()
                .map(FluidStack::getFluid)
                .map(BlockUtil::getBucketFromFluid)
        );
    }

    private Optional<List<ItemStack>> tryExtractRequired(List<ItemStack> requiredItems,
                                                         List<FluidStack> requiredFluids,
                                                         boolean simulate) {
        if (StackUtil.mergeSameItems(requiredItems).stream()
            .noneMatch(stack ->
                tile.getInvResources().extract(
                    extracted -> StackUtil.canMerge(stack, extracted),
                    stack.getCount(),
                    stack.getCount(),
                    true
                ).isEmpty()
            ) &&
            FluidUtilBC.mergeSameFluids(requiredFluids).stream()
                .allMatch(stack ->
                    FluidUtilBC.areFluidStackEqual(stack, tile.getTankManager().drain(stack, false))
                )) {
            return Optional.of(
                StackUtil.mergeSameItems(
                    Stream.concat(
                        requiredItems.stream()
                            .map(stack ->
                                tile.getInvResources().extract(
                                    extracted -> StackUtil.canMerge(stack, extracted),
                                    stack.getCount(),
                                    stack.getCount(),
                                    simulate
                                )
                            ),
                        FluidUtilBC.mergeSameFluids(requiredFluids).stream()
                            .map(fluidStack -> tile.getTankManager().drain(fluidStack, !simulate))
                            .map(fluidStack -> {
                                ItemStack stack = BlockUtil.getBucketFromFluid(fluidStack.getFluid());
                                if (!stack.hasTagCompound()) {
                                    stack.setTagCompound(new NBTTagCompound());
                                }
                                // noinspection ConstantConditions
                                stack.getTagCompound().setTag(
                                    "BuilderFluidStack",
                                    fluidStack.writeToNBT(new NBTTagCompound())
                                );
                                return stack;
                            })
                    ).collect(Collectors.toList())
                )
            );
        } else {
            return Optional.empty();
        }
    }

    @Override
    protected Set<BlockPos> getToBreak() {
        return Optional.ofNullable(getBuildingInfo())
            .map(buildingInfo -> buildingInfo.toBreak)
            .orElse(Collections.emptySet());
    }

    @Override
    protected Set<BlockPos> getToPlace() {
        return Optional.ofNullable(getBuildingInfo())
            .map(buildingInfo -> getBuildingInfo().toPlace.keySet())
            .orElse(Collections.emptySet());
    }

    @Override
    protected boolean canPlace(BlockPos blockPos) {
        return !getBuildingInfo().toPlace.get(blockPos).isAir() &&
            getBuildingInfo().toPlace.get(blockPos).canBuild(tile.getWorldBC(), blockPos);
    }

    @Override
    protected boolean readyToPlace(BlockPos blockPos) {
        return getBuildingInfo().toPlace.get(blockPos).getRequiredBlockOffsets().stream()
            .map(blockPos::add)
            .allMatch(pos ->
                getBuildingInfo().toPlace.containsKey(pos)
                    ? checkResults.get(CheckResult.CORRECT).contains(pos)
                    : !getToBreak().contains(pos) || tile.getWorldBC().isAirBlock(pos)
            );
    }

    @Override
    protected boolean hasEnoughToPlaceItems(BlockPos blockPos) {
        return Optional.ofNullable(getBuildingInfo()).flatMap(buildingInfo ->
            tryExtractRequired(
                buildingInfo.toPlaceRequiredItems.get(blockPos),
                buildingInfo.toPlaceRequiredFluids.get(blockPos),
                true
            )
        ).isPresent();
    }

    @Override
    protected List<ItemStack> getToPlaceItems(BlockPos blockPos) {
        return Optional.ofNullable(getBuildingInfo()).flatMap(buildingInfo ->
            tryExtractRequired(
                buildingInfo.toPlaceRequiredItems.get(blockPos),
                buildingInfo.toPlaceRequiredFluids.get(blockPos),
                false
            )
        ).orElseThrow(IllegalStateException::new);
    }

    @Override
    protected void cancelPlaceTask(PlaceTask placeTask) {
        super.cancelPlaceTask(placeTask);
        // noinspection ConstantConditions
        placeTask.items.stream()
            .filter(stack -> !stack.hasTagCompound() || !stack.getTagCompound().hasKey("BuilderFluidStack"))
            .forEach(stack -> tile.getInvResources().insert(stack, false, false));
        // noinspection ConstantConditions
        placeTask.items.stream()
            .filter(stack -> stack.hasTagCompound() && stack.getTagCompound().hasKey("BuilderFluidStack"))
            .map(stack -> Pair.of(stack.getCount(), stack.getTagCompound().getCompoundTag("BuilderFluidStack")))
            .map(countNbt -> {
                FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(countNbt.getRight());
                if (fluidStack != null) {
                    fluidStack.amount *= countNbt.getLeft();
                }
                return fluidStack;
            })
            .forEach(fluidStack -> tile.getTankManager().fill(fluidStack, true));
    }

    @Override
    protected boolean isBlockCorrect(BlockPos blockPos) {
        return getBuildingInfo() != null &&
            getBuildingInfo().toPlace.containsKey(blockPos) &&
            getBuildingInfo().toPlace.get(blockPos).isBuilt(tile.getWorldBC(), blockPos);
    }

    @Override
    protected boolean doPlaceTask(PlaceTask placeTask) {
        return getBuildingInfo() != null &&
            getBuildingInfo().toPlace.get(placeTask.pos) != null &&
            getBuildingInfo().toPlace.get(placeTask.pos).build(tile.getWorldBC(), placeTask.pos);
    }

    @Override
    public Box getBox() {
        return Optional.ofNullable(getBuildingInfo())
            .map(Blueprint.BuildingInfo::getBox)
            .orElse(null);
    }

    @Override
    public boolean tick() {
        if (tile.getWorldBC().isRemote) {
            return super.tick();
        }
        return Optional.ofNullable(getBuildingInfo()).map(buildingInfo -> {
            tile.getWorldBC().profiler.startSection("entitiesWithinBox");
            List<Entity> entitiesWithinBox = tile.getWorldBC().getEntitiesWithinAABB(
                Entity.class,
                buildingInfo.getBox().getBoundingBox(),
                Objects::nonNull
            );
            tile.getWorldBC().profiler.endSection();
            tile.getWorldBC().profiler.startSection("toSpawn");
            List<ISchematicEntity<?>> toSpawn = buildingInfo.entities.stream()
                .filter(schematicEntity ->
                    entitiesWithinBox.stream()
                        .map(Entity::getPositionVector)
                        .map(schematicEntity.getPos().add(new Vec3d(buildingInfo.basePos))::distanceTo)
                        .noneMatch(distance -> distance < MAX_ENTITY_DISTANCE)
                )
                .collect(Collectors.toList());
            tile.getWorldBC().profiler.endSection();
            // Compute needed stacks
            tile.getWorldBC().profiler.startSection("remainingDisplayRequired");
            remainingDisplayRequired.clear();
            remainingDisplayRequired.addAll(StackUtil.mergeSameItems(
                Stream.concat(
                    getToPlace().stream()
                        .filter(blockPos -> !checkResults.get(CheckResult.CORRECT).contains(blockPos))
                        .flatMap(blockPos ->
                            getDisplayRequired(
                                buildingInfo.toPlaceRequiredItems.get(blockPos),
                                buildingInfo.toPlaceRequiredFluids.get(blockPos)
                            )
                        ),
                    toSpawn.stream()
                        .flatMap(schematicEntity ->
                            getDisplayRequired(
                                buildingInfo.entitiesRequiredItems.get(schematicEntity),
                                buildingInfo.entitiesRequiredFluids.get(schematicEntity)
                            )
                        )
                ).collect(Collectors.toList())
            ));
            tile.getWorldBC().profiler.endSection();
            // Kill not needed entities
            tile.getWorldBC().profiler.startSection("toKill");
            List<Entity> toKill = entitiesWithinBox.stream()
                .filter(entity ->
                    entity != null &&
                        buildingInfo.entities.stream()
                            .map(ISchematicEntity::getPos)
                            .map(new Vec3d(buildingInfo.basePos)::add)
                            .map(entity.getPositionVector()::distanceTo)
                            .noneMatch(distance -> distance < MAX_ENTITY_DISTANCE) &&
                        SchematicEntityManager.getSchematicEntity(
                            tile.getWorldBC(),
                            BlockPos.ORIGIN,
                            entity
                        ) != null
                )
                .collect(Collectors.toList());
            if (!toKill.isEmpty()) {
                if (!tile.getBattery().isFull()) {
                    return false;
                } else {
                    tile.getWorldBC().profiler.startSection("kill");
                    toKill.forEach(Entity::setDead);
                    tile.getWorldBC().profiler.endSection();
                }
            }
            tile.getWorldBC().profiler.endSection();
            // Call superclass method
            if (super.tick()) {
                // Spawn needed entities
                if (!toSpawn.isEmpty()) {
                    if (!tile.getBattery().isFull()) {
                        return false;
                    } else {
                        tile.getWorldBC().profiler.startSection("spawn");
                        toSpawn.stream()
                            .filter(schematicEntity ->
                                tryExtractRequired(
                                    buildingInfo.entitiesRequiredItems.get(schematicEntity),
                                    buildingInfo.entitiesRequiredFluids.get(schematicEntity),
                                    true
                                ).isPresent()
                            )
                            .filter(schematicEntity ->
                                schematicEntity.build(tile.getWorldBC(), buildingInfo.basePos) != null
                            )
                            .forEach(schematicEntity ->
                                tryExtractRequired(
                                    buildingInfo.entitiesRequiredItems.get(schematicEntity),
                                    buildingInfo.entitiesRequiredFluids.get(schematicEntity),
                                    false
                                )
                            );
                        tile.getWorldBC().profiler.endSection();
                    }
                }
                return true;
            } else {
                return false;
            }
        }).orElseGet(super::tick);
    }

    @Override
    public void writeToByteBuf(PacketBufferBC buffer) {
        super.writeToByteBuf(buffer);
        buffer.writeInt(remainingDisplayRequired.size());
        remainingDisplayRequired.forEach(stack -> {
            buffer.writeItemStack(stack);
            buffer.writeInt(stack.getCount());
        });
    }

    @Override
    public void readFromByteBuf(PacketBufferBC buffer) {
        super.readFromByteBuf(buffer);
        remainingDisplayRequired.clear();
        IntStream.range(0, buffer.readInt()).mapToObj(i -> {
            ItemStack stack;
            try {
                stack = buffer.readItemStack();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            stack.setCount(buffer.readInt());
            return stack;
        }).forEach(remainingDisplayRequired::add);
    }
}
