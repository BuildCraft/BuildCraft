package buildcraft.lib.client.sprite;

import buildcraft.api.core.render.ISprite;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.misc.SpriteUtil;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/** Defines a sprite that is taken directly from the given resource location. */
public class SpriteChanging implements ISprite {
    public final Supplier<String> location;
    public final INodeDouble uMin, vMin, width, height;

    public SpriteChanging(Supplier<String> location, INodeDouble xMin, INodeDouble yMin, INodeDouble width, INodeDouble height) {
        this.location = location;
        this.uMin = xMin;
        this.vMin = yMin;
        this.width = width;
        this.height = height;
    }

    @Override
    public void bindTexture() {
//        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(location.get()));
        SpriteUtil.bindTexture(new ResourceLocation(location.get()));
    }

    @Override
    public double getInterpU(double u) {
        return uMin.evaluate() + u * width.evaluate();
    }

    @Override
    public double getInterpV(double v) {
        return vMin.evaluate() + v * height.evaluate();
    }
}
