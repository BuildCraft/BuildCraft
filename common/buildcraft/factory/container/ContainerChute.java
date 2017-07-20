/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.container;

import buildcraft.factory.tile.TileChute;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import net.minecraft.entity.player.EntityPlayer;

public class ContainerChute extends ContainerBCTile<TileChute> {
    public ContainerChute(EntityPlayer player, TileChute tile) {
        super(player, tile);
        addFullPlayerInventory(71);

        addSlotToContainer(new SlotBase(tile.inv, 0, 62, 18));
        addSlotToContainer(new SlotBase(tile.inv, 1, 80, 18));
        addSlotToContainer(new SlotBase(tile.inv, 2, 98, 18));
        addSlotToContainer(new SlotBase(tile.inv, 3, 80, 36));
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
