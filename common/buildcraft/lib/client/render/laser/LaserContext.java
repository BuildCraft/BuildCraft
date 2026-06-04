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

import net.minecraft.util.math.Vec3d;

/**
 * STUB(R.Chen): client render — full implementation in Phase 5.
 *
 * The Forge original built a {@code javax.vecmath} (Matrix4f/Point3f/Vector3f/Vector4f) transform and
 * fed transformed quads to an {@link ILaserRenderer}, using {@code MutableQuad.diffuseLight} for
 * lighting and {@code LaserRenderer_BC8.computeLightmap} for the lightmap. Migrating that math to
 * {@code org.joml} (whose API differs substantially) is part of the dedicated render pass. Only the
 * compile-time surface that {@link CompiledLaserRow}/{@link CompiledLaserType} bake against is kept:
 * {@link #length}, {@link #setFaceNormal}, {@link #addPoint}. Baking is a no-op for now.
 */
public class LaserContext {
    public final double length;

    private final ILaserRenderer renderer;

    public LaserContext(ILaserRenderer renderer, LaserData_BC8 data, boolean useNormalColour, boolean isCullEnabled) {
        this.renderer = renderer;
        // Real length is |start-end| / scale; preserved so baking math stays sensible once render lands.
        Vec3d delta = data.start.subtract(data.end);
        this.length = delta.length() / data.scale;
    }

    @Environment(EnvType.CLIENT)
    public void setFaceNormal(double nx, double ny, double nz) {
        // STUB(R.Chen): client render — normal/diffuse-light computation deferred to Phase 5.
    }

    @Environment(EnvType.CLIENT)
    public void addPoint(double xIn, double yIn, double zIn, double uIn, double vIn) {
        // STUB(R.Chen): client render — quad emission (matrix transform + lightmap) deferred to Phase 5.
    }
}
