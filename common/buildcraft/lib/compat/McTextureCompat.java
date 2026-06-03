/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.compat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

/** Compat wrapper: provides 1.12.2 TextureMap-style API via Fabric 1.20.1 SpriteAtlasTexture. */
public final class McTextureCompat {

    private McTextureCompat() {}

    /** Replaces MinecraftClient.getInstance().getTextureMapBlocks() */
    public static SpriteAtlasTexture getBlockAtlas() {
        return (SpriteAtlasTexture) MinecraftClient.getInstance()
                .getTextureManager()
                .getTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
    }

    /** Replaces TextureMap/SpriteAtlasTexture.getMissingSprite() */
    public static Sprite getMissingSprite() {
        return MinecraftClient.getInstance()
                .getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)
                .apply(MissingSprite.getMissingSpriteId());
    }

    /** Replaces TextureMap.getAtlasSprite(loc) */
    public static Sprite getSprite(Identifier loc) {
        return MinecraftClient.getInstance()
                .getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)
                .apply(loc);
    }
}
