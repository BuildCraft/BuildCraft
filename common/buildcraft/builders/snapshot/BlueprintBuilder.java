package buildcraft.builders.snapshot;

import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.net.PacketBufferBC;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BlueprintBuilder extends SnapshotBuilder<ITileForBlueprintBuilder> {
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
                                        .filter(entry -> !isBlockCorrect(entry.getKey()) )
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
        List<ItemStack> requiredItems = Stream.concat(
                schematicBlock.requiredItems == null ? Stream.empty() : schematicBlock.requiredItems.stream(),
                schematicBlock.requiredFluids == null ? Stream.empty() : schematicBlock.requiredFluids.stream()
                        .map(BlockUtil::getBucketFromFluid)
        ).collect(Collectors.toList());
        if (requiredItems.stream()
                .noneMatch(stack ->
                        tile.getInvResources().extract(
                                extracted -> StackUtil.canMerge(stack, extracted),
                                stack.getCount(),
                                stack.getCount(),
                                true
                        ).isEmpty()
                )) {
            return requiredItems.stream()
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

    @Override
    protected void cancelPlaceTask(PlaceTask placeTask) {
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
    public boolean tick() {
        boolean result = super.tick();
        if (tile.getWorldBC().isRemote) {
            return result;
        }

        neededStacks.clear();
        getToPlace().stream()
                .filter(blockPos -> !isBlockCorrect(blockPos))
                .map(blockPos -> tile.getBlueprintBuildingInfo().toPlace.get(blockPos))
                .flatMap(schematicBlock ->
                        Stream.concat(
                                schematicBlock.requiredItems == null ? Stream.empty() : schematicBlock.requiredItems.stream(),
                                schematicBlock.requiredFluids == null ? Stream.empty() : schematicBlock.requiredFluids.stream()
                                        .map(BlockUtil::getBucketFromFluid)
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

        return result;
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

    @Override
    protected boolean isDone() {
        return getBuiltLevel() == getMaxLevel();
    }
}
