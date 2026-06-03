/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): ContainerBCTile electronic library deferred
package buildcraft.builders.container;

import net.minecraft.entity.player.PlayerEntity;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.builders.tile.TileElectronicLibrary;

public class ContainerElectronicLibrary extends ContainerBCTile<TileElectronicLibrary> {
    public ContainerElectronicLibrary(PlayerEntity player, TileElectronicLibrary tile) {
        super(player, tile);
    }
}
