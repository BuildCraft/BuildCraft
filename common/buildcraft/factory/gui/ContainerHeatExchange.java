package buildcraft.factory.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;

import buildcraft.core.lib.gui.BuildCraftContainer;
import buildcraft.core.lib.gui.slots.SlotValidated;
import buildcraft.factory.tile.TileHeatExchange;

public class ContainerHeatExchange extends BuildCraftContainer {
    public final TileHeatExchange heatExchange;

    public ContainerHeatExchange(EntityPlayer player, TileHeatExchange heatExchange) {
        super(player, 3);
        this.heatExchange = heatExchange;

        addSlot(new SlotValidated(heatExchange, 0, 8, 23));
        addSlot(new SlotValidated(heatExchange, 1, 8, 64));
        addSlot(new SlotValidated(heatExchange, 2, 152, 12));
        addSlot(new SlotValidated(heatExchange, 3, 152, 54));

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addSlotToContainer(new Slot(player.inventory, x + y * 9 + 9, 8 + x * 18, 89 + y * 18));
            }
        }

        for (int x = 0; x < 9; x++) {
            addSlotToContainer(new Slot(player.inventory, x, 8 + x * 18, 147));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

}
