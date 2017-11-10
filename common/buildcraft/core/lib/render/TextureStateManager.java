/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.render;

import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.api.core.render.ITextureStateManager;

/*
 * This class moves texture management from PipeRenderState 
 * to be filled while rendering as efficient as possible
 */

@SideOnly(Side.CLIENT)
public final class TextureStateManager implements ITextureStateManager {

	private IIcon currentTexture;
	private IIcon[] textureArray;
	private IIcon[] textureArrayCache;

	public TextureStateManager(IIcon placeholder) {
		currentTexture = placeholder;
		textureArrayCache = new IIcon[6];
	}

	public IIcon[] popArray() {
		textureArray = textureArrayCache;
		return textureArrayCache; //Thread safety. Seriously.
	}

	public void pushArray() {
		textureArray = null;
	}


	public IIcon getTexture() {
		return currentTexture;
	}

	public IIcon[] getTextureArray() {
		return textureArray;
	}

	public boolean isSided() {
		return textureArray != null;
	}

	public void set(IIcon icon) {
		currentTexture = icon;
	}
}
