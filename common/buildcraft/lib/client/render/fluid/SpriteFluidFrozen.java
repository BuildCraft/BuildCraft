/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.fluid;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.PngSizeInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.BCLog;

import buildcraft.lib.misc.SpriteUtil;

public class SpriteFluidFrozen extends TextureAtlasSprite {
    /** The source sprite of this fluid. */
    public final ResourceLocation srcLocation;
    private int[][] data = null;

    public SpriteFluidFrozen(ResourceLocation srcLocation) {
        super("buildcraftlib:fluid_" + srcLocation.toString().replace(':', '_') + "_convert_frozen");
        this.srcLocation = srcLocation;
    }

    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
        return true;
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation location, Function<ResourceLocation, TextureAtlasSprite> textureGetter) {
        location = SpriteUtil.transformLocation(srcLocation);
        TextureAtlasSprite src = Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(srcLocation.toString());
        if (src == null) {
            BCLog.logger.warn("[lib.fluid] Failed to create a frozen sprite of " + srcLocation.toString() + " as the source sprite wasn't able to be loaded!");
            return true;
        }

        if (src.getFrameCount() <= 0) {
            if (src.hasCustomLoader(manager, location)) {
                src.load(manager, location, textureGetter);
            } else {
                try {
                    PngSizeInfo size = PngSizeInfo.makeFromResource(manager.getResource(location));
                    try (IResource resource = manager.getResource(location)) {
                        boolean hasAnimation = resource.getMetadata("animation") != null;
                        if (!hasAnimation && size.pngHeight != size.pngWidth) {
                            BCLog.logger.warn(
                                "[lib.fluid] Failed to create a frozen sprite of " + srcLocation.toString()
                                    + " as the source sprite wasnn't an animation and had a different width ("
                                    + size.pngWidth + ") from height (" + size.pngWidth + ")!"
                            );
                            return true;
                        }
                        src.loadSprite(size, hasAnimation);
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
            width = widthOld * 2;
            height = heightOld * 2;

            int[][] srcData = src.getFrameTextureData(0);

            data = new int[Minecraft.getMinecraft().gameSettings.mipmapLevels + 1][];
            for (int m = 0; m < data.length; m++) {
                data[m] = new int[width * height / (m + 1) / (m + 1)];
            }
            int[] relData = srcData[0];
            if (relData.length < (width * height / 4)) {
                Arrays.fill(data[0], 0xFF_FF_FF_00);
            } else {
                for (int x = 0; x < width; x++) {
                    int fx = (x % widthOld) * heightOld;
                    for (int y = 0; y < height; y++) {
                        int fy = y % heightOld;
                        data[0][x * height + y] = relData[fx + fy];
                    }
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
        return super.getInterpolatedU(u / 2 + 4);
    }

    @Override
    public float getInterpolatedV(double v) {
        return super.getInterpolatedV(v / 2 + 4);
    }
}
