/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.fluid;

import buildcraft.api.core.BCLog;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;

public class SpriteFluidFrozen extends TextureAtlasSprite {
    /** The source sprite of this fluid. */
//    public final ResourceLocation srcLocation;
    public final ResourceLocation srcRegistryName;
//    private int[][] data = null;
    
    public SpriteFluidFrozen(TextureAtlas atlas, TextureAtlasSprite.Info info, int mipmapLevel, int atlasWidth, int atlasHeight, int x, int y, NativeImage nativeImage, ResourceLocation srcRegistryName) {
        super(atlas, info, mipmapLevel, atlasWidth, atlasHeight, x, y, nativeImage);
        this.srcRegistryName = srcRegistryName;
    }

//    @Override
//    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
//        return true;
//    }

    @Override
    public int getFrameCount() {
//        return data == null ? 0 : 1;
        return 1;
    }

//    @Override
//    public int[][] getFrameTextureData(int index) {
//        return data;
//    }

    @Override
//    public float getInterpolatedU(double u)
    public float getU(double u) {
//        return super.getInterpolatedU(u / 2 + 4);
        return super.getU(u / 2 + 4);
    }

    @Override
//    public float getInterpolatedV(double v)
    public float getV(double v) {
//        return super.getInterpolatedV(v / 2 + 4);
        return super.getV(v / 2 + 4);
    }

    public static NativeImage createSpriteContents(TextureAtlasSprite src) {
//        if (src.getFrameCount() > 0)
        int frameCount = src.getFrameCount();
        if (frameCount > 0) {
//            int widthOld = src.getIconWidth();
            int widthOld = src.getWidth();
//            int heightOld = src.getIconHeight();
            int heightOld = src.getHeight();
            int width = widthOld * 2;
            int height = heightOld * 2;

//            int[][] srcData = src.getFrameTextureData(0);
            int[] srcDataMipmap0Frame0 = new int[widthOld * heightOld];
            for (int x = 0; x < widthOld; x++) {
                for (int y = 0; y < heightOld; y++) {
                    srcDataMipmap0Frame0[x * widthOld + y] = src.getPixelRGBA(0, x, y);
                }
            }

//            data = new int[Minecraft.getMinecraft().gameSettings.mipmapLevels + 1][];
            int[] data1Frame = new int[width * height];
//            for (int m = 0; m < data.length; m++) {
//                data[m] = new int[width * height / (m + 1) / (m + 1)];
//            }
//            int[] relData = srcData[0];
            int[] relData = srcDataMipmap0Frame0;
            if (relData.length < (width * height / 4)) {
//                Arrays.fill(data[0], 0xFF_FF_FF_00);
                Arrays.fill(data1Frame, 0xFF_FF_FF_00);
            } else {
                for (int x = 0; x < width; x++) {
                    int fx = (x % widthOld) * heightOld;
                    for (int y = 0; y < height; y++) {
                        int fy = y % heightOld;
//                        data[0][x * height + y] = relData[fx + fy];
                        data1Frame[x * height + y] = relData[fx + fy];
                    }
                }
            }
            NativeImage nativeImage = new NativeImage(width, height, false);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    nativeImage.setPixelRGBA(x, y, data1Frame[x * width + y]);
                }
            }
            return nativeImage;
        } else {
            // Urm... idk
//            BCLog.logger.warn("[lib.fluid] Failed to create a frozen sprite of " + src.getIconName() + " as the source sprite didn't have any frames!");
            BCLog.logger.warn("[lib.fluid] Failed to create a frozen sprite of " + src.getName() + " as the source sprite didn't have any frames!");
            return new NativeImage(0, 0, false);
        }
    }
}
