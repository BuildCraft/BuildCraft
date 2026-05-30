/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.client.sprite;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

import buildcraft.api.core.render.ISprite;

/**
 * Manages named sprite holders that lazily resolve to stitched atlas sprites.
 *
 * Sprites must appear in the block atlas — either by being referenced in a block/item model JSON or
 * by explicit listing in {@code assets/<namespace>/atlases/blocks.json}.  Once the atlas is
 * ready (after {@link MinecraftClient} resource reload), {@link SpriteHolder#getSprite()} resolves
 * via {@code MinecraftClient.getSpriteAtlas(BLOCK_ATLAS_TEXTURE).apply(id)}.
 */
@Environment(EnvType.CLIENT)
public class SpriteHolderRegistry {

    private static final Map<Identifier, SpriteHolder> holders = new HashMap<>();

    public static SpriteHolder getHolder(String location) {
        return getHolder(new Identifier(location));
    }

    public static SpriteHolder getHolder(Identifier location) {
        return holders.computeIfAbsent(location, SpriteHolder::new);
    }

    /** All registered holders — used by sprite registration callbacks. */
    public static Collection<SpriteHolder> allHolders() {
        return holders.values();
    }

    public static class SpriteHolder implements ISprite {
        public final Identifier spriteLocation;

        private SpriteHolder(Identifier spriteLocation) {
            this.spriteLocation = spriteLocation;
        }

        /**
         * Returns the stitched atlas sprite for this holder.  Returns {@code null} before the
         * block atlas has been stitched (e.g. during server-side init).  The sprite is resolved
         * lazily so atlas reloads are reflected automatically.
         */
        @Environment(EnvType.CLIENT)
        public Sprite getSprite() {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc == null) return null;
            return mc.getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(spriteLocation);
        }

        @Override
        public double getInterpU(double u) {
            Sprite s = getSprite();
            if (s == null) return u;
            return s.getMinU() + u * (s.getMaxU() - s.getMinU());
        }

        @Override
        public double getInterpV(double v) {
            Sprite s = getSprite();
            if (s == null) return v;
            return s.getMinV() + v * (s.getMaxV() - s.getMinV());
        }

        @Override
        public void bindTexture() {
            // Texture binding is handled by the Fabric render pipeline via RenderLayer.
        }
    }
}
