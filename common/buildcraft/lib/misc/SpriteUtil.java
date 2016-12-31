package buildcraft.lib.misc;

import java.util.HashMap;
import java.util.Map;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.ResourceLocation;

import buildcraft.lib.client.sprite.ISprite;
import buildcraft.lib.client.sprite.LibSprites;
import buildcraft.lib.client.sprite.RawSprite;

public class SpriteUtil {

    private static final Map<GameProfile, GameProfile> CACHED = new HashMap<>();

    public static void bindBlockTextureMap() {
        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
    }

    public static void bindTexture(String identifier) {
        bindTexture(new ResourceLocation(identifier));
    }

    public static void bindTexture(ResourceLocation identifier) {
        Minecraft.getMinecraft().renderEngine.bindTexture(identifier);
    }

    public static ISprite getFaceSprite(GameProfile profile) {
        if (profile == null) {
            return LibSprites.HELP;
        }
        Minecraft mc = Minecraft.getMinecraft();

        if (CACHED.containsKey(profile) && CACHED.get(profile) == null && Math.random() >= 0.99) {
            CACHED.remove(profile);
        }

        if (!CACHED.containsKey(profile)) {
            CACHED.put(profile, TileEntitySkull.updateGameprofile(profile));
        }
        GameProfile p2 = CACHED.get(profile);
        if (p2 == null) {
            return LibSprites.LOCK;
        }
        profile = p2;
        Map<Type, MinecraftProfileTexture> map = mc.getSkinManager().loadSkinFromCache(profile);
        MinecraftProfileTexture tex = map.get(Type.SKIN);
        if (tex != null) {
            ResourceLocation loc = mc.getSkinManager().loadSkin(tex, Type.SKIN);
            return new RawSprite(loc, 8, 8, 8, 8, 64);
        }
        return LibSprites.LOADING;
    }

    public static TextureAtlasSprite missingSprite() {
        return Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
    }
}
