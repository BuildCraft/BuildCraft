/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.gui;

import javax.annotation.Nullable;

import ct.buildcraft.lib.tile.TileBC_Neptune;

/** Exposes the backing BuildCraft tile to GUI helpers without forcing every menu to extend ContainerBCTile. */
public interface IMenuBCTile {
    @Nullable
    TileBC_Neptune getBCTile();
}
