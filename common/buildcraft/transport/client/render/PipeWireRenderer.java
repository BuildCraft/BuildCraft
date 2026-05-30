/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.transport.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.util.DyeColor;

import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

// STUB(R.Chen): PipeWireRenderer (313 LOC) — Forge Tessellator/GL11 → Fabric VertexConsumer Phase 5.
// getWireSprite is a static accessor stub used by migrated Phase-4 code.
@Environment(EnvType.CLIENT)
public class PipeWireRenderer {

    public static void clearWireCache() {} // STUB Phase 5

    public static SpriteHolder getWireSprite(DyeColor colour) {
        return null; // STUB Phase 5
    }
}
