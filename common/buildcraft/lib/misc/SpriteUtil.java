package buildcraft.lib.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

public class SpriteUtil {
    public static void bindBlockTextureMap() {
        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
    }

    public static void bindTexture(String identifier) {
        bindTexture(new ResourceLocation(identifier));
    }

    public static void bindTexture(ResourceLocation identifier) {
        Minecraft.getMinecraft().renderEngine.bindTexture(identifier);
    }
}
