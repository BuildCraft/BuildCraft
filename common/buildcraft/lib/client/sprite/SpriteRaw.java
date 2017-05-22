package buildcraft.lib.client.sprite;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

/** Defines a sprite that is taken directly from the given resource location. */
public class SpriteRaw implements ISprite {
    public final ResourceLocation location;
    public final double uMin, vMin, width, height;

    public SpriteRaw(ResourceLocation location, double xMin, double yMin, double width, double height, double textureSize) {
        this.location = location;
        this.uMin = xMin / textureSize;
        this.vMin = yMin / textureSize;
        this.width = width / textureSize;
        this.height = height / textureSize;
    }

    public SpriteRaw(ResourceLocation location, double xMin, double yMin, double width, double height) {
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
