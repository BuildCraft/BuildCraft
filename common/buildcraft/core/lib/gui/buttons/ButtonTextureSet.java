/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.gui.buttons;

import net.minecraft.util.ResourceLocation;

public class ButtonTextureSet implements IButtonTextureSet {
	private final ResourceLocation texture;
	private final int x, y, height, width;

	public ButtonTextureSet(int x, int y, int height, int width) {
		this(x, y, height, width, StandardButtonTextureSets.BUTTON_TEXTURES);
	}

	public ButtonTextureSet(int x, int y, int height, int width, ResourceLocation texture) {
		this.x = x;
		this.y = y;
		this.height = height;
		this.width = width;
		this.texture = texture;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public ResourceLocation getTexture() {
		return texture;
	}
}
