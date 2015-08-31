package buildcraft.robotics.render;

import java.util.HashMap;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.material.MapColor;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.core.lib.render.DynamicTextureBC;
import buildcraft.core.lib.render.FakeIcon;
import buildcraft.core.lib.render.RenderEntityBlock;
import buildcraft.robotics.TileZonePlan;

public class RenderZonePlan extends TileEntitySpecialRenderer {
	private static final float Z_OFFSET = 2049 / 2048.0F;
	private static final HashMap<TileZonePlan, DynamicTextureBC> TEXTURES = new HashMap<TileZonePlan, DynamicTextureBC>();

	@Override
	public void renderTileEntityAt(TileEntity tile, double tx, double ty, double tz, float partialTicks) {
		boolean rendered = true;
		TileZonePlan zonePlan = (TileZonePlan) tile;

		if (!TEXTURES.containsKey(zonePlan)) {
			DynamicTextureBC textureBC = new DynamicTextureBC(16, 16);
			TEXTURES.put(zonePlan, textureBC);
			rendered = false;
		}
		DynamicTextureBC textureBC = TEXTURES.get(zonePlan);
		FakeIcon fakeIcon = new FakeIcon(0, 1, 0, 1, 16, 16);

		byte[] previewColors = zonePlan.getPreviewTexture(!rendered);

		if (previewColors != null) {
			for (int y = 0; y < 8; y++) {
				for (int x = 0; x < 10; x++) {
					int col = MapColor.mapColorArray[previewColors[y * 10 + x]].colorValue;
					if ((x & 1) != (y & 1)) {
						int ocol = col;
						col = (ocol & 0xFF) * 15 / 16
								| (((ocol & 0xFF00) >> 8) * 15 / 16) << 8
								| (((ocol & 0xFF0000) >> 16) * 15 / 16) << 16;
					}
					textureBC.setColor(x + 3, y + 3, 0xFF000000 | col);
				}
			}
		}

		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT);

		GL11.glTranslatef((float) tx + 0.5F, (float) ty + 0.5F, (float) tz + 0.5F);
		GL11.glScalef(Z_OFFSET, Z_OFFSET, Z_OFFSET);
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

		textureBC.updateTexture();

		RenderEntityBlock.RenderInfo renderBox = new RenderEntityBlock.RenderInfo();
		renderBox.setRenderSingleSide(((BlockBuildCraft) zonePlan.getBlockType()).getFrontSide(zonePlan.getBlockMetadata()));
		renderBox.texture = fakeIcon;
		renderBox.light = 15;
		RenderEntityBlock.INSTANCE.renderBlock(renderBox);

		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}
}
