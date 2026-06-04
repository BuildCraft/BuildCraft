/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.transport.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// STUB(R.Chen): PipeModelCachePluggable — was referenced by the old BlockPipeHolder addHitEffects /
// addDestroyEffects for pluggable-specific particle sprites. Null caches until Phase 5 FRAPI.
@Environment(EnvType.CLIENT)
public class PipeModelCachePluggable {

    // STUB(R.Chen): Phase 5 — caches for cutout and translucent pluggable quads.
    public static final Object cacheCutoutSingle = null;
    public static final Object cacheTranslucentSingle = null;
}
