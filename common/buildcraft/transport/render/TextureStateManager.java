/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.render;


import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/*
 * This class moves texture management from PipeRenderState 
 * to be filled while rendering as efficient as possible
 */

@SideOnly(Side.CLIENT)
public final class TextureStateManager {
	
	/*private IIcon currentTexture;
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
	}*/
	
}
