/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy.render;

import org.lwjgl.opengl.GL11;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftCore.RenderMode;
import buildcraft.core.DefaultProps;
import buildcraft.core.EntityLaser;
import buildcraft.core.render.RenderLaser;
import buildcraft.energy.TileEnergyEmitter;
import buildcraft.energy.TileEnergyEmitter.Target;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class RenderEnergyEmitter extends TileEntitySpecialRenderer {

	private ModelBase model = new ModelBase() {
	};
	private ModelRenderer box;

	private static final ResourceLocation CHAMBER_TEXTURE = new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/chamber2.png");

	public RenderEnergyEmitter() {
		box = new ModelRenderer(model, 0, 1);
		box.addBox(-8F, -8F, -8F, 16, 4, 16);
		box.rotationPointX = 8;
		box.rotationPointY = 8;
		box.rotationPointZ = 8;
	}

	public void renderTileEntityAt2(TileEntity tileentity, double x, double y, double z, float f) {

		if (BuildCraftCore.render == RenderMode.NoDynamic) {
			return;
		}

		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glColor3f(1, 1, 1);

		GL11.glTranslatef((float) x, (float) y, (float) z);

		float step;

		float[] angle = { 0, 0, 0 };

		box.rotateAngleX = angle[0];
		box.rotateAngleY = angle[1];
		box.rotateAngleZ = angle[2];


		float factor = (float) (1.0 / 16.0);

		//bindTexture(EntityLaser.LASER_TEXTURES[3]);
		bindTexture(CHAMBER_TEXTURE);

		box.render(factor);

		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}

	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {
		TileEnergyEmitter emitter = ((TileEnergyEmitter) tileentity);

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
				RenderLaser.doRenderLaserWave(tileEntityRenderer.renderEngine,
						t.data, EntityLaser.LASER_TEXTURES[3]);
				GL11.glPopMatrix();
			}

			//GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glPopAttrib();
			GL11.glPopMatrix();
		}
	}
}
