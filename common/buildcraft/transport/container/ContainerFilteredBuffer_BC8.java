/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.transport.container;

import net.minecraft.entity.player.PlayerEntity;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotPhantom;

import buildcraft.transport.tile.TileFilteredBuffer;

public class ContainerFilteredBuffer_BC8 extends ContainerBCTile<TileFilteredBuffer> {

    public ContainerFilteredBuffer_BC8(PlayerEntity player, int syncId, TileFilteredBuffer tile) {
        super(player, syncId, tile);
        addFullPlayerInventory(86);

        for (int i = 0; i < 9; i++) {
            // STUB(R.Chen): getBackgroundSprite() removed — Fabric Slot has no such hook.
            addSlot(new SlotPhantom(tile.invFilter, i, 8 + i * 18, 27) {
                @Override
                public boolean canAdjustCount() {
                    return false;
                }
            });
            addSlot(new SlotBase(tile.invMain, i, 8 + i * 18, 61));
        }
    }
}
