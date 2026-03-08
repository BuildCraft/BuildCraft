/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.fluid;

import buildcraft.api.core.BCLog;
import buildcraft.lib.misc.SpriteUtil;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.data.AnimationFrame;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

// TODO Calen
public class SpriteFluidFrozen extends TextureAtlasSprite {
    /** The source sprite of this fluid. */
    public final ResourceLocation srcLocation;
//    private int[][] data = null;

    private NativeImage data = new NativeImage(16, 16, false);

    public SpriteFluidFrozen(ResourceLocation srcLocation) throws IOException {
        super(
                null,
                new TextureAtlasSprite.Info(
                        /*pName*/new ResourceLocation("buildcraftlib:fluid_" + srcLocation.toString().replace(':', '_') + "_convert_frozen"),
                        /*pWidth*/16, /*pHeight*/16,
                        /*pMetadata*/new AnimationMetadataSection(
                        /*pFrames*/ImmutableList.of(
                        new AnimationFrame(/*pIndex*/0, /*pTime*/-1)
                ),
                        /*pFrameWidth*/16, /*pFrameHeight*/16, /*pDefaultFrameTime*/1, /*pInterpolatedFrames*/false)
                ),
                0, 0, 0, 0, 0,
                NativeImage.read(Minecraft.getInstance().getResourceManager().getResource(srcLocation).getInputStream())
        );
//        super(new AtlasTexture(srcLocation), TextureAtlasSprite.Info p_118359_, int p_118360_, int p_118361_, int p_118362_, int p_118363_, int p_118364_, NativeImage p_118365_);
        // TODO Calen
//        super("buildcraftlib:fluid_" + srcLocation.toString().replace(':', '_') + "_convert_frozen");
        this.srcLocation = srcLocation;

//    }
////    @Override
////    public boolean load(IResourceManager manager, ResourceLocation location, Function<ResourceLocation, TextureAtlasSprite> textureGetter)
//    public boolean load(IResourceManager manager, ResourceLocation location, Function<ResourceLocation, TextureAtlasSprite> textureGetter)
//    {
        IResourceManager manager = Minecraft.getInstance().getResourceManager(); // Calen test
        ResourceLocation location = SpriteUtil.transformLocation(this.srcLocation);
//        TextureAtlasSprite src = Minecraft.getInstance().getTextureMapBlocks().getTextureExtry(srcLocation.toString());
        Texture src_ = Minecraft.getInstance().getTextureManager().getTexture(srcLocation);
        TextureAtlasSprite src = Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(location);
        if (src == null)
//        if (!(src_ instanceof AtlasTexture ta &&ta.getSprite(location)))
        {
            BCLog.logger.warn("[lib.fluid] Failed to create a frozen sprite of " + srcLocation.toString() + " as the source sprite wasn't able to be loaded!");
//            return true;
            return;
        }
//        AtlasTexture src = (AtlasTexture) src_;
        if (src.getFrameCount() <= 0) {
//            if (src.hasCustomLoader(manager, location))
            if (false) {
//                src.load(manager, location, textureGetter);
            } else {
//                try
//                {
////                    PngSizeInfo size = PngSizeInfo.makeFromResource(manager.getResource(location));
//                    PngSizeInfo size = new  PngInfo(manager.getResource(location));
////                    try (IResource resource = manager.getResource(location))
//                    try (Resource resource = manager.getResource(location))
//                    {
////                        boolean hasAnimation = resource.getMetadata("animation") != null;
//                        boolean hasAnimation = resource.getMetadata(TextureMetadataSection.SERIALIZER) != null;
//                        if (!hasAnimation && size.pngHeight != size.pngWidth)
//                        {
//                            BCLog.logger.warn(
//                                    "[lib.fluid] Failed to create a frozen sprite of " + srcLocation.toString()
//                                            + " as the source sprite wasnn't an animation and had a different width ("
//                                            + size.pngWidth + ") from height (" + size.pngWidth + ")!"
//                            );
////                            return true;
//                            return;
//                        }
////                        src.loadSprite(size, hasAnimation);
//                        src.atlas().load(manager);
////                        src.loadSpriteFrames(resource, Minecraft.getInstance().gameSettings.mipmapLevels + 1);
//                        src.atlas().prepareToStitch(manager, Stream.<ResourceLocation>builder().add(location).build(), Minecraft.getInstance().getProfiler(), Minecraft.getInstance().options.mipmapLevels + 1);
//                    }
//                }
//                catch (IOException io)
//                {
//                    io.printStackTrace();
//                }
            }
        }

//        if (src.getFrameCount() > 0)
//        {
//            int widthOld = src.getWidth();
//            int heightOld = src.getHeight();
//            super.width = widthOld * 2;
//            super.height = heightOld * 2;
//
////            int[][] srcData = src.getFrameTextureData(0);
//            int[][] srcData = src.getUniqueFrames().;
//
////            data = new int[Minecraft.getInstance().gameSettings.mipmapLevels + 1][];
//            data = new int[Minecraft.getInstance().options.mipmapLevels + 1][];
//            for (int m = 0; m < data.length; m++)
//            {
//                data[m] = new int[super.width * super.height / (m + 1) / (m + 1)];
//            }
//            int[] relData = srcData[0];
//            if (relData.length < (super.width * super.height / 4))
//            {
//                Arrays.fill(data[0], 0xFF_FF_FF_00);
//                for (int x = 0; x < data.getWidth(); x++)
//                {
//                    for (int y = 0; y < data.getHeight(); y++)
//                    {
//                        data.setPixelRGBA(x, y, 0xFF_FF_FF_00);
//                    }
//                }
//            }
//            else
//            {
//                for (int x = 0; x < super.width; x++)
//                {
//                    int fx = (x % widthOld) * heightOld;
//                    for (int y = 0; y < super.height; y++)
//                    {
//                        int fy = y % heightOld;
////                        data[0][x * super.height + y] = relData[fx + fy];
//                        data.setPixelRGBA(0, x * super.height + y, relData[fx + fy]);
//                    }
//                }
//            }
//        }
//        else
//        {
//            // Urm... idk
//            BCLog.logger.warn("[lib.fluid] Failed to create a frozen sprite of " + src.getName() + " as the source sprite didn't have any frames!");
////            return true;
//            return;
//        }
//        return false;
        return;
    }

