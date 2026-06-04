/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
// STUB(R.Chen): TileElectronicLibrary deferred
package buildcraft.builders.tile;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;

import buildcraft.lib.tile.TileBC_Neptune;

public class TileElectronicLibrary extends TileBC_Neptune {

    public static BlockEntityType<TileElectronicLibrary> TYPE;

    public TileElectronicLibrary(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }
}
