package buildcraft.builders.container;

import buildcraft.builders.item.ItemSnapshot;
import net.minecraft.entity.player.EntityPlayer;

import buildcraft.builders.tile.TileArchitectTable;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotOutput;
import net.minecraft.item.ItemStack;

public class ContainerArchitectTable extends ContainerBCTile<TileArchitectTable> {
    public ContainerArchitectTable(EntityPlayer player, TileArchitectTable tile) {
        super(player, tile);
        addFullPlayerInventory(88, 84);

        addSlotToContainer(new SlotBase(tile.invBptIn, 0, 135, 35) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() instanceof ItemSnapshot;
            }
        });
        addSlotToContainer(new SlotOutput(tile.invBptOut, 0, 194, 35));
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
