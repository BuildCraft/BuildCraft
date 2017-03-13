package buildcraft.builders.snapshot;

import buildcraft.lib.misc.data.Box;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlueprintBuilder extends SnapshotBuilder<ITileForBlueprintBuilder> {
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
    protected List<ItemStack> getToPlaceItems(BlockPos blockPos) {
        List<ItemStack> requiredItems = getBuildingInfo().toPlace.get(blockPos).requiredItems;
        return Collections.singletonList(tile.getInvResources().extract(null, 1, 1, false));
    }

    @Override
    protected void cancelPlaceTask(PlaceTask placeTask) {
    }

    @Override
    protected boolean isBlockCorrect(BlockPos blockPos) {
        return getBuildingInfo() != null &&
                getBuildingInfo().toPlace.containsKey(blockPos) &&
                getBuildingInfo().toPlace.get(blockPos).blockState != null &&
                getBuildingInfo().toPlace.get(blockPos).blockState.equals(tile.getWorld().getBlockState(blockPos)); // FIXME: wrong! no equals method overrode!
    }

    @Override
    protected boolean doPlaceTask(PlaceTask placeTask) {
        return tile.getWorld().setBlockState(placeTask.getPos(), getBuildingInfo().toPlace.get(placeTask.getPos()).blockState);
    }

    @Override
    protected Box getBox() {
        return tile.getBlueprintBuildingInfo().getBox();
    }
}
