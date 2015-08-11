package buildcraft.energy.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

import buildcraft.core.lib.render.RenderEntityBlock;
import buildcraft.energy.BlockFlywheel;
import buildcraft.energy.TileFlywheel;

public class RenderFlywheel extends TileEntitySpecialRenderer {
	private static final float Z_OFFSET = 1 / 2048.0F;

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
		bindTexture(TextureMap.locationBlocksTexture);
		RenderEntityBlock.RenderInfo renderBox = new RenderEntityBlock.RenderInfo();
		renderBox.setBounds(0.125, 0.875, 0.125, 0.875, 1.0 - Z_OFFSET, 0.875);
		renderBox.textureArray = new IIcon[] {
				BlockFlywheel.FW_TOP,
				BlockFlywheel.FW_TOP,
				BlockFlywheel.FW_SIDE,
				BlockFlywheel.FW_SIDE,
				BlockFlywheel.FW_SIDE,
				BlockFlywheel.FW_SIDE,
		};

		GL11.glPushMatrix();

		GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
		GL11.glRotatef(((TileFlywheel) tile).getClientRotation(partialTicks), 0, 1, 0);
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

		renderBox.setRenderAllSides();
		renderBox.renderSide[0] = false;
		RenderEntityBlock.INSTANCE.renderBlock(renderBox);

		GL11.glPopMatrix();
	}
}
