/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.urbanism;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

import buildcraft.core.lib.gui.BuildCraftContainer;

public class ContainerUrbanist extends BuildCraftContainer {

    IInventory playerIInventory;
    TileUrbanist urbanist;

    public ContainerUrbanist(IInventory playerInventory, TileUrbanist urbanist) {
        super(urbanist.getSizeInventory());
        this.playerIInventory = playerInventory;
        this.urbanist = urbanist;

        /* addSlotToContainer(new Slot(builder, 0, 80, 27)); for (int k = 0; k < 3; k++) { for (int j1 = 0; j1 < 9;
         * j1++) { addSlotToContainer(new Slot(builder, 1 + j1 + k * 9, 8 + j1 * 18, 72 + k * 18)); } } for (int y = 0;
         * y < 3; y++) { for (int x = 0; x < 9; x++) { addSlotToContainer(new Slot(playerInventory, x + y * 9 + 9, 8 + x
         * * 18, 140 + y * 18)); } } for (int x = 0; x < 9; x++) { addSlotToContainer(new Slot(playerInventory, x, 8 + x
         * * 18, 198)); } */
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer) {
        return urbanist.isUseableByPlayer(entityplayer);
    }

}
