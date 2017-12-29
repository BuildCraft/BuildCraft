/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory.container;

import net.minecraft.entity.player.EntityPlayer;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotDisplay;
import buildcraft.lib.gui.slot.SlotOutput;
import buildcraft.lib.gui.slot.SlotPhantom;

import buildcraft.factory.tile.TileAutoWorkbenchItems;

public class ContainerAutoCraftItems extends ContainerBCTile<TileAutoWorkbenchItems> {
    public ContainerAutoCraftItems(EntityPlayer player, TileAutoWorkbenchItems tile) {
        super(player, tile);

        addSlotToContainer(new SlotOutput(tile.invResult, 0, 124, 35));
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                addSlotToContainer(new SlotPhantom(tile.invBlueprint, x + y * 3, 30 + x * 18, 17 + y * 18, false));
            }
        }
        for (int x = 0; x < 9; x++) {
            addSlotToContainer(new SlotBase(tile.invMaterials, x, 8 + x * 18, 84));
        }
        addSlotToContainer(new SlotDisplay(i -> tile.resultClient, 0, 93, 27));

        addFullPlayerInventory(115);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
