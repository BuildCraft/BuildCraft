/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.fluid;

import buildcraft.api.core.BCLog;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.client.textures.ForgeTextureMetadata;
import net.minecraftforge.client.textures.ITextureAtlasSpriteLoader;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class SpriteFluidFrozen extends TextureAtlasSprite {
    /** The source sprite of this fluid. */
//    public final ResourceLocation srcLocation;
    public final ResourceLocation srcRegistryName;
//    private int[][] data = null;

    public SpriteFluidFrozen(ResourceLocation atlasLocation, SpriteContents spriteContents, int atlasWidth, int atlasHeight, int x, int y, ResourceLocation srcRegistryName) {
        super(atlasLocation, spriteContents, atlasWidth, atlasHeight, x, y);
        this.srcRegistryName = srcRegistryName;
    }

//    @Override
//    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
//        return true;
//    }

//    @Override
//    public int getFrameCount() {
//        return data == null ? 0 : 1;
//    }

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

    public static SpriteContents createSpriteContents(TextureAtlasSprite src, ResourceLocation srcRegistryName, ResourceLocation frozenLocation) {
//        if (src.getFrameCount() > 0)
        int frameCount = src.contents().getFrameCount();
        if (frameCount > 0) {
//            int widthOld = src.getIconWidth();
            int widthOld = src.contents().width();
//            int heightOld = src.getIconHeight();
            int heightOld = src.contents().height();
            int width = widthOld * 2;
            int height = heightOld * 2;

//            int[][] srcData = src.getFrameTextureData(0);
            int[] srcDataMipmap0Frame0 = new int[widthOld * heightOld];
            NativeImage mipmap0 = src.contents().byMipLevel[0];
            for (int x = 0; x < widthOld; x++) {
                for (int y = 0; y < heightOld; y++) {
                    srcDataMipmap0Frame0[x * widthOld + y] = mipmap0.getPixelsRGBA()[x * widthOld + y];
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
            return new SpriteContents(frozenLocation, new FrameSize(width, height), nativeImage, AnimationMetadataSection.EMPTY, new ForgeTextureMetadata(new FrozenSpriteLoader(srcRegistryName)));
        } else {
            // Urm... idk
//            BCLog.logger.warn("[lib.fluid] Failed to create a frozen sprite of " + src.getIconName() + " as the source sprite didn't have any frames!");
            BCLog.logger.warn("[lib.fluid] Failed to create a frozen sprite of " + src.contents().name() + " as the source sprite didn't have any frames!");
            return new SpriteContents(frozenLocation, new FrameSize(0, 0), new NativeImage(0, 0, false), AnimationMetadataSection.EMPTY);
        }
    }

    private static class FrozenSpriteLoader implements ITextureAtlasSpriteLoader {
        private final ResourceLocation srcRegistryName;

        private FrozenSpriteLoader(ResourceLocation srcRegistryName) {
            this.srcRegistryName = srcRegistryName;
        }

        @Override
        public SpriteContents loadContents(ResourceLocation name, Resource resource, FrameSize frameSize, NativeImage image, AnimationMetadataSection animationMeta, ForgeTextureMetadata forgeMeta) {
            BCLog.logger.warn("[lib.fluid] Should not call this method of FrozenSpriteLoader!");
            return null;
        }

        @Override
        public @NotNull TextureAtlasSprite makeSprite(ResourceLocation atlasName, SpriteContents contents, int atlasWidth, int atlasHeight, int spriteX, int spriteY, int mipmapLevel) {
            return new SpriteFluidFrozen(atlasName, contents, atlasWidth, atlasHeight, spriteX, spriteY, this.srcRegistryName);
        }
    }
}
