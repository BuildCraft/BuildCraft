/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
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

import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.ISchematicEntity;

import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.net.PacketBufferBC;

public class BlueprintBuilder extends SnapshotBuilder<ITileForBlueprintBuilder> {
    private static final double MAX_ENTITY_DISTANCE = 0.1D;

    private List<ItemStack>[] remainingDisplayRequiredBlocks;
    private List<ItemStack> remainingDisplayRequiredBlocksConcat = Collections.emptyList();
    public List<ItemStack> remainingDisplayRequired = new ArrayList<>();
    private final Map<Pair<List<ItemStack>, List<FluidStack>>, Optional<List<ItemStack>>> extractRequiredCache =
        new HashMap<>();

    public BlueprintBuilder(ITileForBlueprintBuilder tile) {
        super(tile);
    }

    private ISchematicBlock<?> getSchematicBlock(BlockPos blockPos) {
        BlockPos snapshotPos = getBuildingInfo().fromWorld(blockPos);
        return getBuildingInfo().box.contains(blockPos) ? getBuildingInfo().getSnapshot().palette.get(
            getBuildingInfo().getSnapshot().data[getBuildingInfo().getSnapshot().posToIndex(snapshotPos)]
        ) : null;
    }

    @Override
    protected boolean isAir(BlockPos blockPos) {
        // noinspection ConstantConditions
        return getSchematicBlock(blockPos) == null || getSchematicBlock(blockPos).isAir();
    }

    @Override
    protected Blueprint.BuildingInfo getBuildingInfo() {
        return tile.getBlueprintBuildingInfo();
    }

    @Override
    public void updateSnapshot() {
        super.updateSnapshot();
        // noinspection unchecked
        remainingDisplayRequiredBlocks = (List<ItemStack>[])
            new List<?>[getBuildingInfo().box.size().getX() * getBuildingInfo().box.size().getY() * getBuildingInfo().box.size().getZ()];
        Arrays.fill(remainingDisplayRequiredBlocks, Collections.emptyList());
    }

    @Override
    public void invResourcesChanged() {
        super.invResourcesChanged();
        extractRequiredCache.clear();
    }

    @Override
    public void cancel() {
        super.cancel();
        remainingDisplayRequiredBlocks = null;
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
        Supplier<Optional<List<ItemStack>>> function = () ->
            (
                StackUtil.mergeSameItems(requiredItems).stream()
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
                        )
            )
                ?
                Optional.of(
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
                )
                : Optional.empty();
        if (!simulate) {
            return function.get();
        }
        return extractRequiredCache.computeIfAbsent(
            Pair.of(requiredItems, requiredFluids),
            pair -> function.get()
        );
    }

    @Override
    protected boolean canPlace(BlockPos blockPos) {
        // noinspection ConstantConditions
        return !isAir(blockPos) && getSchematicBlock(blockPos).canBuild(tile.getWorldBC(), blockPos);
    }

    @Override
    protected boolean readyToPlace(BlockPos blockPos) {
        // noinspection ConstantConditions
        return getSchematicBlock(blockPos).getRequiredBlockOffsets().stream()
            .map(blockPos::add)
            .allMatch(pos ->
                getSchematicBlock(pos) != null
                    ? checkResults[posToIndex(pos)] == CHECK_RESULT_CORRECT
                    : !isAir(pos) || tile.getWorldBC().isAirBlock(pos)
            );
    }

    @Override
    protected boolean hasEnoughToPlaceItems(BlockPos blockPos) {
        return Optional.ofNullable(getBuildingInfo()).flatMap(buildingInfo ->
            tryExtractRequired(
                buildingInfo.toPlaceRequiredItems[posToIndex(blockPos)],
                buildingInfo.toPlaceRequiredFluids[posToIndex(blockPos)],
                true
            )
        ).isPresent();
    }

    @Override
    protected List<ItemStack> getToPlaceItems(BlockPos blockPos) {
        return Optional.ofNullable(getBuildingInfo()).flatMap(buildingInfo ->
            tryExtractRequired(
                buildingInfo.toPlaceRequiredItems[posToIndex(blockPos)],
                buildingInfo.toPlaceRequiredFluids[posToIndex(blockPos)],
                false
            )
        ).orElse(null);
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
        // noinspection ConstantConditions
        return getBuildingInfo() != null &&
            getSchematicBlock(blockPos) != null &&
            getSchematicBlock(blockPos).isBuilt(tile.getWorldBC(), blockPos);
    }

    @Override
    protected boolean doPlaceTask(PlaceTask placeTask) {
        // noinspection ConstantConditions
        return getBuildingInfo() != null &&
            getSchematicBlock(placeTask.pos) != null &&
            getSchematicBlock(placeTask.pos).build(tile.getWorldBC(), placeTask.pos);
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
                buildingInfo.box.getBoundingBox(),
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
                    remainingDisplayRequiredBlocksConcat.stream(),
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
    protected boolean check(BlockPos blockPos) {
        if (super.check(blockPos)) {
            remainingDisplayRequiredBlocks[posToIndex(blockPos)] =
                checkResults[posToIndex(blockPos)] != CHECK_RESULT_CORRECT
                    ?
                    getDisplayRequired(
                        getBuildingInfo().toPlaceRequiredItems[posToIndex(blockPos)],
                        getBuildingInfo().toPlaceRequiredFluids[posToIndex(blockPos)]
                    ).collect(Collectors.toList())
                    : Collections.emptyList();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void afterChecks() {
        remainingDisplayRequiredBlocksConcat = StackUtil.mergeSameItems(
            Arrays.stream(remainingDisplayRequiredBlocks)
                .flatMap(Collection::stream)
                .collect(Collectors.toList())
        );
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
