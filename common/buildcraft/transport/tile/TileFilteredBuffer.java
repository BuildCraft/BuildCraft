package buildcraft.transport.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.lib.tile.TileBCInventory_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerSimple;

public class TileFilteredBuffer extends TileBCInventory_Neptune {
    public final ItemHandlerSimple invFilter;
    public final ItemHandlerSimple invMain;

    public TileFilteredBuffer() {
        invFilter = addInventory("blueprint", 9, ItemHandlerManager.EnumAccess.NONE);;
        invMain = addInventory("materials", 9, ItemHandlerManager.EnumAccess.INSERT, EnumPipePart.VALUES);
    }
}
