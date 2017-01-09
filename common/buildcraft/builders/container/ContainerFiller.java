package buildcraft.builders.container;

import buildcraft.builders.filling.Filling;
import buildcraft.builders.tile.TileFiller;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ContainerFiller extends ContainerBCTile<TileFiller> {
    private static final int PLAYER_INV_START = 153;

    public ContainerFiller(EntityPlayer player, TileFiller tile) {
        super(player, tile);

        addFullPlayerInventory(PLAYER_INV_START);

        for (int sy = 0; sy < 3; sy++) {
            for (int sx = 0; sx < 9; sx++) {
                addSlotToContainer(new SlotBase(tile.invResources, sx + sy * 9, 8 + sx * 18, 85 + sy * 18) {
                    @Override
                    public boolean isItemValid(ItemStack stack) {
                        return Filling.INSTANCE.getItemBlocks().contains(stack.getItem());
                    }
                });
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
