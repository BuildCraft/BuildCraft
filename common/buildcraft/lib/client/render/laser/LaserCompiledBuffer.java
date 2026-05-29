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

/**
 * STUB(R.Chen): client render — full implementation in Phase 5.
 *
 * The Forge original buffered vertex data in trove primitive lists ({@code gnu.trove}, no longer on the
 * classpath) and replayed it into a {@code BufferBuilder} using the old {@code pos/color/tex/lightmap}
 * API. Vertex emission is part of the dedicated render pass; the {@link Builder}/{@link #render} surface
 * is kept as no-ops so callers compile.
 */
@Environment(EnvType.CLIENT)
public class LaserCompiledBuffer {

    public LaserCompiledBuffer() {
    }

    public void render(BufferBuilder buffer) {
        // STUB(R.Chen): client render — vertex replay deferred to Phase 5.
    }

    public static class Builder implements ILaserRenderer {
        public Builder(boolean useNormalColour) {
        }

        @Override
        public void vertex(double x, double y, double z, double u, double v, int lmap, float nx, float ny, float nz, float diffuse) {
            // STUB(R.Chen): client render — vertex accumulation deferred to Phase 5.
        }

        public LaserCompiledBuffer build() {
            return new LaserCompiledBuffer();
        }
    }
}
