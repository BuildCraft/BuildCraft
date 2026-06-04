/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.lib.client.render.laser;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.render.BufferBuilder;

import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import buildcraft.lib.misc.data.Box;

/**
 * STUB(R.Chen): client render — full implementation in Phase 5.
 *
 * The Forge original built the 12 edge lasers of a {@link Box} and cached them on the box's
 * {@code laserData}/{@code lastMin}/{@code lastMax}/{@code lastType} client fields (which were stubbed
 * out of {@link Box} during its migration). Restoring this needs those cache fields plus the migrated
 * {@link LaserRenderer_BC8}; both are part of the dedicated render pass. The public entry points are
 * kept as no-ops so callers compile.
 */
@Environment(EnvType.CLIENT)
public class LaserBoxRenderer {

    public static void renderLaserBoxStatic(Box box, LaserType type, boolean center) {
        // STUB(R.Chen): client render — edge-laser baking + static draw deferred to Phase 5.
    }

    public static void renderLaserBoxDynamic(Box box, LaserType type, BufferBuilder bb, boolean center) {
        // STUB(R.Chen): client render — edge-laser baking + dynamic draw deferred to Phase 5.
    }
}
