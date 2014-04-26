package buildcraft.factory.render;

import buildcraft.factory.TileTowerRegulator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

public class RenderTower extends TileEntitySpecialRenderer {

	public static ModelTower.BaseSmall towerBaseSmall;
	public static ModelTower.BaseMedium towerBaseMedium;
	public static ModelTower.TopSmall towerTopSmall;
	public static ModelTower.TopMedium towerTopMedium;

	static {
		towerBaseSmall = new ModelTower.BaseSmall();
		towerBaseMedium = new ModelTower.BaseMedium();
		towerTopSmall = new ModelTower.TopSmall();
		towerTopMedium = new ModelTower.TopMedium();
	}

	public void renderTowerAt(TileTowerRegulator tile, double x, double y, double z, float partial) {
		GL11.glPushMatrix();

		GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);

		GL11.glRotated(180, 1, 0, 0);

		if (tile.formed) {
			if (tile.size == 3) {
				Minecraft.getMinecraft().renderEngine.bindTexture(ModelTower.TEXTURE_BASE_SMALL);
				towerBaseSmall.render(0.0625F);

				Minecraft.getMinecraft().renderEngine.bindTexture(ModelTower.TEXTURE_TOP_SMALL);
				for (int i = 1; i < tile.height - 1; i++) {
					GL11.glTranslated(0, -1, 0);
					if (i % 2 != 0) {
						towerTopSmall.render(0.0625F);
					}
				}
			} else if (tile.size == 5) {
				Minecraft.getMinecraft().renderEngine.bindTexture(ModelTower.TEXTURE_BASE_MEDIUM);
				towerBaseMedium.render(0.0625F);

				Minecraft.getMinecraft().renderEngine.bindTexture(ModelTower.TEXTURE_TOP_MEDIUM);
				for (int i = 1; i < tile.height - 1; i++) {
					GL11.glTranslated(0, -1, 0);
					if (i % 2 != 0) {
						towerTopMedium.render(0.0625F);
					}
				}
			}
		}

		GL11.glPopMatrix();
	}

	@Override
	public void renderTileEntityAt(TileEntity var1, double var2, double var4, double var6, float var8) {
		renderTowerAt((TileTowerRegulator) var1, var2, var4, var6, var8);
	}

}
