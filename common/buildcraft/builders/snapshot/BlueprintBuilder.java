package buildcraft.builders.snapshot;

import buildcraft.lib.misc.StackUtil;
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
    protected boolean doPlaceTask(PlaceTask placeTask) {
        tile.getWorld().setBlockState(placeTask.getPos(), getBuildingInfo().toPlace.get(placeTask.getPos()).blockState);
        return true;
    }

    @Override
    protected Box getBox() {
        return tile.getBlueprintBuildingInfo().getBox();
    }
}
