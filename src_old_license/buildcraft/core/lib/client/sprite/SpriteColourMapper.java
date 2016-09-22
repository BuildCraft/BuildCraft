package buildcraft.core.lib.client.sprite;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import com.google.common.base.Throwables;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.util.ResourceLocation;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.BCLog;
import buildcraft.core.lib.fluids.FluidDefinition.BCFluid;

import javax.imageio.ImageIO;

public class SpriteColourMapper extends TextureAtlasSprite {
    private final ResourceLocation from;
    private final int dark, light;

    public SpriteColourMapper(BCFluid bcFluid, String fluidTextureFrom, boolean still) {
        super(still ? bcFluid.getStill().toString() : bcFluid.getFlowing().toString());
        from = new ResourceLocation(fluidTextureFrom);
        this.dark = bcFluid.getDarkColour();
        this.light = bcFluid.getLightColour();
    }

    public SpriteColourMapper(String spriteName, ResourceLocation from, int dark, int light) {
        super(spriteName);
        this.from = from;
        this.dark = dark;
        this.light = light;
    }

    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
        return true;
    }

    private static int lerp(int[] values) {
        float t = values[0] / (float) 0xFF;
        int l = values[1];
        int u = values[2];

        float v = ((1 - t) * u + t * l);

        return ((int) v) & 0xFF;
    }

    private int average(int from) {
        int[] a = { (from >> 24) & 0xFF, (light >> 24) & 0xFF, (dark >> 24) & 0xFF };
        int[] b = { (from >> 16) & 0xFF, (light >> 16) & 0xFF, (dark >> 16) & 0xFF };
        int[] g = { (from >> 8) & 0xFF, (light >> 8) & 0xFF, (dark >> 8) & 0xFF };
        int[] r = { from & 0xFF, light & 0xFF, dark & 0xFF };
        return lerp(a) << 24 | lerp(b) << 16 | lerp(g) << 8 | lerp(r);
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation location) {
        TextureData data = loadFromManager(manager, from);
        BufferedImage[] images = new BufferedImage[1 + Minecraft.getMinecraft().gameSettings.mipmapLevels];
        images[0] = new BufferedImage(data.image.getWidth(), data.image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);

        for (int w = 0; w < images[0].getWidth(); w++) {
            for (int h = 0; h < images[0].getHeight(); h++) {
                int rgba = data.image.getRGB(w, h);
                images[0].setRGB(w, h, average(rgba));
            }
        }

        output(data.image, location.toString().replace("/", "__") + "_from");
        output(images[0], location.toString().replace("/", "__") + "_to");

        try {
            super.loadSprite(images, data.animationMeta);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
        return false;
    }

    private static void output(BufferedImage bufferedImage, String string) {
        if (BuildCraftCore.DEVELOPER_MODE) {
            File loc = new File("./bc-tex/");
            loc = new File(loc.getAbsolutePath());
            loc.mkdir();
            loc = new File(loc, string + ".png");
            try {
                ImageIO.write(bufferedImage, "png", loc);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static ResourceLocation completeResourceLocation(ResourceLocation location) {
        return new ResourceLocation(location.getResourceDomain(), "textures/" + location.getResourcePath() + ".png");
    }

    private static TextureData loadFromManager(IResourceManager manager, ResourceLocation location) {
        try {
            IResource iresource = manager.getResource(completeResourceLocation(location));
            TextureData data = new TextureData();
            data.image = TextureUtil.readBufferedImage(iresource.getInputStream());
            data.textureMeta = (TextureMetadataSection) iresource.getMetadata("texture");
            data.animationMeta = (AnimationMetadataSection) iresource.getMetadata("animation");
            return data;
        } catch (Throwable t) {
            BCLog.logger.warn("[SpriteColourMapper] Could not find " + location);
            return missingTex(16, 16);
        }
    }

    private static class TextureData {
        BufferedImage image;
        TextureMetadataSection textureMeta;
        AnimationMetadataSection animationMeta;
    }

    private static TextureData missingTex(int width, int height) {
        TextureData tex = new TextureData();
        tex.image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        int yellow = 0xFF_FF_FF_00;
        int black = 0xFF_00_00_00;
        int[][] c = { { yellow, black, -1 }, { black, yellow, -1 }, { -1, -1, -1 } };
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                tex.image.setRGB(w, h, c[w * 2 / width][h * 2 / height]);
            }
        }
        return tex;
    }
}
