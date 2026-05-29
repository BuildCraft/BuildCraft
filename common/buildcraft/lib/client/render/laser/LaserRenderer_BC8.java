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
import net.minecraft.client.render.VertexFormat;

/**
 * STUB(R.Chen): client render — full implementation in Phase 5.
 *
 * The Forge original cached compiled laser geometry (Guava {@code LoadingCache}), computed lightmaps
 * via {@code World#getLightFor(EnumSkyBlock,...)}, and emitted vertices through {@code BufferBuilder}
 * with custom {@code VertexFormat}s built from {@code DefaultVertexFormats}. All of that is part of the
 * dedicated render pass. The public surface (format fields + the render/compute entry points) is kept
 * so callers compile; bodies are no-ops / return defaults.
 */
@Environment(EnvType.CLIENT)
public class LaserRenderer_BC8 {

    // STUB(R.Chen): vertex formats — rebuilt from VertexFormats/VertexFormatElement in Phase 5.
    public static final VertexFormat FORMAT_LESS = null;
    public static final VertexFormat FORMAT_ALL = null;

    public static void clearModels() {
        // STUB(R.Chen): client render — compiled-laser cache invalidation deferred to Phase 5.
    }

    public static int computeLightmap(double x, double y, double z, int minBlockLight) {
        // STUB(R.Chen): client render — world lightmap sampling deferred to Phase 5.
        return 0;
    }

    public static void renderLaserStatic(LaserData_BC8 data) {
        // STUB(R.Chen): client render — static (display-list/VBO) laser draw deferred to Phase 5.
    }

    public static void renderLaserDynamic(LaserData_BC8 data, BufferBuilder buffer) {
        // STUB(R.Chen): client render — dynamic laser draw deferred to Phase 5.
    }
}
