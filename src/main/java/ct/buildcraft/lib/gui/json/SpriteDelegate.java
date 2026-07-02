package ct.buildcraft.lib.gui.json;

import ct.buildcraft.lib.client.sprite.SpriteAtlas;
import ct.buildcraft.lib.misc.SpriteUtil;
import ct.buildcraft.api.core.render.ISprite;

public class SpriteDelegate implements ISprite {
    public ISprite delegate;

    public SpriteDelegate(ISprite delegate) {
        this.delegate = delegate;
    }

    public SpriteDelegate() {
        this(new SpriteAtlas(SpriteUtil.missingSprite()));
    }

    @Override
    public void bindTexture() {
        if (delegate != null) {
            delegate.bindTexture();
        }
    }

    @Override
    public float getInterpU(double u) {
        return delegate == null ? 0 : delegate.getInterpU(u);
    }

    @Override
    public float getInterpV(double v) {
        return delegate == null ? 0 : delegate.getInterpV(v);
    }
}
