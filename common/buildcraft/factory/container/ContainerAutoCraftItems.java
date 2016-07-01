/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory.container;

import net.minecraft.entity.player.EntityPlayer;

import buildcraft.core.lib.gui.slots.SlotBase;
import buildcraft.core.lib.gui.slots.SlotOutput;
import buildcraft.core.lib.gui.slots.SlotPhantom;
import buildcraft.factory.tile.TileAutoWorkbenchItems;
import buildcraft.lib.gui.ContainerBCTile;

public class ContainerAutoCraftItems extends ContainerBCTile<TileAutoWorkbenchItems> {
    private static final int PLAYER_INV_START = 115;

    public ContainerAutoCraftItems(EntityPlayer player, TileAutoWorkbenchItems tile) {
        super(player, tile);
        addFullPlayerInventory(PLAYER_INV_START);

        for (int x = 0; x < 9; x++) {
            addSlotToContainer(new SlotBase(tile.invMaterials, x, 8 + x * 18, 84));
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                addSlotToContainer(new SlotPhantom(tile.invBlueprint, x + y * 3, 30 + x * 18, 17 + y * 18));
            }
        }

        addSlotToContainer(new SlotOutput(tile.invResult, 0, 124, 35));

        if (!tile.getWorld().isRemote) {
            // Delta system test
            tile.deltaProgress.addDelta(0, 200, 100);
            tile.deltaProgress.addDelta(200, 220, -100);
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
