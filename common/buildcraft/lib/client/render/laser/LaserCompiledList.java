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

/**
 * STUB(R.Chen): client render — full implementation in Phase 5.
 *
 * The Forge original compiled laser geometry into either a GL display list ({@code GLAllocation} +
 * {@code GL11.glNewList}) or a {@code VertexBuffer} (VBO), driven by {@code GlStateManager}/
 * {@code OpenGlHelper} and a {@code RenderUtil} thread-local tessellator. 1.20.1 has no display lists
 * and a different VBO/RenderSystem pipeline, so this is deferred to the dedicated render pass. The
 * abstract {@link #render}/{@link #delete} surface and the {@link Builder} are kept as no-ops.
 */
@Environment(EnvType.CLIENT)
public abstract class LaserCompiledList {
    public abstract void render();

    public abstract void delete();

    public static class Builder implements ILaserRenderer, AutoCloseable {
        public Builder(boolean useNormalColour) {
            // STUB(R.Chen): OpenGL — tessellator setup deferred to Phase 5.
        }

        @Override
        public void vertex(double x, double y, double z, double u, double v, int lmap, float nx, float ny, float nz, float diffuse) {
            // STUB(R.Chen): client render — vertex accumulation deferred to Phase 5.
        }

        public LaserCompiledList build() {
            // STUB(R.Chen): OpenGL — display-list / VBO compile deferred to Phase 5.
            return new Noop();
        }

        @Override
        public void close() {
            // STUB(R.Chen): OpenGL — tessellator release deferred to Phase 5.
        }
    }

    /** Placeholder compiled list that renders nothing until the Phase 5 render pass. */
    private static class Noop extends LaserCompiledList {
        @Override
        public void render() {
            // STUB(R.Chen): OpenGL — display-list / VBO draw deferred to Phase 5.
        }

        @Override
        public void delete() {
            // STUB(R.Chen): OpenGL — GL resource deletion deferred to Phase 5.
        }
    }
}
