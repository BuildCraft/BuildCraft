/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.model.key;

import java.util.Objects;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pluggable.PluggableModelKey;

public class KeyPlugFacade extends PluggableModelKey {
    public final IBlockState state;
    public final boolean isHollow;
    private final int hash;

    public KeyPlugFacade(BlockRenderLayer layer, EnumFacing side, IBlockState state, boolean isHollow) {
        super(layer, side);
        this.state = state;
        this.isHollow = isHollow;
        this.hash = Objects.hash(layer, side, state, isHollow);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        KeyPlugFacade other = (KeyPlugFacade) obj;
        return other.isHollow == isHollow//
                && other.layer == layer//
                && other.state == state//
                && other.side == side;
    }
}
