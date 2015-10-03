package buildcraft.core.lib.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class DynamicTextureBC {
	public final int width, height;
	public int[] colorMap;

	@SideOnly(Side.CLIENT)
	protected DynamicTexture dynamicTexture;

	public DynamicTextureBC(int iWidth, int iHeight) {
		width = iWidth;
		height = iHeight;
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			createDynamicTexture();
		} else {
			colorMap = new int[iWidth * iHeight];
		}
	}

	@SideOnly(Side.CLIENT)
	private void createDynamicTexture() {
		dynamicTexture = new DynamicTexture(width, height);
		colorMap = dynamicTexture.getTextureData();
	}

	public void setColord(int index, double r, double g, double b, double a) {
		int i = (int) (a * 255.0F);
		int j = (int) (r * 255.0F);
		int k = (int) (g * 255.0F);
		int l = (int) (b * 255.0F);
		colorMap[index] = i << 24 | j << 16 | k << 8 | l;
	}

	public void setColord(int x, int y, double r, double g, double b, double a) {
		setColord(x + y * width, r, g, b, a);
	}

	public void setColori(int index, int r, int g, int b, int a) {
		colorMap[index] = (a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | (b & 255);
	}

	public void setColori(int x, int y, int r, int g, int b, int a) {
		setColori(x + y * width, r, g, b, a);
	}

	public void setColor(int x, int y, int color) {
		colorMap[x + y * width] = color;
	}

	public void setColor(int x, int y, int color, float alpha) {
		int a = (int) (alpha * 255.0F);

		colorMap[x + y * width] = a << 24 | color;
	}

	@SideOnly(Side.CLIENT)
	public void updateTexture() {
		dynamicTexture.updateDynamicTexture();
	}


	@SideOnly(Side.CLIENT)
	public void draw(int screenX, int screenY, float zLevel) {
		draw(screenX, screenY, zLevel, 0, 0, width, height);
	}

	@SideOnly(Side.CLIENT)
	public void draw(int screenX, int screenY, float zLevel, int clipX, int clipY, int clipWidth, int clipHeight) {
		updateTexture();

		float f = 1F / width;
		float f1 = 1F / height;
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(
				screenX + 0,
				screenY + clipHeight,
				zLevel,
				(clipX + 0) * f,
				(clipY + clipHeight) * f1);
		tessellator.addVertexWithUV(
				screenX + clipWidth,
				screenY + clipHeight,
				zLevel,
				(clipX + clipWidth) * f,
				(clipY + clipHeight) * f1);
		tessellator.addVertexWithUV(
				screenX + clipWidth,
				screenY + 0,
				zLevel,
				(clipX + clipWidth) * f,
				(clipY + 0) * f1);
		tessellator.addVertexWithUV(
				screenX + 0,
				screenY + 0,
				zLevel,
				(clipX + 0) * f,
				(clipY + 0) * f1);
		tessellator.draw();
	}
}
