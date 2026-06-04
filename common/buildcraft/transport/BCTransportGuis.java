/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.transport;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

// STUB(R.Chen): Forge NetworkRegistry.INSTANCE.registerGuiHandler + GUI IDs removed.
// Replaced by Fabric ScreenHandlerType registration in BCTransportInitializer (Phase 5).
public enum BCTransportGuis {
    FILTERED_BUFFER,
    PIPE_DIAMOND,
    PIPE_DIAMOND_WOOD,
    PIPE_EMZULI;

    public static final BCTransportGuis[] VALUES = values();

    public static BCTransportGuis get(int id) {
        if (id < 0 || id >= VALUES.length) return null;
        return VALUES[id];
    }

    public void openGui(PlayerEntity player) {
        // STUB(R.Chen): Forge player.openGui → Fabric ScreenHandlerFactory (Phase 5).
    }

    public void openGui(PlayerEntity player, BlockPos pos) {
        // STUB(R.Chen): Phase 5.
    }
}
