/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.model.key;

import java.util.Objects;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pluggable.PluggableModelKey;

public class KeyPlugLens extends PluggableModelKey {
    public final EnumDyeColor colour;
    public final boolean isFilter;
    private final int hash;

    public KeyPlugLens(BlockRenderLayer layer, EnumFacing side, EnumDyeColor colour, boolean isFilter) {
        super(layer, side);
        this.colour = colour;
        this.isFilter = isFilter;
        this.hash = Objects.hash(layer, side, colour, isFilter);
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
        KeyPlugLens other = (KeyPlugLens) obj;
        return other.isFilter == isFilter//
            && other.layer == layer//
            && other.colour == colour//
            && other.side == side;
    }
}
