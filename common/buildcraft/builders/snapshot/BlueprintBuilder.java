package buildcraft.builders.snapshot;

import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.net.PacketBufferBC;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BlueprintBuilder extends SnapshotBuilder<ITileForBlueprintBuilder> {
    public List<ItemStack> neededStacks = new ArrayList<>();

    public BlueprintBuilder(ITileForBlueprintBuilder tile) {
        super(tile);
    }

    private Blueprint.BuildingInfo getBuildingInfo() {
        return tile.getBlueprintBuildingInfo();
    }

    @Override
    protected List<BlockPos> getToBreak() {
        return getBuildingInfo() == null ? Collections.emptyList() : getBuildingInfo().toBreak;
    }

    @Override
    protected List<BlockPos> getToPlace() {
        return getBuildingInfo() == null ? Collections.emptyList() : new ArrayList<>(getBuildingInfo().toPlace.keySet());
    }

    @Override
    protected boolean canPlace(BlockPos blockPos) {
        return getBuildingInfo().toPlace.get(blockPos).requiredBlockOffsets.stream()
                .map(blockPos::add)
                .allMatch(pos -> getBuildingInfo().toPlace.containsKey(pos) ? isBlockCorrect(pos) : !tile.getWorld().isAirBlock(pos));
    }

    @Override
    protected List<ItemStack> getToPlaceItems(BlockPos blockPos) {
        List<ItemStack> requiredItems = getBuildingInfo().toPlace.get(blockPos).requiredItems;
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
            return Collections.emptyList();
        }
    }

    @Override
    protected void cancelPlaceTask(PlaceTask placeTask) {
    }

    @Override
    protected boolean isBlockCorrect(BlockPos blockPos) {
        return !tile.getWorld().isAirBlock(blockPos) &&
                getBuildingInfo() != null &&
                getBuildingInfo().toPlace.containsKey(blockPos) &&
                getBuildingInfo().toPlace.get(blockPos).blockState != null &&
                getBuildingInfo().toPlace.get(blockPos).blockState.equals(tile.getWorld().getBlockState(blockPos)); // FIXME: wrong! no equals method overrode!
    }

    @Override
    protected boolean doPlaceTask(PlaceTask placeTask) {
        return getBuildingInfo().toPlace.get(placeTask.pos).build(tile.getWorld(), placeTask.pos);
    }

    @Override
    protected Box getBox() {
        return tile.getBlueprintBuildingInfo().getBox();
    }

    @Override
    public void tick() {
        super.tick();
        if (tile.getWorld().isRemote) {
            return;
        }

        neededStacks.clear();
        getToPlace().stream()
                .filter(blockPos -> !isBlockCorrect(blockPos))
                .map(blockPos -> tile.getBlueprintBuildingInfo().toPlace.get(blockPos))
                .flatMap(schematicBlock -> schematicBlock.requiredItems.stream())
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
    }

    @Override
    public void writePayload(PacketBufferBC buffer) {
        super.writePayload(buffer);
        buffer.writeInt(neededStacks.size());
        neededStacks.forEach(stack -> {
            buffer.writeItemStack(stack);
            buffer.writeInt(stack.getCount());
        });
    }

    @Override
    public void readPayload(PacketBufferBC buffer) {
        super.readPayload(buffer);
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
