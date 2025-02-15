/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.api.core.render.ISprite;
import buildcraft.lib.BCLibSprites;
import buildcraft.lib.client.sprite.SpriteRaw;
import buildcraft.lib.client.sprite.White;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class SpriteUtil {

    // private static final ResourceLocation LOCATION_SKIN_LOADING = new ResourceLocation("skin:loading");
    private static final ResourceLocation LOCATION_SKIN_LOADING = new ResourceLocation("textures/entity/player/wide/steve.png");
    private static final Map<GameProfile, GameProfile> CACHED = new HashMap<>();

    public static void bindBlockTextureMap() {
//        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        bindTexture(TextureAtlas.LOCATION_BLOCKS);
    }

    public static void bindTexture(String identifier) {
        bindTexture(new ResourceLocation(identifier));
    }

    public static void bindTexture(int identifier) {
        RenderSystem.setShaderTexture(0, identifier);
    }

    public static void bindTexture(ResourceLocation identifier) {
//        Minecraft.getInstance().textureManager.bindForSetup(identifier);
        RenderSystem.setShaderTexture(0, identifier);
    }

    /** Transforms the given {@link ResourceLocation}, adding ".png" to the end and prepending that
     * {@link ResourceLocation#getPath()} with "textures/", just like what {@link TextureAtlas} does. */
    public static ResourceLocation transformLocation(ResourceLocation location) {
        return new ResourceLocation(location.getNamespace(), "textures/" + location.getPath() + ".png");
    }

    @Nullable
    public static ResourceLocation getSkinSpriteLocation(GameProfile profile) {
        ResourceLocation loc = getSkinSpriteLocation0(profile);
        return loc == LOCATION_SKIN_LOADING ? null : loc;
    }

    @Nullable
    private static ResourceLocation getSkinSpriteLocation0(GameProfile profile) {
        if (profile == null) {
            return null;
        }
        Minecraft mc = Minecraft.getInstance();

        if (CACHED.containsKey(profile) && CACHED.get(profile) == null && Math.random() >= 0.99) {
            CACHED.remove(profile);
        }

        try {
            if (!CACHED.containsKey(profile)) {
//                CACHED.put(profile, TileEntitySkull.updateGameprofile(profile));
                SkullBlockEntity.updateGameprofile(profile, (gameProfile) ->
                {
                });
                CACHED.put(profile, profile);
            }
            GameProfile p2 = CACHED.get(profile);
            if (p2 == null) {
                return null;
            }
            profile = p2;
            Map<Type, MinecraftProfileTexture> map = mc.getSkinManager().getInsecureSkinInformation(profile);
            MinecraftProfileTexture tex = map.get(Type.SKIN);
            if (tex != null) {
                return mc.getSkinManager().registerTexture(tex, Type.SKIN);
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
        ResourceLocation loc = getSkinSpriteLocation0(profile);
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
        ResourceLocation loc = getSkinSpriteLocation0(profile);
        if (loc == null) {
            return null;
        }
        return new SpriteRaw(loc, 40, 8, 8, 8, 64);
    }

    private static LazyLoadedValue<TextureAtlasSprite> MISSING_NO = new LazyLoadedValue<>(() ->
            Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(MissingTextureAtlasSprite.getLocation()));

    public static LazyLoadedValue<TextureAtlasSprite> missingSprite() {
        return MISSING_NO;
    }

    // Calen
    private static LazyLoadedValue<TextureAtlasSprite> WHITE = new LazyLoadedValue<>(White::instance);

    public static TextureAtlasSprite white() {
        return WHITE.get();
    }
}
