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

import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.api.transport.pipe.PipeFaceTex;

// STUB(R.Chen): PipeBaseModelGenStandard (419 LOC) — Forge BakedModel/IFaceBakery/Sprite
// → Fabric FRAPI MutableQuadView Phase 5. getItemSprites() stub used by block-particle effects.
@Environment(EnvType.CLIENT)
public class PipeBaseModelGenStandard implements IPipeBaseModelGen {

    public static final PipeBaseModelGenStandard INSTANCE = new PipeBaseModelGenStandard();

    // STUB(R.Chen): Phase 5 — Forge Sprite → net.minecraft.client.texture.Sprite.
    public net.minecraft.client.texture.Sprite[] getItemSprites(PipeDefinition def) {
        return new net.minecraft.client.texture.Sprite[0]; // STUB
    }
}
