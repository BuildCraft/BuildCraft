/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.resource;

import java.io.IOException;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.PngSizeInfo;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuideImage;
import buildcraft.lib.client.guide.parts.GuidePartFactory;

public class TextureResourceHolder extends ResourceHolder implements GuidePartFactory {
    private boolean langFallback;
    private final int dispWidth, dispHeight;
    private int texWidth, texHeight;

    public TextureResourceHolder(ResourceLocation location) {
        this(location, -1, -1);
    }

    public TextureResourceHolder(ResourceLocation location, int width, int height) {
        super(location);
        dispWidth = width;
        dispHeight = height;
    }

    @Override
    protected byte[] load(IResourceManager resourceManager) {
        if (createFrom(getLocationForLang(false))) {
            langFallback = false;
        } else if (createFrom(getLocationForLang(true))) {
            langFallback = true;
        }
        return null;
    }

    private boolean createFrom(ResourceLocation location) {
        try (IResource res = Minecraft.getMinecraft().getResourceManager().getResource(location)) {
            PngSizeInfo info = new PngSizeInfo(res.getInputStream());
            texWidth = info.pngWidth;
            texHeight = info.pngHeight;
            return true;
        } catch (IOException io) {
            return false;
        }
    }

    @Override
    protected void onLoad(byte[] data) {}

    @Override
    public GuideImage createNew(GuiGuide gui) {
        return new GuideImage(gui, getLocationForLang(langFallback), texWidth, texHeight, dispWidth, dispHeight);
    }
}
