/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.model.key;

import buildcraft.api.transport.pluggable.PluggableModelKey;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;

import java.util.Objects;

public class KeyPlugLens extends PluggableModelKey {
    public final DyeColor colour;
    public final boolean isFilter;
    private final int hash;

    public KeyPlugLens(RenderType layer, Direction side, DyeColor colour, boolean isFilter) {
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
