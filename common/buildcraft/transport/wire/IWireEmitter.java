/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.wire;

import net.minecraft.item.EnumDyeColor;

public interface IWireEmitter {
    /** Checks to see if this wire emitter is currently emitting the given colour. Only used to check if a given emitter
     * is still active. */
    boolean isEmitting(EnumDyeColor colour);

    /** Emits the given wire colour this tick. */
    void emitWire(EnumDyeColor colour);
}
