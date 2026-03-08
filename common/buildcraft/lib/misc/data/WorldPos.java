/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.data;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class WorldPos {
    @SuppressWarnings("WeakerAccess")
//    public final int dimension;
    public final String dimension;
    public final BlockPos pos;

    @SuppressWarnings("WeakerAccess")
    public WorldPos(String dimension, BlockPos pos) {
        this.dimension = dimension;
        this.pos = pos.getClass() == BlockPos.class ? pos : new BlockPos(pos);
    }

    public WorldPos(World world, BlockPos pos) {
        this(world.dimension().location().toString(), pos);
    }

    public WorldPos(TileEntity tile) {
        this(tile.getLevel(), tile.getBlockPos());
    }

    @Override
    public boolean equals(Object o) {
        return this == o ||
                o != null &&
                        getClass() == o.getClass() &&
//                        dimension == ((WorldPos) o).dimension &&
                        dimension.equals(((WorldPos) o).dimension) &&
                        pos.equals(((WorldPos) o).pos);

    }

    @Override
    public int hashCode() {
//        return 31 * dimension + pos.hashCode();
        return 31 * dimension.hashCode() + pos.hashCode();
    }
}
