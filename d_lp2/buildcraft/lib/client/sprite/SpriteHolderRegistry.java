package buildcraft.lib.client.sprite;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.BCLog;

public class SpriteHolderRegistry {
    private static final Map<ResourceLocation, SpriteHolder> HOLDER_MAP = new HashMap<>();

    public static SpriteHolder getHolder(ResourceLocation location) {
        if (!HOLDER_MAP.containsKey(location)) {
            HOLDER_MAP.put(location, new SpriteHolder(location));
        }
        return HOLDER_MAP.get(location);
    }

    public static SpriteHolder getHolder(String location) {
        return getHolder(new ResourceLocation(location));
    }

    public static void onTextureStitchPre(TextureMap map) {
        for (SpriteHolder holder : HOLDER_MAP.values()) {
            holder.onTextureStitchPre(map);
            BCLog.logger.info("Stitching " + holder.spriteLocation);
        }
    }

    public static class SpriteHolder {
        public final ResourceLocation spriteLocation;
        private TextureAtlasSprite sprite;

        private SpriteHolder(ResourceLocation spriteLocation) {
            this.spriteLocation = spriteLocation;
        }

        public void onTextureStitchPre(TextureMap map) {
            sprite = map.registerSprite(spriteLocation);
        }

        public TextureAtlasSprite getSprite() {
            return sprite;
        }
    }
}
