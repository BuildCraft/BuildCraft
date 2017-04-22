package buildcraft.lib.client.sprite;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class SpriteAtlas implements ISprite {
    public final TextureAtlasSprite sprite;

    public SpriteAtlas(TextureAtlasSprite sprite) {
        this.sprite = sprite;
    }

    @Override
    public void bindTexture() {
        throw new IllegalStateException("You cannot bind these sprites!");
    }

    @Override
    public double getInterpU(double u) {
        return sprite.getInterpolatedU(u);
    }

    @Override
    public double getInterpV(double v) {
        return sprite.getInterpolatedV(v);
    }
}
