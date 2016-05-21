package buildcraft.core.client;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.tiles.IControllable.Mode;

public enum CoreIconProvider {
    ENERGY("buildcraftcore:items/icons/energy"),
    SLOT("buildcraftcore:gui/slot"),
    LOCK("buildcraftcore:items/icons/lock"),
    TURNED_OFF("buildcraftcore:triggers/action_machinecontrol_off"),
    LOOPING("buildcraftcore:triggers/action_machinecontrol_loop");

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

    public static CoreIconProvider getForControlMode(Mode mode) {
        if (mode == Mode.Off) return TURNED_OFF;
        if (mode == Mode.Loop) return LOOPING;
        return null;
    }
}
