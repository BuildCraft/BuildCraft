package buildcraft.silicon.tile;

import java.util.List;

import net.minecraft.util.EnumFacing;

import buildcraft.api.core.EnumPipePart;

import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerSimple;

public class TileAdvancedCraftingTable extends TileLaserTableBase {
    public final ItemHandlerSimple invBlueprint = addInventory("blueprint", 3 * 3, ItemHandlerManager.EnumAccess.NONE);
    public final ItemHandlerSimple invMaterials = addInventory("materials", 5 * 3, ItemHandlerManager.EnumAccess.INSERT, EnumPipePart.VALUES);;
    public final ItemHandlerSimple invResults = addInventory("result", 3 * 3, ItemHandlerManager.EnumAccess.EXTRACT, EnumPipePart.VALUES);

    public long getTarget() {
        return 1000000000;
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        super.getDebugInfo(left, right, side);
        left.add("target - " + getTarget());
    }

    @Override
    public void update() {
        super.update();

        if(world.isRemote) {
            return;
        }

        sendNetworkGuiUpdate(NET_GUI_DATA);
    }

    @Override
    public boolean hasWork() {
        return true;
    }
}
