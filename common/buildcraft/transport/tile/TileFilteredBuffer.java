package buildcraft.transport.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.tile.TileBCInventory_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.item.ItemStack;

public class TileFilteredBuffer extends TileBCInventory_Neptune {
    public final ItemHandlerSimple invFilter;
    public final ItemHandlerSimple invMain;

    public TileFilteredBuffer() {
        invFilter = addInventory("filter", 9, ItemHandlerManager.EnumAccess.NONE);
        invMain = addInventory("main", new ItemHandlerSimple(9, this::onSlotChange) {
            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                if(invFilter.getStackInSlot(slot) != null  && StackUtil.canMerge(invFilter.getStackInSlot(slot), stack)) {
                    return super.insertItem(slot, stack, simulate);
                }
                return stack;
            }
        }, ItemHandlerManager.EnumAccess.INSERT, EnumPipePart.VALUES);
    }
}
