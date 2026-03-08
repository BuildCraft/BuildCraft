/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.render.ISprite;
import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.sprite.SpriteAtlas;
import buildcraft.lib.client.sprite.SpriteRaw;
import com.mojang.blaze3d.platform.PngInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.util.function.Function;

public class GuideImageFactory implements GuidePartFactory {
    // private final ISprite sprite;
    private ISprite sprite;
    // private final int srcWidth, srcHeight;
    private int srcWidth, srcHeight;
    // private final int width, height;
    private int width, height;

    public GuideImageFactory(String location) {
        this(location, -1, -1);
    }

    // Calen: fmlPostInit -> RuntimeException: getAtlasTexture called too early!
    // delay load sprite
    private String locationRaw;
    private int widthRaw, heightRaw;

    public GuideImageFactory(String location, int width, int height) {
        this.locationRaw = location;
        this.widthRaw = width;
        this.heightRaw = height;
    }

    private void load() {
        String location = this.locationRaw;
        int width = this.widthRaw;
        int height = this.heightRaw;

//        TextureMap textureMap = Minecraft.getMinecraft().getTextureMapBlocks();
        Function<ResourceLocation, TextureAtlasSprite> textureMap = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS);
//        TextureAtlasSprite stitched = textureMap.getAtlasSprite(location);
        TextureAtlasSprite stitched = textureMap.apply(new ResourceLocation(location));
//        if (stitched != textureMap.getSprite(MissingTextureAtlasSprite.getLocation()))
        if (stitched != textureMap.apply(MissingTextureAtlasSprite.getLocation())) {
            this.sprite = new SpriteAtlas(stitched);
            this.srcWidth = stitched.getWidth();
            this.srcHeight = stitched.getHeight();
        } else {
            ISprite s;
            int sw, sh;
            ResourceLocation resLoc = new ResourceLocation(location);
            try (Resource resource = Minecraft.getInstance().getResourceManager().getResource(resLoc)) {
//                PngSizeInfo size = PngSizeInfo.makeFromResource(resource);
                PngInfo png = new PngInfo(resource.getSourceName(), resource.getInputStream());
                s = new SpriteRaw(resLoc, 0, 0, 1, 1);
//                sw = size.pngWidth;
                sw = png.width;
//                sh = size.pndgHeight;
                sh = png.height;
            } catch (IOException io) {
                BCLog.logger.warn("[lib.guide.loader.image] Couldn't load image '" + resLoc + "' because " + io.getMessage());
//                stitched = textureMap.getMissingSprite();
                stitched = textureMap.apply(MissingTextureAtlasSprite.getLocation());
                s = new SpriteAtlas(stitched);
                sw = stitched.getWidth();
                sh = stitched.getHeight();
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
        // Calen
        if (this.sprite == null) {
            load();
        }
        return new GuideImage(gui, sprite, srcWidth, srcHeight, width, height);
//        return new GuideImage(gui, new SpriteAtlas(new TextureAtlas(new ResourceLocation("")).getSprite(MissingTextureAtlasSprite.getLocation())), 0, 0, 0, 0);
    }
}
