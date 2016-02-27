package buildcraft.factory.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;

import buildcraft.core.lib.gui.BuildCraftContainer;
import buildcraft.core.lib.gui.slots.SlotValidated;
import buildcraft.core.lib.gui.widgets.FluidGaugeWidget;
import buildcraft.factory.tile.TileEnergyHeater;

public class ContainerEnergyHeater extends BuildCraftContainer {
    public final TileEnergyHeater heater;

    public ContainerEnergyHeater(EntityPlayer player, TileEnergyHeater heater) {
        super(player, 2);
        this.heater = heater;

        addSlot(new SlotValidated(heater, 0, 8, 23));
        addSlot(new SlotValidated(heater, 1, 152, 23));

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addSlotToContainer(new Slot(player.inventory, x + y * 9 + 9, 8 + x * 18, 62 + y * 18));
            }
        }

        for (int x = 0; x < 9; x++) {
            addSlotToContainer(new Slot(player.inventory, x, 8 + x * 18, 120));
        }

        addWidget(new FluidGaugeWidget(heater.getInputTank(), 44, 12, 16, 38));
        addWidget(new FluidGaugeWidget(heater.getOutputTank(), 116, 12, 16, 38));
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }
}
