/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import com.sun.prism.util.tess.Tess;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BCDynamicTexture {

	public int width, height;
	public int[] colorMap;

	@SideOnly(Side.CLIENT)
	private DynamicTexture dynamicTexture;

	public BCDynamicTexture(int iWidth, int iHeight) {
		width = iWidth;
		height = iHeight;
		colorMap = new int[iWidth * iHeight];
	}

	@SideOnly(Side.CLIENT)
	public void createDynamicTexture() {
		dynamicTexture = new DynamicTexture(width, height);
		colorMap = dynamicTexture.getTextureData();
	}

	public void setColor(int index, double r, double g, double b, double a) {
		int i = (int) (a * 255.0F);
		int j = (int) (r * 255.0F);
		int k = (int) (g * 255.0F);
		int l = (int) (b * 255.0F);
		colorMap[index] = i << 24 | j << 16 | k << 8 | l;
	}

	public void setColor(int x, int y, double r, double g, double b, double a) {
		int i = (int) (a * 255.0F);
		int j = (int) (r * 255.0F);
		int k = (int) (g * 255.0F);
		int l = (int) (b * 255.0F);
		colorMap[x + y * width] = i << 24 | j << 16 | k << 8 | l;
	}

	public void setColor(int x, int y, int color) {
		colorMap[x + y * height] = 255 << 24 | color;
	}

	public void setColor(int x, int y, int color, float alpha) {
		int a = (int) (alpha * 255.0F);

		colorMap[x + y * height] = a << 24 | color;
	}

	@SideOnly(Side.CLIENT)
	public void drawMap(int screenX, int screenY, float zLevel) {
		drawMap(screenX, screenY, zLevel, 0, 0, width, height);
	}

	@SideOnly(Side.CLIENT)
	public void updateDynamicTexture() {
		dynamicTexture.updateDynamicTexture();
	}

	@SideOnly(Side.CLIENT)
	public void drawMap(int screenX, int screenY, float zLevel, int clipX, int clipY, int clipWidth, int clipHeight) {
		dynamicTexture.updateDynamicTexture();

		float f = 1F / width;
		float f1 = 1F / height;
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer renderer = tessellator.getWorldRenderer();
		renderer.startDrawingQuads();
		renderer.addVertexWithUV(
				screenX + 0,
				screenY + clipHeight,
				zLevel,
				(clipX + 0) * f,
				(clipY + clipHeight) * f1);
		renderer.addVertexWithUV(
				screenX + clipWidth,
				screenY + clipHeight,
				zLevel,
				(clipX + clipWidth) * f,
				(clipY + clipHeight) * f1);
		renderer.addVertexWithUV(
				screenX + clipWidth,
				screenY + 0,
				zLevel,
				(clipX + clipWidth) * f,
				(clipY + 0) * f1);
		renderer.addVertexWithUV(
				screenX + 0,
				screenY + 0,
				zLevel,
				(clipX + 0) * f,
				(clipY + 0) * f1);
		tessellator.draw();
	}
}
