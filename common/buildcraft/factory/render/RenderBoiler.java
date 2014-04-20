package buildcraft.factory.render;

import buildcraft.factory.TileRefineryController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderBoiler extends TileEntitySpecialRenderer {

	public static final ResourceLocation TEXTURE = new ResourceLocation("buildcraft:textures/models/boiler.png");

	public static ModelBoiler model;

	static {
		model = new ModelBoiler();
	}

	public void renderBoilerAt(TileRefineryController tile, double x, double y, double z, float partial) {
		GL11.glPushMatrix();

		GL11.glTranslated(x + 1.5, y + 0.5, z + 0.5);

		GL11.glRotated(180, 1, 0, 0);

		GL11.glRotated(90, 0, 1, 0);

		if (tile.formed) {
			Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);
			model.render(0.0625F);
		}

		GL11.glPopMatrix();
	}

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partial) {
		renderBoilerAt((TileRefineryController) tile, x, y, z, partial);
	}

}
