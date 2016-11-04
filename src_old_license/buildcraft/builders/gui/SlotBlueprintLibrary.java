/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.gui;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.items.IItemHandler;

import buildcraft.builders.tile.TileLibrary_Neptune;
import buildcraft.lib.gui.slot.SlotBase;

public class SlotBlueprintLibrary extends SlotBase {
    private TileLibrary_Neptune library;
    private EntityPlayer player;
    private int slot;

    public SlotBlueprintLibrary(TileLibrary_Neptune library, IItemHandler itemHandler, EntityPlayer player, int slotIndex, int posX, int posY) {
        super(itemHandler, slotIndex, posX, posY);
        this.library = library;
        this.slot = slotIndex;
        this.player = player;
    }

    @Override
    public void onSlotChanged() {
        // When downloading or uploading a blueprint, the server needs to know
        // who requested it. The way to do it so far is by recording the last
        // player that clicks on the slots. To be improved if the method is
        // not robust enough (e.g. what if the player is not logged anymore?
        // is that robust against race conditions? etc.)

        // We can change this now, right?
        // ~ AlexIIL
        if (slot == 0) {
//            library.uploadingPlayer = player;
        } else if (slot == 2) {
//            library.downloadingPlayer = player;
        }

        this.inventory.markDirty();
    }
}
