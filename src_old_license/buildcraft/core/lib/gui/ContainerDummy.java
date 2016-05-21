package buildcraft.core.lib.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;

public class ContainerDummy extends Container {
    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return false;
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventory) {}
}
