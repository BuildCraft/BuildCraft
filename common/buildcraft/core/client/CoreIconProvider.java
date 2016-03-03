package buildcraft.core.client;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

public enum CoreIconProvider {
    ENERGY("buildcraftcore:items/icons/energy"),
    SLOT("buildcraftcore:gui/slot"),
    LOCK("buildcraftcore:items/icons/lock"),
    TUNED_OFF("buildcraftcore:triggers/action_machinecontrol_off");

    private final ResourceLocation location;
    private TextureAtlasSprite sprite;

    CoreIconProvider(String location) {
        this.location = new ResourceLocation(location);
    }

    public static void registerSprites(TextureMap map) {
        for (CoreIconProvider icon : values()) {
            icon.registerSprite(map);
        }
    }

    private void registerSprite(TextureMap map) {
        sprite = null;
        sprite = map.getTextureExtry(location.toString());
        if (sprite == null) sprite = map.registerSprite(location);
    }

    public TextureAtlasSprite getSprite() {
        return sprite;
    }
}
