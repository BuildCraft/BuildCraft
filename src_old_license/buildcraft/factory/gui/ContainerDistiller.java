package buildcraft.factory.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;

import buildcraft.core.lib.gui.BuildCraftContainer;
import buildcraft.core.lib.gui.widgets.FluidTankWidget;
import buildcraft.factory.tile.TileDistiller_BC8;
import buildcraft.lib.gui.slot.SlotValidated;

public class ContainerDistiller extends BuildCraftContainer {
    public final TileDistiller_BC8 distiller;

    public ContainerDistiller(EntityPlayer player, TileDistiller_BC8 distiller) {
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

        addWidget(new FluidTankWidget(distiller.getInputTank(), 44, 23, 16, 38).withOverlay(0, 161));
        addWidget(new FluidTankWidget(distiller.getOutputTankGas(), 98, 10, 34, 17).withOverlay(17, 161));
        addWidget(new FluidTankWidget(distiller.getOutputTankLiquid(), 98, 54, 34, 17).withOverlay(17, 161));
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }
}
