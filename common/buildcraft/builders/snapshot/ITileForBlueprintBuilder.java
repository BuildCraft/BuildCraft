package buildcraft.builders.snapshot;

import buildcraft.api.inventory.IItemTransactor;

import buildcraft.lib.fluid.Tank;
import buildcraft.lib.fluid.TankManager;

public interface ITileForBlueprintBuilder extends ITileForSnapshotBuilder {
    Blueprint.BuildingInfo getBlueprintBuildingInfo();

    IItemTransactor getInvResources();

    TankManager<Tank> getTankManager();
}
