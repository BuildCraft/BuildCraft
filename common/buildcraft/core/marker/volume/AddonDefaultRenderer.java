/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.core.marker.volume;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// STUB(R.Chen): client render Phase 5.
// Yarn 1.20.1:
//   BufferBuilder          → net.minecraft.client.render.BufferBuilder
//   Sprite     → net.minecraft.client.texture.Sprite
//   ModelLoader.White      → no Fabric equivalent; use RenderSystem / solid-white approach instead.
// The original vertex-building calls (builder.pos/color/tex/lightmap/endVertex) map to
// VertexConsumer.vertex/color/texture/light/next in Fabric's render pipeline; deferred to Phase 5.
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.entity.player.PlayerEntity;

@Environment(EnvType.CLIENT)
public class AddonDefaultRenderer<T extends Addon> implements IFastAddonRenderer<T> {

    public AddonDefaultRenderer() {
        // STUB(R.Chen): buildcraft.lib.compat.McTextureCompat.getMissingSprite() dropped; Fabric uses solid-white sprite differently.
    }

    public AddonDefaultRenderer(Object sprite) {
        // STUB(R.Chen): Sprite → net.minecraft.client.texture.Sprite (Phase 5).
    }

    @Override
    public void renderAddonFast(T addon, PlayerEntity player, float partialTicks, BufferBuilder builder) {
        // STUB(R.Chen): client render Phase 5 — BufferBuilder vertex calls require VertexFormat rewrite.
    }
}
