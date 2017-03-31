package buildcraft.lib.client.sprite;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** A {@link TextureAtlasSprite} but with a public constructor. */
public class AtlasSpriteDirect extends TextureAtlasSprite {
    public AtlasSpriteDirect(String spriteName) {
        super(spriteName);
    }
}
