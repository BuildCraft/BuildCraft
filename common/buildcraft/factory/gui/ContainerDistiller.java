package buildcraft.factory.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;

import buildcraft.core.lib.gui.BuildCraftContainer;
import buildcraft.core.lib.gui.slots.SlotValidated;
import buildcraft.factory.tile.TileDistiller;

public class ContainerDistiller extends BuildCraftContainer {
    public final TileDistiller distiller;

    public ContainerDistiller(EntityPlayer player, TileDistiller distiller) {
        super(player, 3);
        this.distiller = distiller;

        addSlot(new SlotValidated(distiller, 0, 8, 35));
        addSlot(new SlotValidated(distiller, 1, 152, 10));
        addSlot(new SlotValidated(distiller, 2, 152, 55));

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addSlotToContainer(new Slot(player.inventory, x + y * 9 + 9, 8 + x * 18, 79 + y * 18));
            }
        }

        for (int x = 0; x < 9; x++) {
            addSlotToContainer(new Slot(player.inventory, x, 8 + x * 18, 137));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }
}
