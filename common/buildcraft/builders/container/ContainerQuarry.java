package buildcraft.builders.container;

import buildcraft.builders.block.BlockFrame;
import buildcraft.builders.tile.TileQuarry;
import buildcraft.core.lib.gui.slots.SlotBase;
import buildcraft.lib.gui.ContainerBCTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ContainerQuarry extends ContainerBCTile<TileQuarry> {
    private static final int PLAYER_INV_START = 50;

    public ContainerQuarry(EntityPlayer player, TileQuarry tile) {
        super(player, tile);
        addFullPlayerInventory(PLAYER_INV_START);

        for(int i = 0; i < 9; i++) {
            addSlotToContainer(new SlotBase(tile.invFrames, i, 8 + i * 18, 18) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).getBlock() instanceof BlockFrame;
                }
            });
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
