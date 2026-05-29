/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.client.sprite;

import java.util.HashMap;
import java.util.Map;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;

import buildcraft.api.core.render.ISprite;

/**
 * STUB(R.Chen): client render — full implementation in Phase 5.
 *
 * The Forge original stitched BuildCraft sprites into the block texture atlas via
 * {@code TextureStitchEvent} + raw GL ({@code GL11}/{@code GL12}/{@code GlStateManager}/{@code TextureMap}).
 * Fabric uses a different texturing pipeline ({@code SpriteAtlasTexture} + data-driven atlases /
 * {@code ClientSpriteRegistryCallback}). This stub keeps only the compile-time surface that the
 * laser/marker data layer needs: the {@link SpriteHolder} {@link ISprite} implementation and the
 * {@link #getHolder} factories. UV interpolation returns the identity passthrough until the real
 * atlas wiring lands.
 */
public class SpriteHolderRegistry {

    private static final Map<Identifier, SpriteHolder> holders = new HashMap<>();

    public static SpriteHolder getHolder(String location) {
        return getHolder(new Identifier(location));
    }

    public static SpriteHolder getHolder(Identifier location) {
        return holders.computeIfAbsent(location, SpriteHolder::new);
    }

    public static class SpriteHolder implements ISprite {
        public final Identifier spriteLocation;

        // STUB(R.Chen): the backing atlas Sprite is resolved during stitching in Phase 5.
        @Environment(EnvType.CLIENT)
        private Sprite sprite;

        private SpriteHolder(Identifier spriteLocation) {
            this.spriteLocation = spriteLocation;
        }

        @Environment(EnvType.CLIENT)
        public Sprite getSprite() {
            // STUB(R.Chen): client render — returns the stitched atlas sprite in Phase 5.
            return sprite;
        }

        @Override
        public double getInterpU(double u) {
            // STUB(R.Chen): client render — identity passthrough until atlas UVs are wired (Phase 5).
            return u;
        }

        @Override
        public double getInterpV(double v) {
            // STUB(R.Chen): client render — identity passthrough until atlas UVs are wired (Phase 5).
            return v;
        }

        @Override
        public void bindTexture() {
            // STUB(R.Chen): OpenGL — texture binding handled by the Fabric render pipeline in Phase 5.
        }
    }
}
