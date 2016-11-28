package buildcraft.lib.client.render.fluid;

import java.io.IOException;
import java.util.Arrays;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.PngSizeInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.BCLog;

public class SpriteFluidFrozen extends TextureAtlasSprite {
    /** The source sprite of this fluid. */
    public final TextureAtlasSprite src;
    private int[][] data = null;

    public SpriteFluidFrozen(TextureAtlasSprite src) {
        super("buildcraftlib:fluid_" + src.getIconName().replace(':', '_') + "_convert_frozen");
        this.src = src;
    }

    private static ResourceLocation getResourceLocation(TextureAtlasSprite src) {
        ResourceLocation resourcelocation = new ResourceLocation(src.getIconName());
        return new ResourceLocation(resourcelocation.getResourceDomain(), String.format("%s/%s%s", "textures", resourcelocation.getResourcePath(), ".png"));
    }

    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
        return true;
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation location) {
        if (src.getFrameCount() <= 0) {
            location = getResourceLocation(src);
            if (src.hasCustomLoader(manager, location)) {
                src.load(manager, location);
            } else {
                try {
                    PngSizeInfo pngsizeinfo = PngSizeInfo.makeFromResource(manager.getResource(location));
                    try (IResource resource = manager.getResource(location)) {
                        src.loadSprite(pngsizeinfo, resource.getMetadata("animation") != null);
                        src.loadSpriteFrames(resource, Minecraft.getMinecraft().gameSettings.mipmapLevels + 1);
                    }
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }
        }

        if (src.getFrameCount() > 0) {
            int widthOld = src.getIconWidth();
            int heightOld = src.getIconHeight();
            width = widthOld * 4;
            height = heightOld * 4;

            int[][] srcData = src.getFrameTextureData(0);
            int[] relData = srcData[0];

            data = new int[Minecraft.getMinecraft().gameSettings.mipmapLevels + 1][width * height];
            Arrays.fill(data[0], 0xFF_AA_00_FF);

            for (int x = 0; x < width; x++) {
                int fx = ((x + widthOld / 4) % widthOld) * heightOld;
                for (int y = 0; y < height; y++) {
                    int fy = (y + heightOld / 4) % heightOld;
                    data[0][x * height + y] = relData[fx + fy];
                }
            }
        } else {
            // Urm... idk
            BCLog.logger.warn("[lib.fluid] Failed to create a frozen sprite of " + src.getIconName() + " as the source sprite didn't have any frames!");
            return true;
        }
        return false;
    }

    @Override
    public int getFrameCount() {
        return data == null ? 0 : 1;
    }

    @Override
    public int[][] getFrameTextureData(int index) {
        return data;
    }

    @Override
    public float getInterpolatedU(double u) {
        return super.getInterpolatedU(u / 4 + 8);
    }

    @Override
    public float getInterpolatedV(double v) {
        return super.getInterpolatedV(v / 4 + 8);
    }
}
