package buildcraft.transport.tile;

import net.minecraft.item.ItemStack;

import buildcraft.api.core.EnumPipePart;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.tile.TileBCInventory_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.lib.tile.item.StackInsertionFunction;

public class TileFilteredBuffer extends TileBCInventory_Neptune {
    public final ItemHandlerSimple invFilter;
    public final ItemHandlerSimple invMain;

    public TileFilteredBuffer() {
        invFilter = addInventory("filter", 9, ItemHandlerManager.EnumAccess.NONE);
        ItemHandlerSimple handler = new ItemHandlerSimple(9, this::canInsert, StackInsertionFunction.getDefaultInserter(), this::onSlotChange);
        invMain = addInventory("main", handler, ItemHandlerManager.EnumAccess.BOTH, EnumPipePart.VALUES);
    }

    private boolean canInsert(int slot, ItemStack stack) {
        if (stack == null) {
            return true;
        }
        ItemStack filterStack = invFilter.getStackInSlot(slot);
        return filterStack != null && StackUtil.canMerge(filterStack, stack);
    }
}
