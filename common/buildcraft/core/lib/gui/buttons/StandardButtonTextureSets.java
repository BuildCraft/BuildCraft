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

public enum StandardButtonTextureSets implements IButtonTextureSet {
	LARGE_BUTTON(0, 0, 20, 200),
	SMALL_BUTTON(0, 80, 15, 200),
	LEFT_BUTTON(204, 0, 16, 10),
	RIGHT_BUTTON(214, 0, 16, 10);
	public static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation("buildcraftcore:textures/gui/buttons.png");
	private final int x, y, height, width;

	StandardButtonTextureSets(int x, int y, int height, int width) {
		this.x = x;
		this.y = y;
		this.height = height;
		this.width = width;
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
		return BUTTON_TEXTURES;
	}
}
