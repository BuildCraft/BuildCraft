package buildcraft.lib.client.sprite;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

/** Defines a sprite that is taken directly from the given resource location. */
public class RawSprite implements ISprite {
    public final ResourceLocation location;
    public final float uMin, vMin, uMax, vMax;

    public RawSprite(ResourceLocation location, int xMin, int yMin, int xMax, int yMax, float textureSize) {
        this.location = location;
        this.uMin = xMin / textureSize;
        this.vMin = yMin / textureSize;
        this.uMax = xMax / textureSize;
        this.vMax = yMax / textureSize;
    }

    public RawSprite(ResourceLocation location, float xMin, float yMin, float xMax, float yMax) {
        this.location = location;
        this.uMin = xMin;
        this.vMin = yMin;
        this.uMax = xMax;
        this.vMax = yMax;
    }

    @Override
    public void bindTexture() {
        Minecraft.getMinecraft().renderEngine.bindTexture(location);
    }

    @Override
    public double getInterpU(double u) {
        return uMin * (1 - u) + uMax * u;
    }

    @Override
    public double getInterpV(double v) {
        return vMin * (1 - v) + vMax * v;
    }
}
