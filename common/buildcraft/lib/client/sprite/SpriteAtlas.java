package buildcraft.lib.client.sprite;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import buildcraft.api.core.render.ISprite;

import buildcraft.lib.misc.SpriteUtil;

public class SpriteAtlas implements ISprite {
    public final TextureAtlasSprite sprite;

    public SpriteAtlas(TextureAtlasSprite sprite) {
        this.sprite = sprite;
    }

    @Override
    public void bindTexture() {
        SpriteUtil.bindBlockTextureMap();
    }

    @Override
    public double getInterpU(double u) {
        return sprite.getInterpolatedU(u * 16);
    }

    @Override
    public double getInterpV(double v) {
        return sprite.getInterpolatedV(v * 16);
    }
}
