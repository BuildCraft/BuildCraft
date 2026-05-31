/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.silicon.plug;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;

/**
 * STUB(R.Chen): PluggableLens implementation deferred until the full pluggable system
 * and dependent APIs are migrated to Fabric 1.20.1.
 */
public class PluggableLens extends PipePluggable {

    public PluggableLens(PluggableDefinition def, IPipeHolder holder, Direction side) {
        super(def, holder, side);
    }

    public PluggableLens(PluggableDefinition def, IPipeHolder holder, Direction side, NbtCompound nbt) {
        super(def, holder, side);
        // STUB(R.Chen): NBT loading deferred.
    }

    public PluggableLens(PluggableDefinition def, IPipeHolder holder, Direction side, PacketByteBuf buf) {
        super(def, holder, side);
        // STUB(R.Chen): Net loading deferred.
    }

    @Override
    public Box getBoundingBox() {
        // STUB(R.Chen): Direction-specific bounding box deferred.
        return new Box(5/16.0, 5/16.0, 5/16.0, 11/16.0, 11/16.0, 11/16.0);
    }
}
