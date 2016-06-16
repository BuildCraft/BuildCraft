/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

import buildcraft.builders.tile.TileLibrary_Neptune;
import buildcraft.core.lib.gui.slots.SlotOutput;
import buildcraft.lib.gui.ContainerBCTile;

public class ContainerBlueprintLibrary extends ContainerBCTile<TileLibrary_Neptune> {
    protected IInventory playerInventory;

    private int progressIn, progressOut;

    public ContainerBlueprintLibrary(EntityPlayer player, TileLibrary_Neptune library) {
        super(player, library);
        this.playerInventory = player.inventory;

        addSlotToContainer(new SlotBlueprintLibrary(library, library.inv, player, 0, 219, 57));
        addSlotToContainer(new SlotOutput(library.inv, 1, 175, 57));

        addSlotToContainer(new SlotBlueprintLibrary(library, library.inv, player, 2, 175, 79));
        addSlotToContainer(new SlotOutput(library.inv, 3, 219, 79));

        // Player inventory
        for (int l = 0; l < 3; l++) {
            for (int k1 = 0; k1 < 9; k1++) {
                addSlotToContainer(new Slot(playerInventory, k1 + l * 9 + 9, 8 + k1 * 18, 138 + l * 18));
            }
        }

        for (int i1 = 0; i1 < 9; i1++) {
            addSlotToContainer(new Slot(playerInventory, i1, 8 + i1 * 18, 196));
        }
    }

    // @Override
    // public void detectAndSendChanges() {
    // super.detectAndSendChanges();
    // for (IContainerListener listener : this.listeners) {
    // if (progressIn != tile.progressIn) {
    // listener.sendProgressBarUpdate(this, 0, tile.progressIn);
    // }
    // if (progressOut != tile.progressOut) {
    // listener.sendProgressBarUpdate(this, 1, tile.progressOut);
    // }
    // }
    //
    // progressIn = tile.progressIn;
    // progressOut = tile.progressOut;
    // }

    // @Override
    // public void updateProgressBar(int i, int j) {
    // if (i == 0) {
    // tile.progressIn = j;
    // } else if (i == 1) {
    // tile.progressOut = j;
    // }
    // }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer) {
        return tile.canInteractWith(entityplayer);
    }
}
