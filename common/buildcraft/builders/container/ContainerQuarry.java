package buildcraft.builders.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import buildcraft.builders.block.BlockFrame;
import buildcraft.builders.tile.TileQuarry;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;

public class ContainerQuarry extends ContainerBCTile<TileQuarry> {
    public ContainerQuarry(EntityPlayer player, TileQuarry tile) {
        super(player, tile);
        addFullPlayerInventory(50);

        for(int i = 0; i < 9; i++) {
            addSlotToContainer(new SlotBase(tile.invFrames, i, 8 + i * 18, 18) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return stack != null && stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).getBlock() instanceof BlockFrame;
                }
            });
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
