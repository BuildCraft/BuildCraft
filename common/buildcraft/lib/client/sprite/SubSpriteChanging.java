package buildcraft.lib.client.sprite;

import buildcraft.api.core.render.ISprite;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.gui.pos.IGuiArea;

public class SubSpriteChanging implements ISprite {
    private final ISprite delegate;
    private final INodeDouble uMin, vMin, uMax, vMax;

    public SubSpriteChanging(ISprite delegate, INodeDouble uMin, INodeDouble vMin, INodeDouble uMax, INodeDouble vMax) {
        this.delegate = delegate;
        this.uMin = uMin;
        this.vMin = vMin;
        this.uMax = uMax;
        this.vMax = vMax;
    }

    public SubSpriteChanging(ISprite delegate, IGuiArea area) {
        this(delegate, area::getX, area::getY, area::getEndX, area::getEndY);
    }

    @Override
    public void bindTexture() {
        delegate.bindTexture();
    }

    @Override
    public double getInterpU(double u) {
        double iu = uMin.evaluate() * (1 - u) + uMax.evaluate() * u;
        return delegate.getInterpU(iu);
    }

    @Override
    public double getInterpV(double v) {
        double iv = vMin.evaluate() * (1 - v) + vMax.evaluate() * v;
        return delegate.getInterpV(iv);
    }
}
