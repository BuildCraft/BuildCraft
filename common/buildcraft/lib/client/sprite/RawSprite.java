package buildcraft.lib.client.sprite;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

/** Defines a sprite that is taken directly from the given resource location. */
public class RawSprite implements ISprite {
    public final ResourceLocation location;
    public final float uMin, vMin, width, height;

    public RawSprite(ResourceLocation location, int xMin, int yMin, int width, int height, float textureSize) {
        this.location = location;
        this.uMin = xMin / textureSize;
        this.vMin = yMin / textureSize;
        this.width = width / textureSize;
        this.height = height / textureSize;
    }

    public RawSprite(ResourceLocation location, float xMin, float yMin, float width, float height) {
        this.location = location;
        this.uMin = xMin;
        this.vMin = yMin;
        this.width = width;
        this.height = height;
    }

    @Override
    public void bindTexture() {
        Minecraft.getMinecraft().renderEngine.bindTexture(location);
    }

    @Override
    public double getInterpU(double u) {
        return uMin + u * width;
    }

    @Override
    public double getInterpV(double v) {
        return vMin + v * height;
    }
}
