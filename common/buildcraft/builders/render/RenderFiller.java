package buildcraft.builders.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.tileentity.TileEntity;

import buildcraft.BuildCraftBuilders;
import buildcraft.builders.TileFiller;
import buildcraft.core.lib.render.RenderEntityBlock;
import buildcraft.core.render.RenderBuilder;

public class RenderFiller extends RenderBuilder {
	private static final float Z_OFFSET = 2049 / 2048.0F;

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {
		super.renderTileEntityAt(tileentity, x, y, z, f);

		bindTexture(TextureMap.locationBlocksTexture);
		RenderEntityBlock.RenderInfo renderBox = new RenderEntityBlock.RenderInfo();

		GL11.glPushMatrix();

		GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
		GL11.glScalef(Z_OFFSET, Z_OFFSET, Z_OFFSET);
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

		renderBox.setRenderSingleSide(BuildCraftBuilders.fillerBlock.getFrontSide(tileentity.getBlockMetadata()));
		renderBox.texture = ((TileFiller) tileentity).currentPattern.getBlockOverlay();
		RenderEntityBlock.INSTANCE.renderBlock(renderBox);

		GL11.glPopMatrix();
	}
}
