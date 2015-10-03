/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.render;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.render.ITextureStateManager;

/* This class moves texture management from PipeRenderState to be filled while rendering as efficient as possible */

@SideOnly(Side.CLIENT)
public final class TextureStateManager implements ITextureStateManager {

    private TextureAtlasSprite currentTexture;
    private TextureAtlasSprite[] textureArray;
    private TextureAtlasSprite[] textureArrayCache;

    public TextureStateManager(TextureAtlasSprite placeholder) {
        currentTexture = placeholder;
        textureArrayCache = new TextureAtlasSprite[6];
    }

    public TextureAtlasSprite[] popArray() {
        textureArray = textureArrayCache;
        return textureArrayCache; // Thread safety. Seriously.
    }

    public void pushArray() {
        textureArray = null;
    }

    public TextureAtlasSprite getTexture() {
        return currentTexture;
    }

    public TextureAtlasSprite[] getTextureArray() {
        return textureArray;
    }

    public boolean isSided() {
        return textureArray != null;
    }

    @Override
    public void set(TextureAtlasSprite icon) {
        currentTexture = icon;
    }
}
