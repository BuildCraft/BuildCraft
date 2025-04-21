/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.api.core.render.ISprite;
import buildcraft.lib.BCLibSprites;
import buildcraft.lib.client.sprite.SpriteRaw;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.util.LazyValue;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class SpriteUtil {

    // private static final ResourceLocation LOCATION_SKIN_LOADING = new ResourceLocation("skin:loading");
    private static final ResourceLocation LOCATION_SKIN_LOADING = new ResourceLocation("textures/entity/steve.png");
    private static final Map<GameProfile, GameProfile> CACHED = new HashMap<>();

    public static void bindBlockTextureMap() {
//        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        bindTexture(AtlasTexture.LOCATION_BLOCKS);
    }

    public static void bindTexture(String identifier) {
        bindTexture(new ResourceLocation(identifier));
    }

    public static void bindTexture(int identifier) {
        GlStateManager._bindTexture(identifier);
    }

    public static void bindTexture(ResourceLocation identifier) {
//        Minecraft.getInstance().textureManager.bindForSetup(identifier);
        Minecraft.getInstance().textureManager.bind(identifier);
    }

    /** Transforms the given {@link ResourceLocation}, adding ".png" to the end and prepending that
     * {@link ResourceLocation#getPath()} with "textures/", just like what {@link AtlasTexture} does. */
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
                CACHED.put(profile, SkullTileEntity.updateGameprofile(profile));
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

    private static LazyValue<TextureAtlasSprite> MISSING_NO = new LazyValue<>(() ->
            Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(MissingTextureSprite.getLocation()));

    public static TextureAtlasSprite missingSprite() {
        return MISSING_NO.get();
    }

    // Calen
    private static LazyValue<TextureAtlasSprite> WHITE = new LazyValue<>(ModelLoader.White::instance);

    public static TextureAtlasSprite white() {
        return WHITE.get();
    }
}
