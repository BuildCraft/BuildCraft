/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.urbanism;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import buildcraft.core.DefaultProps;
import buildcraft.core.render.RenderBox;

public class RenderUrbanist extends TileEntitySpecialRenderer {

	private static final ResourceLocation LASER_RED = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_ENTITIES + "/laser_1.png");
	private static final ResourceLocation LASER_YELLOW = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_ENTITIES + "/laser_2.png");
	private static final ResourceLocation LASER_GREEN = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_ENTITIES + "/laser_3.png");
	private static final ResourceLocation LASER_BLUE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_ENTITIES + "/laser_4.png");
	private static final ResourceLocation STRIPES = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_ENTITIES + "/stripes.png");

	public RenderUrbanist() {
	}

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {
		TileUrbanist urbanist = (TileUrbanist) tileentity;

		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		for (AnchoredBox b : urbanist.frames) {
			GL11.glPushMatrix();
			GL11.glTranslated(-tileentity.xCoord, -tileentity.yCoord, -tileentity.zCoord);

			ResourceLocation texture = LASER_RED;

			switch (b.kind) {
			case LASER_RED:
				texture = LASER_RED;
				break;
			case LASER_YELLOW:
				texture = LASER_YELLOW;
				break;
			case LASER_GREEN:
				texture = LASER_GREEN;
				break;
			case LASER_BLUE:
				texture = LASER_BLUE;
				break;
			case STRIPES:
				texture = STRIPES;
				break;
			}

			RenderBox.doRender(TileEntityRendererDispatcher.instance.field_147553_e, texture, b.box, x, y, z, f, 0);
			GL11.glPopMatrix();
		}

		GL11.glPopAttrib();
		GL11.glPopMatrix();

		/*TileEnergyEmitter emitter = ((TileEnergyEmitter) tileentity);

		if (emitter != null) {
			GL11.glPushMatrix();
			GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			GL11.glTranslated(x, y, z);

			for (Target t : emitter.targets.values()) {
				GL11.glPushMatrix();
				GL11.glTranslated(0.5F, 0.5F, 0.5F);
				RenderLaser.doRenderLaserWave(TileEntityRendererDispatcher.instance.field_147553_e,
						t.data, EntityLaser.LASER_TEXTURES[3]);
				GL11.glPopMatrix();
			}

			//GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glPopAttrib();
			GL11.glPopMatrix();
		}*/
	}
}
