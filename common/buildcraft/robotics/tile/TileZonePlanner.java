package buildcraft.robotics.tile;

import buildcraft.lib.tile.TileBCInventory_Neptune;

public class TileZonePlanner extends TileBCInventory_Neptune {
//    public final ItemHandlerSimple invFilter;
//    public final ItemHandlerSimple invMain;

    public TileZonePlanner() {
//        invFilter = addInventory("filter", 9, ItemHandlerManager.EnumAccess.NONE);
//        invMain = addInventory("main", new ItemHandlerSimple(9, this::onSlotChange) {
//            @Override
//            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
//                if(invFilter.getStackInSlot(slot) != null  && StackUtil.canMerge(invFilter.getStackInSlot(slot), stack)) {
//                    return super.insertItem(slot, stack, simulate);
//                }
//                return stack;
//            }
//        }, ItemHandlerManager.EnumAccess.INSERT, EnumPipePart.VALUES);
    }
}
