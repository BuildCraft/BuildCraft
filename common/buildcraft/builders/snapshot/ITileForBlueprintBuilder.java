package buildcraft.builders.snapshot;

import buildcraft.api.inventory.IItemTransactor;

public interface ITileForBlueprintBuilder extends ITileForSnapshotBuilder {
    Blueprint.BuildingInfo getBlueprintBuildingInfo();

    IItemTransactor getInvResources();
}
