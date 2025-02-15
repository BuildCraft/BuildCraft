/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.ISchematicEntity;
import buildcraft.api.schematics.SchematicEntityContext;
import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.net.PacketBufferBC;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BlueprintBuilder extends SnapshotBuilder<ITileForBlueprintBuilder> {
    private static final double MAX_ENTITY_DISTANCE = 0.1D;
    private static final String FLUID_STACK_KEY = "BuilderFluidStack";

    private List<ItemStack>[] remainingDisplayRequiredBlocks;
    private List<ItemStack> remainingDisplayRequiredBlocksConcat = Collections.emptyList();
    public List<ItemStack> remainingDisplayRequired = new ArrayList<>();
    private final Map<Pair<List<ItemStack>, List<FluidStack>>, Optional<List<ItemStack>>> extractRequiredCache =
            new HashMap<>();

    public BlueprintBuilder(ITileForBlueprintBuilder tile) {
        super(tile);
    }

    private ISchematicBlock getSchematicBlock(BlockPos blockPos) {
        return getBuildingInfo().box.contains(blockPos)
                ?
                getBuildingInfo().rotatedPalette.get(
                        getBuildingInfo().getSnapshot().data[getBuildingInfo().getSnapshot().posToIndex(
                                getBuildingInfo().fromWorld(blockPos)
                        )]
                )
                : null;
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
        remainingDisplayRequiredBlocks = (List<ItemStack>[]) new List<?>[getBuildingInfo().getSnapshot().getDataSize()];
        Arrays.fill(remainingDisplayRequiredBlocks, Collections.emptyList());
    }

    @Override
    public void resourcesChanged() {
        super.resourcesChanged();
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
                        .map(FluidUtil::getFilledBucket)
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
//                                                        FluidUtilBC.areFluidStackEqual(stack, tile.getTankManager().drain(stack, false))
                                                        FluidUtilBC.areFluidStackEqual(stack, tile.getTankManager().drain(stack, FluidAction.SIMULATE))
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
//                                                        .map(fluidStack -> tile.getTankManager().drain(fluidStack, !simulate))
                                                        .map(fluidStack -> tile.getTankManager().drain(fluidStack, simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE))
                                                        .map(fluidStack ->
                                                        {
                                                            ItemStack stack = FluidUtil.getFilledBucket(fluidStack);
                                                            if (!stack.hasTag()) {
                                                                stack.setTag(new CompoundTag());
                                                            }
                                                            // noinspection ConstantConditions
                                                            stack.getTag().put(
                                                                    FLUID_STACK_KEY,
                                                                    fluidStack.writeToNBT(new CompoundTag())
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
    protected boolean isReadyToPlace(BlockPos blockPos) {
        // noinspection ConstantConditions
        return getSchematicBlock(blockPos).getRequiredBlockOffsets().stream()
                .map(blockPos::offset)
                .allMatch(pos -> getSchematicBlock(pos) == null || checkResults[posToIndex(pos)] == CHECK_RESULT_CORRECT) &&
                getSchematicBlock(blockPos).isReadyToBuild(tile.getWorldBC(), blockPos);
    }

    @Override
    protected boolean hasEnoughToPlaceItems(BlockPos blockPos) {
        return tryExtractRequired(
                getBuildingInfo().toPlaceRequiredItems[posToIndex(blockPos)],
                getBuildingInfo().toPlaceRequiredFluids[posToIndex(blockPos)],
                true
        ).isPresent();
    }

    @Override
    protected List<ItemStack> getToPlaceItems(BlockPos blockPos) {
        return tryExtractRequired(
                getBuildingInfo().toPlaceRequiredItems[posToIndex(blockPos)],
                getBuildingInfo().toPlaceRequiredFluids[posToIndex(blockPos)],
                false
        ).orElse(null);
    }

    @Override
    protected void cancelPlaceTask(PlaceTask placeTask) {
        super.cancelPlaceTask(placeTask);
        // noinspection ConstantConditions
        placeTask.items.stream()
                .filter(stack -> !stack.hasTag() || !stack.getTag().contains(FLUID_STACK_KEY))
                .forEach(stack -> tile.getInvResources().insert(stack, false, false));
        // noinspection ConstantConditions
        placeTask.items.stream()
                .filter(stack -> stack.hasTag() && stack.getTag().contains(FLUID_STACK_KEY))
                .map(stack -> Pair.of(stack.getCount(), stack.getTag().getCompound(FLUID_STACK_KEY)))
                .map(countNbt ->
                {
                    FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(countNbt.getRight());
                    if (fluidStack != null) {
                        fluidStack.setAmount(fluidStack.getAmount() * countNbt.getLeft());
                    }
                    return fluidStack;
                })
                .forEach(fluidStack -> tile.getTankManager().fill(fluidStack, FluidAction.EXECUTE));
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
        if (tile.getWorldBC().isClientSide) {
            return super.tick();
        }
        tile.getWorldBC().getProfiler().push("entitiesWithinBox");
//        List<Entity> entitiesWithinBox = tile.getWorldBC().getEntitiesWithinAABB(
        List<Entity> entitiesWithinBox = tile.getWorldBC().getEntitiesOfClass(
                Entity.class,
                getBuildingInfo().box.getBoundingBox(),
                Objects::nonNull
        );
        tile.getWorldBC().getProfiler().pop();
        tile.getWorldBC().getProfiler().push("toSpawn");
        List<ISchematicEntity> toSpawn = getBuildingInfo().entities.stream()
                .filter(schematicEntity ->
                                entitiesWithinBox.stream()
//                                        .map(Entity::getPositionVector)
                                        .map(Entity::position)
//                                        .map(schematicEntity.getPos().add(new Vec3(getBuildingInfo().offsetPos))::distanceTo)
                                        .map(schematicEntity.getPos().add(Vec3.atLowerCornerOf(getBuildingInfo().offsetPos))::distanceTo)
                                        .noneMatch(distance -> distance < MAX_ENTITY_DISTANCE)
                )
                .collect(Collectors.toList());
        tile.getWorldBC().getProfiler().pop();
        // Compute needed stacks
        tile.getWorldBC().getProfiler().push("remainingDisplayRequired");
        remainingDisplayRequired.clear();
        remainingDisplayRequired.addAll(StackUtil.mergeSameItems(
                Stream.concat(
                        remainingDisplayRequiredBlocksConcat.stream(),
                        toSpawn.stream()
                                .flatMap(schematicEntity ->
                                        getDisplayRequired(
                                                getBuildingInfo().entitiesRequiredItems.get(schematicEntity),
                                                getBuildingInfo().entitiesRequiredFluids.get(schematicEntity)
                                        )
                                )
                ).collect(Collectors.toList())
        ));
        tile.getWorldBC().getProfiler().pop();
        // Kill not needed entities
        tile.getWorldBC().getProfiler().push("toKill");
        List<Entity> toKill = entitiesWithinBox.stream()
                .filter(entity ->
                                entity != null &&
                                        getBuildingInfo().entities.stream()
                                                .map(ISchematicEntity::getPos)
//                                                .map(new Vec3(getBuildingInfo().offsetPos)::add)
                                                .map(Vec3.atLowerCornerOf(getBuildingInfo().offsetPos)::add)
//                                                .map(entity.getPositionVector()::distanceTo)
                                                .map(entity.position()::distanceTo)
                                                .noneMatch(distance -> distance < MAX_ENTITY_DISTANCE) &&
                                        SchematicEntityManager.getSchematicEntity(new SchematicEntityContext(
                                                tile.getWorldBC(),
//                                                BlockPos.ORIGIN,
                                                BlockPos.ZERO,
                                                entity
                                        )) != null
                )
                .collect(Collectors.toList());
        if (!toKill.isEmpty()) {
            if (!tile.getBattery().isFull()) {
                return false;
            } else {
                tile.getWorldBC().getProfiler().push("kill");
//                toKill.forEach(Entity::setDead);
                toKill.forEach(Entity::kill);
                tile.getWorldBC().getProfiler().pop();
            }
        }
        tile.getWorldBC().getProfiler().pop();
        // Call superclass method
        if (super.tick()) {
            // Spawn needed entities
            if (!toSpawn.isEmpty()) {
                if (!tile.getBattery().isFull()) {
                    return false;
                } else {
                    tile.getWorldBC().getProfiler().push("spawn");
                    toSpawn.stream()
                            .filter(schematicEntity ->
                                    tryExtractRequired(
                                            getBuildingInfo().entitiesRequiredItems.get(schematicEntity),
                                            getBuildingInfo().entitiesRequiredFluids.get(schematicEntity),
                                            true
                                    ).isPresent()
                            )
                            .filter(schematicEntity ->
                                    schematicEntity.build(tile.getWorldBC(), getBuildingInfo().offsetPos) != null
                            )
                            .forEach(schematicEntity ->
                                    tryExtractRequired(
                                            getBuildingInfo().entitiesRequiredItems.get(schematicEntity),
                                            getBuildingInfo().entitiesRequiredFluids.get(schematicEntity),
                                            false
                                    )
                            );
                    tile.getWorldBC().getProfiler().pop();
                }
            }
            return true;
        } else {
            return false;
        }
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
        remainingDisplayRequired.forEach(stack ->
        {
//            buffer.writeItemStack(stack);
            buffer.writeItemStack(stack, false);
            buffer.writeInt(stack.getCount());
        });
    }

    @Override
    public void readFromByteBuf(PacketBufferBC buffer) {
        super.readFromByteBuf(buffer);
        remainingDisplayRequired.clear();
        IntStream.range(0, buffer.readInt()).mapToObj(i ->
        {
            ItemStack stack;
            // Calen: no Exception
//            try {
//                stack = buffer.readItemStack();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
            stack = buffer.readItem();
            stack.setCount(buffer.readInt());
            return stack;
        }).forEach(remainingDisplayRequired::add);
    }
}
