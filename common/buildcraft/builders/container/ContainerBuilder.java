package buildcraft.builders.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import buildcraft.builders.item.ItemBlueprint;
import buildcraft.builders.tile.TileBuilder;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;

public class ContainerBuilder extends ContainerBCTile<TileBuilder> {
    public ContainerBuilder(EntityPlayer player, TileBuilder tile) {
        super(player, tile);

        addFullPlayerInventory(140);

        addSlotToContainer(new SlotBase(tile.invBlueprint, 0, 80, 27) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack == null || stack.getItem() instanceof ItemBlueprint && stack.getMetadata() == ItemBlueprint.META_USED;
            }
        });

        for (int sy = 0; sy < 3; sy++) {
            for (int sx = 0; sx < 9; sx++) {
                addSlotToContainer(new SlotBase(tile.invResources, sx + sy * 9, 8 + sx * 18, 72 + sy * 18));
            }
        }
    }
}
