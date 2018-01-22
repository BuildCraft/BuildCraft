/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.client.sprite;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.BCLog;

import buildcraft.lib.client.sprite.AtlasSpriteDirect;
import buildcraft.lib.client.sprite.AtlasSpriteSwappable;
import buildcraft.lib.fluid.BCFluid;
import buildcraft.lib.misc.SpriteUtil;

public class AtlasSpriteFluid extends AtlasSpriteSwappable {
    final ResourceLocation fromName;
    final BCFluid fluid;
    final int colourLight, colourDark;

    public AtlasSpriteFluid(String baseName, ResourceLocation fromName, BCFluid fluid) {
        super(baseName);
        this.fromName = fromName;
        this.fluid = fluid;
        colourLight = fluid.getLightColour();
        colourDark = fluid.getDarkColour();
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation location) {
        ResourceLocation from = SpriteUtil.transformLocation(fromName);
        AtlasSpriteDirect sprite = loadSprite(manager, from.toString(), from, true);
        if (sprite == null) {
            BCLog.logger.warn("Unable to recolour " + from + " as it couldn't be loaded!");
            return true;
        }
        for (int f = 0; f < sprite.getFrameCount(); f++) {
            recolourFrame(sprite, f);
        }
        swapWith(sprite);
        return false;
    }

    private void recolourFrame(TextureAtlasSprite sprite, int f) {
        int[][] frameData = sprite.getFrameTextureData(f);
        if (frameData != null) {
            // frameData[0] is mipmap 0
            int[] pixels = frameData[0];
            for (int i = 0; i < pixels.length; i++) {
                recolourPixel(pixels, i);
            }
        }
    }

    private void recolourPixel(int[] pixels, int i) {
        int rgba = pixels[i];
        int r = recolourSubPixel(rgba, 0);
        int g = recolourSubPixel(rgba, 8);
        int b = recolourSubPixel(rgba, 16);
        int a = 0xFF;// recolourSubPixel(rgba, 24);
        pixels[i] = (a << 24) | (b << 16) | (g << 8) | r;
    }

    private int recolourSubPixel(int rgba, int offset) {
        int data = (rgba >>> offset) & 0xFF;
        int dark = (colourDark >>> offset) & 0xFF;
        int light = (colourLight >>> offset) & 0xFF;
        return (dark * (256 - data) + light * data) / 256;
    }
}
