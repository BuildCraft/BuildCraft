/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.snapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import ct.buildcraft.lib.misc.*;
import org.apache.commons.lang3.tuple.Pair;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.api.schematics.ISchematicBlock;
import ct.buildcraft.api.schematics.ISchematicEntity;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.api.robots.ResourceIdBlock;
import ct.buildcraft.api.schematics.SchematicEntityContext;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class BlueprintBuilder extends SnapshotBuilder<ITileForBlueprintBuilder> {
    private static final double MAX_ENTITY_DISTANCE = 0.1D;
    private static final String FLUID_STACK_KEY = "BuilderFluidStack";

    private List<ItemStack>[] remainingDisplayRequiredBlocks;
    private List<ItemStack> remainingDisplayRequiredBlocksConcat = Collections.emptyList();
    public List<ItemStack> remainingDisplayRequired = new ArrayList<>();
    private final Map<Pair<List<ItemStack>, List<FluidStack>>, Optional<List<ItemStack>>> extractRequiredCache =
        new HashMap<>();
    private final Set<BlockPos> robotReservedBlocks = new HashSet<>();

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
        robotReservedBlocks.clear();
    }

    @Override
    public void cancel() {
        super.cancel();
        remainingDisplayRequiredBlocks = null;
        robotReservedBlocks.clear();
    }

    private Stream<ItemStack> getDisplayRequired(List<ItemStack> requiredItems, List<FluidStack> requiredFluids) {
        return Stream.concat(
            requiredItems == null ? Stream.empty() : requiredItems.stream(),
            requiredFluids == null ? Stream.empty() : requiredFluids.stream()
                .map(FluidUtilBC::getFragileFluid)
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
                                .map(fluidStack -> tile.getTankManager().drain(fluidStack, !simulate ? FluidAction.EXECUTE : FluidAction.SIMULATE))
                                .map(fluidStack -> {
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


    /**
     * Reserves the next blueprint block that a Builder Robot can construct. This mirrors the classic BC7 builder
     * robot flow: reserve a slot, fetch at most one stack per robot inventory slot, fly to the destination, then build.
     */
    public RobotBuildTask reserveNextRobotTask(EntityRobotBase robot, boolean needMaterial) {
        if (robot == null || getBuildingInfo() == null || checkResults == null) {
            return null;
        }

        return getBuildingInfo().box.getBlocksInArea().stream()
            .sorted(BlockUtil.uniqueBlockPosComparator(java.util.Comparator.comparingDouble(blockPos ->
                100_000 - (Math.pow(blockPos.getX() - tile.getBuilderPos().getX(), 2) +
                    Math.pow(blockPos.getZ() - tile.getBuilderPos().getZ(), 2)) +
                    Math.abs(blockPos.getY() - tile.getBuilderPos().getY()) * 100_000
            )))
            .filter(blockPos -> robot.getZoneToWork() == null || robot.getZoneToWork().contains(Vec3.atCenterOf(blockPos)))
            .filter(blockPos -> !robotReservedBlocks.contains(blockPos))
            .filter(blockPos -> robot.getRegistry() == null || !robot.getRegistry().isTaken(new ResourceIdBlock(blockPos)))
            .filter(blockPos -> {
                check(blockPos);
                int index = posToIndex(blockPos);
                return checkResults[index] == CHECK_RESULT_TO_PLACE && canPlace(blockPos) && isReadyToPlace(blockPos);
            })
            .map(blockPos -> makeRobotTask(robot, blockPos, needMaterial))
            .filter(java.util.Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    private RobotBuildTask makeRobotTask(EntityRobotBase robot, BlockPos blockPos, boolean needMaterial) {
        int index = posToIndex(blockPos);
        List<FluidStack> requiredFluids = getBuildingInfo().toPlaceRequiredFluids[index];
        if (requiredFluids != null && requiredFluids.stream().anyMatch(stack -> stack != null && !stack.isEmpty())) {
            return null;
        }

        List<ItemStack> requirements = needMaterial
            ? StackUtil.mergeSameItems(Optional.ofNullable(getBuildingInfo().toPlaceRequiredItems[index])
                .orElseGet(java.util.Collections::emptyList)
                .stream()
                .filter(stack -> stack != null && !stack.isEmpty())
                .map(ItemStack::copy)
                .collect(Collectors.toList()))
            : java.util.Collections.emptyList();
        if (requirements.size() > robot.getContainerSize() || requirements.stream().anyMatch(stack -> stack.getCount() > stack.getMaxStackSize())) {
            return null;
        }

        ResourceIdBlock resource = new ResourceIdBlock(blockPos);
        if (robot.getRegistry() != null && !robot.getRegistry().take(resource, robot)) {
            return null;
        }
        robotReservedBlocks.add(blockPos);
        return new RobotBuildTask(blockPos, requirements, computeRobotEnergyCost(blockPos));
    }

    public void releaseRobotTask(EntityRobotBase robot, RobotBuildTask task) {
        if (task == null) {
            return;
        }
        robotReservedBlocks.remove(task.pos());
        if (robot != null && robot.getRegistry() != null) {
            robot.getRegistry().release(new ResourceIdBlock(task.pos()));
        }
    }

    public boolean buildRobotTask(EntityRobotBase robot, RobotBuildTask task) {
        if (robot == null || task == null || getBuildingInfo() == null) {
            releaseRobotTask(robot, task);
            return false;
        }

        BlockPos blockPos = task.pos();
        try {
            if (isBlockCorrect(blockPos)) {
                check(blockPos);
                return true;
            }
            check(blockPos);
            int index = posToIndex(blockPos);
            if (checkResults[index] != CHECK_RESULT_TO_PLACE || !canPlace(blockPos) || !isReadyToPlace(blockPos)) {
                return false;
            }
            if (!hasRobotRequirements(robot, task.requirements())) {
                return false;
            }
            ISchematicBlock schematicBlock = getSchematicBlock(blockPos);
            if (schematicBlock == null || schematicBlock.isAir()) {
                return false;
            }

            boolean built = schematicBlock.build(tile.getWorldBC(), blockPos);
            if (built) {
                consumeRobotRequirements(robot, task.requirements());
                if (check(blockPos)) {
                    afterChecks();
                }
            }
            return built;
        } finally {
            releaseRobotTask(robot, task);
        }
    }

    private boolean hasRobotRequirements(EntityRobotBase robot, List<ItemStack> requirements) {
        for (ItemStack requirement : requirements) {
            int found = 0;
            for (int slot = 0; slot < robot.getContainerSize(); slot++) {
                ItemStack stack = robot.getItem(slot);
                if (!stack.isEmpty() && StackUtil.canMerge(requirement, stack)) {
                    found += stack.getCount();
                    if (found >= requirement.getCount()) {
                        break;
                    }
                }
            }
            if (found < requirement.getCount()) {
                return false;
            }
        }
        return true;
    }

    private void consumeRobotRequirements(EntityRobotBase robot, List<ItemStack> requirements) {
        for (ItemStack requirement : requirements) {
            int left = requirement.getCount();
            for (int slot = 0; slot < robot.getContainerSize() && left > 0; slot++) {
                ItemStack stack = robot.getItem(slot);
                if (!stack.isEmpty() && StackUtil.canMerge(requirement, stack)) {
                    int used = Math.min(left, stack.getCount());
                    robot.removeItem(slot, used);
                    left -= used;
                }
            }
        }
    }

    private int computeRobotEnergyCost(BlockPos blockPos) {
        return Math.max(8, (int) Math.ceil(Math.sqrt(blockPos.distSqr(tile.getBuilderPos())) * 10.0D));
    }

    public static class RobotBuildTask {
        private final BlockPos pos;
        private final List<ItemStack> requirements;
        private final int energyCost;

        public RobotBuildTask(BlockPos pos, List<ItemStack> requirements, int energyCost) {
            this.pos = pos;
            this.requirements = ImmutableList.copyOf(requirements == null ? java.util.Collections.emptyList() : requirements);
            this.energyCost = energyCost;
        }

        public RobotBuildTask(CompoundTag nbt) {
            this.pos = NbtUtils.readBlockPos(nbt.getCompound("pos"));
            this.requirements = ImmutableList.copyOf(
                NBTUtilBC.readCompoundList(nbt.get("requirements"))
                    .map(ItemStack::of)
                    .collect(Collectors.toList())
            );
            this.energyCost = nbt.getInt("energyCost");
        }

        public BlockPos pos() {
            return pos;
        }

        public List<ItemStack> requirements() {
            return requirements;
        }

        public int energyCost() {
            return energyCost;
        }

        public CompoundTag writeToNBT() {
            CompoundTag nbt = new CompoundTag();
            nbt.put("pos", NbtUtils.writeBlockPos(pos));
            nbt.put("requirements", NBTUtilBC.writeObjectList(requirements.stream().map(ItemStack::serializeNBT)));
            nbt.putInt("energyCost", energyCost);
            return nbt;
        }
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
        return !tile.needMeterial() || tryExtractRequired(
            getBuildingInfo().toPlaceRequiredItems[posToIndex(blockPos)],
            getBuildingInfo().toPlaceRequiredFluids[posToIndex(blockPos)],
            true
        ).isPresent();
    }

    @Override
    protected List<ItemStack> getToPlaceItems(BlockPos blockPos) {
        return tile.needMeterial() ? tryExtractRequired(
            getBuildingInfo().toPlaceRequiredItems[posToIndex(blockPos)],
            getBuildingInfo().toPlaceRequiredFluids[posToIndex(blockPos)],
            false
        ).orElse(null) : Stream.concat(getBuildingInfo().toPlaceRequiredItems[posToIndex(blockPos)].stream(), 
        		getBuildingInfo().toPlaceRequiredFluids[posToIndex(blockPos)].stream().map(t -> new ItemStack(t.getFluid().getBucket()))).collect(Collectors.toList());
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
            .map(countNbt -> {
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
    	Level level = tile.getWorldBC();
    	
        if (level.isClientSide) {
            return super.tick();
        }
        level.getProfiler().push("entitiesWithinBox");
        List<Entity> entitiesWithinBox = level.getEntitiesOfClass(
            Entity.class,
            getBuildingInfo().box.getBoundingBox(),
            Objects::nonNull
        );
        level.getProfiler().pop();
        level.getProfiler().push("toSpawn");
        List<ISchematicEntity> toSpawn = getBuildingInfo().entities.stream()
            .filter(schematicEntity ->
                entitiesWithinBox.stream()
                .noneMatch($ -> 
                	$.distanceToSqr(schematicEntity.getPos().add(Vec3.atLowerCornerOf(getBuildingInfo().offsetPos)))< MAX_ENTITY_DISTANCE
                )
/*                    .map(Entity::)
                    .map(schematicEntity.getPos().add(new Vec3(getBuildingInfo().offsetPos))::distanceTo)
                    .noneMatch(distance -> distance < MAX_ENTITY_DISTANCE)*/
            )
            .collect(Collectors.toList());
        level.getProfiler().pop();
        // Compute needed stacks
        level.getProfiler().push("remainingDisplayRequired");
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
        level.getProfiler().pop();
        // Kill not needed entities
        level.getProfiler().push("toKill");
        List<Entity> toKill = entitiesWithinBox.stream()
            .filter(entity ->
                entity != null &&
                    getBuildingInfo().entities.stream()
                    	.noneMatch($ -> 
                    	entity.distanceToSqr(Vec3.atLowerCornerOf(
                    			getBuildingInfo().offsetPos)
                    			.add($.getPos()))
                    			< MAX_ENTITY_DISTANCE)&&
/*                        .map(ISchematicEntity::getPos)
                        .map(Vec3.atLowerCornerOf(getBuildingInfo().offsetPos)::add)
                        .map(entity::distanceToSqr)
/                       .noneMatch(distance -> distance < MAX_ENTITY_DISTANCE)&&*/
                    SchematicEntityManager.getSchematicEntity(new SchematicEntityContext(
                        level,
                        BlockPos.ZERO,
                        entity
                    )) != null
            )
            .collect(Collectors.toList());
        if (!toKill.isEmpty()) {
            if (!tile.getBattery().isFull()) {
                return false;
            } else {
                level.getProfiler().push("kill");
                toKill.forEach(Entity::kill);
                level.getProfiler().pop();
            }
        }
        level.getProfiler().pop();
        // Call superclass method
        if (super.tick()) {
            // Spawn needed entities
            if (!toSpawn.isEmpty()) {
                if (!tile.getBattery().isFull()) {
                    return false;
                } else {
                    level.getProfiler().push("spawn");
                    toSpawn.stream()
                        .filter(schematicEntity ->
                            tryExtractRequired(
                                getBuildingInfo().entitiesRequiredItems.get(schematicEntity),
                                getBuildingInfo().entitiesRequiredFluids.get(schematicEntity),
                                true
                            ).isPresent()
                        )
                        .filter(schematicEntity ->
                            schematicEntity.build(level, getBuildingInfo().offsetPos) != null
                        )
                        .forEach(schematicEntity ->
                            tryExtractRequired(
                                getBuildingInfo().entitiesRequiredItems.get(schematicEntity),
                                getBuildingInfo().entitiesRequiredFluids.get(schematicEntity),
                                false
                            )
                        );
                    level.getProfiler().pop();
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
    public void writeToByteBuf(FriendlyByteBuf buffer) {
        super.writeToByteBuf(buffer);
        buffer.writeInt(remainingDisplayRequired.size());
        remainingDisplayRequired.forEach(stack -> {
            buffer.writeItem(stack);
            buffer.writeInt(stack.getCount());
        });
    }

    @Override
    public void readFromByteBuf(FriendlyByteBuf buffer) {
        super.readFromByteBuf(buffer);
        remainingDisplayRequired.clear();
        IntStream.range(0, buffer.readInt()).mapToObj(i -> {
            ItemStack stack;
            stack = buffer.readItem();
            stack.setCount(buffer.readInt());
            return stack;
        }).forEach(remainingDisplayRequired::add);
    }
}
