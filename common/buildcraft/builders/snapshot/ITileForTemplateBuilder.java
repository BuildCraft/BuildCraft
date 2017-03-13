package buildcraft.builders.snapshot;

import buildcraft.api.inventory.IItemTransactor;

public interface ITileForTemplateBuilder extends ITileForSnapshotBuilder {
    Template.BuildingInfo getTemplateBuildingInfo();

    IItemTransactor getInvResources();
}
