package buildcraft.builders.snapshot;

import buildcraft.api.inventory.IItemTransactor;
import buildcraft.lib.fluids.Tank;
import buildcraft.lib.fluids.TankManager;

public interface ITileForBlueprintBuilder extends ITileForSnapshotBuilder {
    Blueprint.BuildingInfo getBlueprintBuildingInfo();

    IItemTransactor getInvResources();

    TankManager<Tank> getTankManager();
}
