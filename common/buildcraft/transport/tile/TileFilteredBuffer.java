package buildcraft.transport.tile;

import net.minecraft.item.ItemStack;

import buildcraft.api.core.EnumPipePart;

import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.lib.tile.item.StackInsertionFunction;

public class TileFilteredBuffer extends TileBC_Neptune {
    public final ItemHandlerSimple invFilter;
    public final ItemHandlerSimple invMain;

    public TileFilteredBuffer() {
        invFilter = itemManager.addInvHandler("filter", 9, EnumAccess.PHANTOM);
        ItemHandlerSimple handler = new ItemHandlerSimple(9, this::canInsert, StackInsertionFunction.getDefaultInserter(), this::onSlotChange);
        invMain = itemManager.addInvHandler("main", handler, EnumAccess.BOTH, EnumPipePart.VALUES);
    }

    private boolean canInsert(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        ItemStack filterStack = invFilter.getStackInSlot(slot);
        return StackUtil.canMerge(filterStack, stack);
    }
}
