/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon.client.model.key;

import java.util.Objects;

import ct.buildcraft.api.transport.pluggable.PluggableModelKey;
import ct.buildcraft.silicon.gate.GateVariant;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;

public class KeyPlugGate extends PluggableModelKey {
    public final GateVariant variant;
    public final int hash;

    public KeyPlugGate(Direction side, GateVariant variant) {
        super(RenderType.cutout(), side);
        this.variant = variant;
        this.hash = Objects.hash(variant, side);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        KeyPlugGate other = (KeyPlugGate) obj;
        return side == other.side && variant.equals(other.variant);
    }
}
