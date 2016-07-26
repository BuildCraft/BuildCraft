package buildcraft.robotics.tile;

import buildcraft.lib.tile.TileBCInventory_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.robotics.ZonePlan;

public class TileZonePlanner extends TileBCInventory_Neptune {
    public final ItemHandlerSimple invPaintbrushes;
    public ZonePlan[] layers = new ZonePlan[16];

    public TileZonePlanner() {
        invPaintbrushes = addInventory("paintbrushes", 16, ItemHandlerManager.EnumAccess.NONE);
        for(int i = 0; i < layers.length; i++) {
            layers[i] = new ZonePlan();
        }
    }
}
