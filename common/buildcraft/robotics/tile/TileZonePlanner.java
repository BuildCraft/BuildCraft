package buildcraft.robotics.tile;

import buildcraft.lib.tile.TileBCInventory_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerSimple;

public class TileZonePlanner extends TileBCInventory_Neptune {
    public final ItemHandlerSimple invPaintbrushes;

    public TileZonePlanner() {
        invPaintbrushes = addInventory("paintbrushes", 16, ItemHandlerManager.EnumAccess.NONE);
    }
}
