/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.robotics.container;

import buildcraft.core.lib.gui.slots.SlotBase;
import buildcraft.lib.gui.ContainerBCTile;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.robotics.tile.TileZonePlanner;

public class ContainerZonePlanner extends ContainerBCTile<TileZonePlanner> {
    private static final int PLAYER_INV_START_X = 88;
    private static final int PLAYER_INV_START_Y = 146;

    public ContainerZonePlanner(EntityPlayer player, TileZonePlanner tile) {
        super(player, tile);
        addFullPlayerInventory(PLAYER_INV_START_X, PLAYER_INV_START_Y);

        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                addSlotToContainer(new SlotBase(tile.invPaintbrushes, x * 4 + y, 8 + x * 18, 146 + y * 18));
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
