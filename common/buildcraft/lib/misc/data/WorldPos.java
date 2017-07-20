/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.data;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public final class WorldPos {
    public final BlockPos pos;
    public final int dimension;
    private final int hash;

    public WorldPos(TileEntity tile) {
        this(tile.getPos(), tile.getWorld().provider.getDimension());
    }

    public WorldPos(BlockPos pos, int dimension) {
        this.pos = pos.getClass() == BlockPos.class ? pos : new BlockPos(pos);
        this.dimension = dimension;
        hash = Objects.hash(pos, dimension);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) {
            return false;
        }
        WorldPos other = (WorldPos) obj;
        return dimension == other.dimension && pos.equals(other.pos);
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
