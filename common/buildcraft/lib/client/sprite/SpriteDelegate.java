package buildcraft.lib.client.sprite;

public abstract class SpriteDelegate implements ISprite {
    @Override
    public void bindTexture() {
        getDelegate().bindTexture();
    }

    @Override
    public double getInterpU(double u) {
        return getDelegate().getInterpU(u);
    }

    @Override
    public double getInterpV(double v) {
        return getDelegate().getInterpV(v);
    }

    public abstract ISprite getDelegate();
}
