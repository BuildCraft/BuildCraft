package buildcraft.builders.snapshot;

import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.net.PacketBufferBC;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fluids.Fluid;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BlueprintBuilder extends SnapshotBuilder<ITileForBlueprintBuilder> {
    private static final double MAX_ENTITY_DISTANCE = 0.1D;
    public List<ItemStack> neededStacks = new ArrayList<>();

    public BlueprintBuilder(ITileForBlueprintBuilder tile) {
        super(tile);
    }

    private Blueprint.BuildingInfo getBuildingInfo() {
        return tile.getBlueprintBuildingInfo();
    }

    private int getBuiltLevel() {
        return Optional.ofNullable(getBuildingInfo())
                .map(buildingInfo ->
                        Stream.concat(
                                buildingInfo.toBreak.stream()
                                        .filter(pos -> !tile.getWorldBC().isAirBlock(pos))
                                        .map(i -> 0),
                                buildingInfo.toPlace.entrySet().stream()
                                        .filter(entry -> !isBlockCorrect(entry.getKey()))
                                        .map(entry -> entry.getValue().level)
                        )
                                .min(Integer::compare)
                                .map(i -> i - 1)
                                .orElse(getBuildingInfo().maxLevel)
                )
                .orElse(-1);
    }

    private int getMaxLevel() {
        return Optional.ofNullable(getBuildingInfo())
                .map(buildingInfo -> buildingInfo.maxLevel)
                .orElse(0);
    }

    public Stream<ItemStack> getRequired(List<ItemStack> requiredItems, List<Fluid> requiredFluids) {
        return Stream.concat(
                requiredItems == null ? Stream.empty() : requiredItems.stream(),
                requiredFluids == null ? Stream.empty() : requiredFluids.stream()
                        .map(BlockUtil::getBucketFromFluid)
        );
    }

    private List<ItemStack> tryExtractRequired(List<ItemStack> requiredItems, List<Fluid> requiredFluids) {
        List<ItemStack> required = getRequired(requiredItems, requiredFluids).collect(Collectors.toList());
        if (required.stream()
                .noneMatch(stack ->
                        tile.getInvResources().extract(
                                extracted -> StackUtil.canMerge(stack, extracted),
                                stack.getCount(),
                                stack.getCount(),
                                true
                        ).isEmpty()
                )) {
            return required.stream()
                    .map(stack ->
                            tile.getInvResources().extract(
                                    extracted -> StackUtil.canMerge(stack, extracted),
                                    stack.getCount(),
                                    stack.getCount(),
                                    false
                            )
                    )
                    .collect(Collectors.toList());
        } else {
            return Collections.singletonList(ItemStack.EMPTY);
        }
    }

    protected boolean customPre() {
        Optional.ofNullable(getBuildingInfo()).ifPresent(buildingInfo ->
                tile.getWorldBC().getEntitiesWithinAABB(
                        Entity.class,
                        buildingInfo.box.getBoundingBox(),
                        entity ->
                                entity != null &&
                                        buildingInfo.entities.stream()
                                                .map(schematicEntity -> schematicEntity.pos)
                                                .map(new Vec3d(buildingInfo.basePos)::add)
                                                .map(entity.getPositionVector()::distanceTo)
                                                .noneMatch(distance -> distance < MAX_ENTITY_DISTANCE) &&
                                        SchematicEntityFactory.getSchematicEntity(
                                                tile.getWorldBC(),
                                                BlockPos.ORIGIN,
                                                entity
                                        ) != null
                ).forEach(Entity::setDead)
        );
        return true;
    }

    @Override
    protected List<BlockPos> getToBreak() {
        return Optional.ofNullable(getBuildingInfo())
                .map(buildingInfo -> buildingInfo.toBreak)
                .orElse(Collections.emptyList());
    }

    @Override
    protected List<BlockPos> getToPlace() {
        return Optional.ofNullable(getBuildingInfo())
                .map(buildingInfo -> getBuildingInfo().toPlace)
                .map(Map::keySet)
                .<List<BlockPos>>map(ArrayList::new)
                .orElse(Collections.emptyList());
    }

    @Override
    protected boolean canPlace(BlockPos blockPos) {
        return getBuildingInfo().toPlace.get(blockPos).requiredBlockOffsets.stream()
                .map(blockPos::add)
                .allMatch(pos ->
                        getBuildingInfo().toPlace.containsKey(pos) ? isBlockCorrect(pos) : !tile.getWorldBC().isAirBlock(pos)
                ) &&
                getBuildingInfo().toPlace.get(blockPos).level == getBuiltLevel() + 1 &&
                getBuildingInfo().toPlace.get(blockPos).placeBlock != Blocks.AIR &&
                (
                        tile.getWorldBC().isAirBlock(blockPos) ||
                                (
                                        BlockUtil.getFluidWithFlowing(
                                                getBuildingInfo().toPlace.get(blockPos).blockState.getBlock()
                                        ) != null &&
                                                BlockUtil.getFluidWithFlowing(tile.getWorldBC(), blockPos) != null
                                )
                );
    }

    @Override
    protected List<ItemStack> getToPlaceItems(BlockPos blockPos) {
        SchematicBlock schematicBlock = getBuildingInfo().toPlace.get(blockPos);
        return tryExtractRequired(schematicBlock.requiredItems, schematicBlock.requiredFluids);
    }

    @Override
    protected void cancelPlaceTask(PlaceTask placeTask) {
        placeTask.items.forEach(item -> tile.getInvResources().insert(item, false, false));
    }

    @Override
    protected boolean isBlockCorrect(BlockPos blockPos) {
        return getBuildingInfo() != null &&
                getBuildingInfo().toPlace.containsKey(blockPos) &&
                getBuildingInfo().toPlace.get(blockPos).blockState != null &&
                getBuildingInfo().toPlace.get(blockPos).canBeReplacedWithBlocks.contains(
                        tile.getWorldBC().getBlockState(blockPos).getBlock()
                ) &&
                getBuildingInfo().toPlace.get(blockPos).blockState.getPropertyKeys().stream()
                        .filter(tile.getWorldBC().getBlockState(blockPos).getPropertyKeys()::contains)
                        .filter(property -> !getBuildingInfo().toPlace.get(blockPos).ignoredProperties.contains(property))
                        .allMatch(property ->
                                Objects.equals(
                                        getBuildingInfo().toPlace.get(blockPos).blockState.getValue(property),
                                        tile.getWorldBC().getBlockState(blockPos).getValue(property)
                                )
                        );
    }

    @Override
    protected boolean doPlaceTask(PlaceTask placeTask) {
        return !(getBuildingInfo() == null || getBuildingInfo().toPlace.get(placeTask.pos) == null) &&
                getBuildingInfo().toPlace.get(placeTask.pos).build(tile.getWorldBC(), placeTask.pos);
    }

    @Override
    public Box getBox() {
        return Optional.ofNullable(getBuildingInfo())
                .map(Blueprint.BuildingInfo::getBox)
                .orElse(null);
    }

    @Override
    protected boolean isDone() {
        return getBuiltLevel() == getMaxLevel();
    }

    protected boolean customPost() {
        Optional.ofNullable(getBuildingInfo()).ifPresent(buildingInfo -> {
            List<Entity> entitiesWithinBox = tile.getWorldBC().getEntitiesWithinAABB(
                    Entity.class,
                    buildingInfo.box.getBoundingBox(),
                    Objects::nonNull
            );
            buildingInfo.entities.stream()
                    .filter(schematicEntity ->
                            entitiesWithinBox.stream()
                                    .map(Entity::getPositionVector)
                                    .map(schematicEntity.pos.add(new Vec3d(buildingInfo.basePos))::distanceTo)
                                    .noneMatch(distance -> distance < MAX_ENTITY_DISTANCE)
                    )
                    .filter(schematicEntity ->
                            !tryExtractRequired(schematicEntity.requiredItems, schematicEntity.requiredFluids)
                                    .contains(ItemStack.EMPTY)
                    )
                    .forEach(schematicEntity -> schematicEntity.build(tile.getWorldBC(), buildingInfo.basePos));
        });
        return true;
    }

    @Override
    public boolean tick() {
        if (tile.getWorldBC().isRemote) {
            return super.tick();
        }
        return Optional.ofNullable(getBuildingInfo()).map(buildingInfo -> {
            // Compute needed stacks
            neededStacks.clear();
            List<Entity> entitiesWithinBox = tile.getWorldBC().getEntitiesWithinAABB(
                    Entity.class,
                    buildingInfo.box.getBoundingBox(),
                    Objects::nonNull
            );
            Stream.concat(
                    getToPlace().stream()
                            .filter(blockPos -> !isBlockCorrect(blockPos))
                            .map(blockPos -> tile.getBlueprintBuildingInfo().toPlace.get(blockPos))
                            .flatMap(schematicBlock ->
                                    getRequired(schematicBlock.requiredItems, schematicBlock.requiredFluids)
                            ),
                    buildingInfo.entities.stream()
                            .filter(schematicEntity ->
                                    entitiesWithinBox.stream()
                                            .map(Entity::getPositionVector)
                                            .map(schematicEntity.pos.add(new Vec3d(buildingInfo.basePos))::distanceTo)
                                            .noneMatch(distance -> distance < MAX_ENTITY_DISTANCE)
                            )
                            .flatMap(schematicEntity ->
                                    getRequired(schematicEntity.requiredItems, schematicEntity.requiredFluids)
                            )
            )
                    .forEach(toAdd -> {
                        boolean found = false;
                        for (ItemStack stack : neededStacks) {
                            if (StackUtil.canMerge(stack, toAdd)) {
                                stack.setCount(stack.getCount() + toAdd.getCount());
                                found = true;
                            }
                        }
                        if (!found) {
                            neededStacks.add(toAdd.copy());
                        }
                    });
            // Kill not needed entities
            List<Entity> toKill = tile.getWorldBC().getEntitiesWithinAABB(
                    Entity.class,
                    buildingInfo.box.getBoundingBox(),
                    entity ->
                            entity != null &&
                                    buildingInfo.entities.stream()
                                            .map(schematicEntity -> schematicEntity.pos)
                                            .map(new Vec3d(buildingInfo.basePos)::add)
                                            .map(entity.getPositionVector()::distanceTo)
                                            .noneMatch(distance -> distance < MAX_ENTITY_DISTANCE) &&
                                    SchematicEntityFactory.getSchematicEntity(
                                            tile.getWorldBC(),
                                            BlockPos.ORIGIN,
                                            entity
                                    ) != null
            );
            if (!toKill.isEmpty()) {
                if (!tile.getBattery().isFull()) {
                    return false;
                } else {
                    toKill.forEach(Entity::setDead);
                }
            }
            // Call superclass method
            if (super.tick()) {
                // Spawn needed entities
                List<SchematicEntity> toSpawn = buildingInfo.entities.stream()
                        .filter(schematicEntity ->
                                entitiesWithinBox.stream()
                                        .map(Entity::getPositionVector)
                                        .map(schematicEntity.pos.add(new Vec3d(buildingInfo.basePos))::distanceTo)
                                        .noneMatch(distance -> distance < MAX_ENTITY_DISTANCE)
                        )
                        .collect(Collectors.toList());
                if (!toSpawn.isEmpty()) {
                    if (!tile.getBattery().isFull()) {
                        return false;
                    } else {
                        toSpawn.stream()
                                .filter(schematicEntity ->
                                        !tryExtractRequired(schematicEntity.requiredItems, schematicEntity.requiredFluids)
                                                .contains(ItemStack.EMPTY)
                                )
                                .forEach(schematicEntity -> schematicEntity.build(tile.getWorldBC(), buildingInfo.basePos));
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
        buffer.writeInt(neededStacks.size());
        neededStacks.forEach(stack -> {
            buffer.writeItemStack(stack);
            buffer.writeInt(stack.getCount());
        });
    }

    @Override
    public void readFromByteBuf(PacketBufferBC buffer) {
        super.readFromByteBuf(buffer);
        neededStacks.clear();
        IntStream.range(0, buffer.readInt()).mapToObj(i -> {
            ItemStack stack;
            try {
                stack = buffer.readItemStack();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            stack.setCount(buffer.readInt());
            return stack;
        }).forEach(neededStacks::add);
    }
}
