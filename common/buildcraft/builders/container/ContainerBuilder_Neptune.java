package buildcraft.builders.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import buildcraft.builders.item.ItemBlueprint;
import buildcraft.builders.tile.TileBuilder_Neptune;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;

public class ContainerBuilder_Neptune extends ContainerBCTile<TileBuilder_Neptune> {
    public ContainerBuilder_Neptune(EntityPlayer player, TileBuilder_Neptune tile) {
        super(player, tile);

        addFullPlayerInventory(140);

        addSlotToContainer(new SlotBase(tile.invBlueprint, 0, 80, 27) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack == null ? true : stack.getItem() instanceof ItemBlueprint && stack.getMetadata() == ItemBlueprint.META_USED;
            }
        });

        final int startX = 8;
        final int startY = 72;

        for (int sy = 0; sy < 3; sy++) {
            for (int sx = 0; sx < 9; sx++) {
                addSlotToContainer(new SlotBase(tile.invResources, sx + sy * 9, startX + sx * 18, startY + sy * 18));
            }
        }
    }
}
