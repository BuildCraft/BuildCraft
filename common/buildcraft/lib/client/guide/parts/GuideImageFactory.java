/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.PngSizeInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.BCLog;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.sprite.ISprite;
import buildcraft.lib.client.sprite.RawSprite;
import buildcraft.lib.client.sprite.SpriteAtlas;

public class GuideImageFactory implements GuidePartFactory {
    private final ISprite sprite;
    private final int srcWidth, srcHeight;
    private final int width, height;

    public GuideImageFactory(String location) {
        this(location, -1, -1);
    }

    public GuideImageFactory(String location, int width, int height) {
        TextureMap textureMap = Minecraft.getMinecraft().getTextureMapBlocks();
        TextureAtlasSprite stitched = textureMap.getAtlasSprite(location);
        if (stitched != textureMap.getMissingSprite()) {
            this.sprite = new SpriteAtlas(stitched);
            this.srcWidth = stitched.getIconWidth();
            this.srcHeight = stitched.getIconHeight();
        } else {
            ISprite s;
            int sw, sh;
            ResourceLocation resLoc = new ResourceLocation(location);
            try (IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(resLoc)) {
                PngSizeInfo size = PngSizeInfo.makeFromResource(resource);
                s = new RawSprite(resLoc, 0, 0, 1, 1);
                sw = size.pngWidth;
                sh = size.pngHeight;
            } catch (IOException io) {
                BCLog.logger.warn("[lib.guide.loader.image] Couldn't load image '" + resLoc + "' because " + io.getMessage());
                stitched = textureMap.getMissingSprite();
                s = new SpriteAtlas(stitched);
                sw = stitched.getIconWidth();
                sh = stitched.getIconHeight();
            }
            this.sprite = s;
            this.srcWidth = sw;
            this.srcHeight = sh;
        }
        this.width = width <= 0 ? srcWidth : width;
        this.height = height <= 0 ? srcHeight : height;
    }

    @Override
    public GuideImage createNew(GuiGuide gui) {
        return new GuideImage(gui, sprite, srcWidth, srcHeight, width, height);
    }
}
