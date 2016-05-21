package buildcraft.builders.tile;

import net.minecraft.item.ItemStack;

import net.minecraftforge.items.IItemHandlerModifiable;

import buildcraft.api.core.EnumPipePart;
import buildcraft.lib.tile.TileBCInventory_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;

public class TileArchitect_Neptune extends TileBCInventory_Neptune {
    protected final IItemHandlerModifiable invBptIn = addInventory("bptIn", 1, EnumAccess.INSERT, EnumPipePart.VALUES);
    protected final IItemHandlerModifiable invBptOut = addInventory("bptOut", 1, EnumAccess.EXTRACT, EnumPipePart.VALUES);

    public TileArchitect_Neptune() {}

    @Override
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, ItemStack before, ItemStack after) {
        super.onSlotChange(handler, slot, before, after);
        if (handler == invBptIn) {
            if (after != null) {
                startScan();
            }
        }
    }
}
