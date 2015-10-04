package buildcraft.core.client;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

public enum CoreIconProvider {
    ENERGY("");

    private final ResourceLocation location;
    private TextureAtlasSprite sprite;

    CoreIconProvider(String location) {
        this.location = new ResourceLocation(location);
    }

    public static void registerIcons(TextureMap map) {
        for (CoreIconProvider icon : values()) {
            icon.registerSprite(map);
        }
    }

    private void registerSprite(TextureMap map) {
        sprite = map.registerSprite(location);
    }

    public TextureAtlasSprite getSprite() {
        return sprite;
    }
}
