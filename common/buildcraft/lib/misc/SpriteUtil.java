/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.block.entity.BlockEntitySkull;
import net.minecraft.util.Identifier;

import buildcraft.api.core.render.ISprite;

import buildcraft.lib.BCLibSprites;
import buildcraft.lib.client.sprite.SpriteRaw;

public class SpriteUtil {

    private static final Identifier LOCATION_SKIN_LOADING = new Identifier("skin:loading");
    private static final Map<GameProfile, GameProfile> CACHED = new HashMap<>();

    public static void bindBlockTextureMap() {
        bindTexture(net.minecraft.screen.PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
    }

    public static void bindTexture(String identifier) {
        bindTexture(new Identifier(identifier));
    }

    public static void bindTexture(Identifier identifier) {
        com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, identifier);
    }

    /** Transforms the given {@link Identifier}, adding ".png" to the end and prepending that
     * {@link Identifier#getResourcePath()} with "textures/", just like what {@link TextureMap} does. */
    public static Identifier transformLocation(Identifier location) {
        return new Identifier(location.getNamespace(), "textures/" + location.getPath() + ".png");
    }

    @Nullable
    public static Identifier getSkinSpriteLocation(GameProfile profile) {
        Identifier loc = getSkinSpriteLocation0(profile);
        return loc == LOCATION_SKIN_LOADING ? null : loc;
    }

    @Nullable
    private static Identifier getSkinSpriteLocation0(GameProfile profile) {
        if (profile == null) {
            return null;
        }
        MinecraftClient mc = MinecraftClient.getInstance();

        if (CACHED.containsKey(profile) && CACHED.get(profile) == null && Math.random() >= 0.99) {
            CACHED.remove(profile);
        }

        try {
            if (!CACHED.containsKey(profile)) {
                CACHED.put(profile, TileEntitySkull.updateGameprofile(profile));
            }
            GameProfile p2 = CACHED.get(profile);
            if (p2 == null) {
                return null;
            }
            profile = p2;
            Map<Type, MinecraftProfileTexture> map = mc.getSkinManager().loadSkinFromCache(profile);
            MinecraftProfileTexture tex = map.get(Type.SKIN);
            if (tex != null) {
                return mc.getSkinManager().loadSkin(tex, Type.SKIN);
            }
            return LOCATION_SKIN_LOADING;
        } catch (NullPointerException | ClassCastException e) {
            // Fix for https://github.com/BuildCraft/BuildCraft/issues/4419
            // I'm not quite sure why this throws an NPE but this should at
            // least stop it from crashing
            e.printStackTrace();
            CACHED.put(profile, profile);
            return null;
        }
    }

    public static ISprite getFaceSprite(GameProfile profile) {
        if (profile == null) {
            return BCLibSprites.HELP;
        }
        Identifier loc = getSkinSpriteLocation0(profile);
        if (loc == null) {
            return BCLibSprites.LOCK;
        }
        return new SpriteRaw(loc, 8, 8, 8, 8, 64);
    }

    @Nullable
    public static ISprite getFaceOverlaySprite(GameProfile profile) {
        if (profile == null) {
            return null;
        }
        Identifier loc = getSkinSpriteLocation0(profile);
        if (loc == null) {
            return null;
        }
        return new SpriteRaw(loc, 40, 8, 8, 8, 64);
    }

    public static Sprite missingSprite() {
        return MinecraftClient.getInstance().getTextureMapBlocks().getMissingSprite();
    }
}
