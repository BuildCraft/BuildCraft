package buildcraft.builders.container;

import buildcraft.builders.item.ItemSnapshot;
import buildcraft.builders.tile.TileBuilder;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ContainerBuilder extends ContainerBCTile<TileBuilder> {
    public ContainerBuilder(EntityPlayer player, TileBuilder tile) {
        super(player, tile);

        addFullPlayerInventory(140);

        addSlotToContainer(new SlotBase(tile.invBlueprint, 0, 80, 27) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() instanceof ItemSnapshot && ItemSnapshot.EnumItemSnapshotType.getFromStack(stack).used;
            }
        });

        for (int sy = 0; sy < 3; sy++) {
            for (int sx = 0; sx < 9; sx++) {
                addSlotToContainer(new SlotBase(tile.invResources, sx + sy * 9, 8 + sx * 18, 72 + sy * 18));
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
