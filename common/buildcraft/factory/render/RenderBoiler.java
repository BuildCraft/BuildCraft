package buildcraft.factory.render;

import buildcraft.factory.TileRefineryController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

public class RenderBoiler extends TileEntitySpecialRenderer {

	public static ModelBoiler.Front modelFront;
	public static ModelBoiler.Middle modelMiddle;
	public static ModelBoiler.Back modelBack;

	static {
		modelFront = new ModelBoiler.Front();
		modelMiddle = new ModelBoiler.Middle();
		modelBack = new ModelBoiler.Back();
	}

	public void renderBoilerAt(TileRefineryController tile, double x, double y, double z, float partial) {
		GL11.glPushMatrix();

		GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

		GL11.glRotated(180, 1, 0, 0);

		float angle = 0;
		switch (tile.orientation) {
			case 2:
				angle = 0;
				break;
			case 3:
				angle = 180;
				break;
			case 4:
				angle = 270;
				break;
			case 5:
				angle = 90;
				break;
		}

		GL11.glRotated(angle, 0, 1, 0);

		if (tile.formed) {
			Minecraft.getMinecraft().renderEngine.bindTexture(ModelBoiler.TEXTURE_FRONT);
			modelFront.render(0.0625F);

			for (int i = 0; i < tile.length - 1; i++) {
				GL11.glTranslated(0, 0, 1);

				if (i == tile.length - 2) {
					Minecraft.getMinecraft().renderEngine.bindTexture(ModelBoiler.TEXTURE_BACK);
					modelBack.render(0.0625F);
				} else {
					Minecraft.getMinecraft().renderEngine.bindTexture(ModelBoiler.TEXTURE_MIDDLE);
					modelMiddle.render(0.0625F);
				}
			}
		}

		GL11.glPopMatrix();
	}

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partial) {
		renderBoilerAt((TileRefineryController) tile, x, y, z, partial);
	}

}
