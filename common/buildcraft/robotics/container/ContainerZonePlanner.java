/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.robotics.container;

import net.minecraft.entity.player.PlayerEntity;

import buildcraft.lib.gui.ContainerBCTile;

import buildcraft.robotics.tile.TileZonePlanner;

public class ContainerZonePlanner extends ContainerBCTile<TileZonePlanner> {
    public ContainerZonePlanner(PlayerEntity player, TileZonePlanner tile) {
        super(player, tile);
        addFullPlayerInventory(88, 146);
        // STUB(R.Chen): slot wiring deferred until TileZonePlanner inventories are ported — Phase 10
    }
}