    // TODO Calen
//    @Override
//    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location)
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
        return true;
    }

    // TODO Calen

    // TODO Calen
//    @Override
    public int getFrameCount() {
        return data == null ? 0 : 1;
    }


    // TODO Calen
//    @Override
//    public int[][] getFrameTextureData(int index)
//    {
//        return data;
//    }

//    @Override
//    public void uploadFirstFrame()
//    {
//        this.upload(0, 0, this.mainImage);
//    }

//    void upload(int pXOffset, int pYOffset, NativeImage[] pFrames)
//    {
//        for (int i = 0; i < this.mainImage.length; ++i)
//        {
//            if ((this.width >> i <= 0) || (this.height >> i <= 0)) break;
////            pFrames[i].upload(i, this.x >> i, this.y >> i, pXOffset >> i, pYOffset >> i, this.width >> i, this.height >> i, this.mainImage.length > 1, false);
//            data.upload(i, this.x >> i, this.y >> i, pXOffset >> i, pYOffset >> i, this.width >> i, this.height >> i, this.mainImage.length > 1, false);
//        }
//
//    }

    @Override
//    public float getInterpolatedU(double u)
    public float getU(double u) {
//        return super.getInterpolatedU(u / 2 + 4);
        return super.getU(u / 2 + 4);
    }

    // TODO Calen
    @Override
//    public float getInterpolatedV(double v)
    public float getV(double v) {
//        return super.getInterpolatedV(v / 2 + 4);
        return super.getV(v / 2 + 4);
    }
}
