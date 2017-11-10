/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.Position;
import buildcraft.core.EntityLaser;
import buildcraft.core.LaserData;
import buildcraft.core.lib.render.RenderEntityBlock;
import buildcraft.core.lib.render.RenderEntityBlock.RenderInfo;

public class RenderLaser extends Render {
	public static final float STEP = 0.04F;

	protected static ModelBase model = new ModelBase() {
	};
	private static ModelRenderer[] box;

	private static int[][][] scaledBoxes;

	public RenderLaser() {
	}

	public static void onTextureReload() {
		scaledBoxes = null;
	}

	private static ModelRenderer getBox(int index) {
		if (box == null) {
			box = new ModelRenderer[40];

			for (int j = 0; j < box.length; ++j) {
				box[j] = new ModelRenderer(model, box.length - j, 0);
				box[j].addBox(0, -0.5F, -0.5F, 16, 1, 1);
				box[j].rotationPointX = 0;
				box[j].rotationPointY = 0;
				box[j].rotationPointZ = 0;
			}
		}

		return box[index];
	}

	private static void initScaledBoxes() {
		if (scaledBoxes == null) {
			scaledBoxes = new int[2][100][20];

			for (int flags = 0; flags < 2; ++flags) {
				for (int size = 0; size < 100; ++size) {
					for (int i = 0; i < 20; ++i) {
						scaledBoxes[flags][size][i] = GLAllocation.generateDisplayLists(1);
						GL11.glNewList(scaledBoxes[flags][size][i], GL11.GL_COMPILE);

						RenderInfo block = new RenderInfo();

						float minSize = 0.2F * size / 100F;
						float maxSize = 0.4F * size / 100F;
						//float minSize = 0.1F;
						//float maxSize = 0.2F;

						float range = maxSize - minSize;

						float diff = MathHelper.cos(i / 20F * 2 * (float) Math.PI)
								* range / 2F;

						block.minX = 0.0;
						block.minY = -maxSize / 2F + diff;
						block.minZ = -maxSize / 2F + diff;

						block.maxX = STEP;
						block.maxY = maxSize / 2F - diff;
						block.maxZ = maxSize / 2F - diff;

						if (flags == 1) {
							block.brightness = 15;
						}

						RenderEntityBlock.INSTANCE.renderBlock(block);

						GL11.glEndList();
					}
				}
			}
		}
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f, float f1) {
		doRender((EntityLaser) entity, x, y, z, f, f1);
		//entity.setAngles(45, 180);
	}

	private void doRender(EntityLaser laser, double x, double y, double z, float f, float f1) {
		if (!laser.isVisible() || laser.getTexture() == null) {
			return;
		}

		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glDisable(GL11.GL_LIGHTING);

		Position offset = laser.renderOffset();
		GL11.glTranslated(x + offset.x, y + offset.y, z + offset.z);

		// FIXME: WARNING! not using getBox (laser) will kill laser movement.
		// we can use some other method for the animation though.
		doRenderLaser(renderManager.renderEngine, laser.data, laser.getTexture());

		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}

	public static void doRenderLaserWave(TextureManager textureManager, LaserData laser, ResourceLocation texture) {
		if (!laser.isVisible || texture == null) {
			return;
		}

		GL11.glPushMatrix();

		GL11.glTranslated(laser.head.x, laser.head.y, laser.head.z);
		laser.update();

		GL11.glRotatef((float) laser.angleZ, 0, 1, 0);
		GL11.glRotatef((float) laser.angleY, 0, 0, 1);

		textureManager.bindTexture(texture);

		int indexList = 0;

		initScaledBoxes();

		double x1 = laser.wavePosition;
		double x2 = x1 + scaledBoxes[0][0].length * STEP;
		double x3 = laser.renderSize;

		doRenderLaserLine(x1, laser.laserTexAnimation);

		for (double i = x1; i <= x2 && i <= laser.renderSize; i += STEP) {
			GL11.glCallList(scaledBoxes[laser.isGlowing ? 1 : 0][(int) (laser.waveSize * 99F)][indexList]);
			indexList = (indexList + 1) % scaledBoxes[0][0].length;
			GL11.glTranslated(STEP, 0, 0);
		}

		if (x2 < x3) {
			doRenderLaserLine(x3 - x2, laser.laserTexAnimation);
		}

		GL11.glPopMatrix();
	}


	public static void doRenderLaser(TextureManager textureManager, LaserData laser, ResourceLocation texture) {
		if (!laser.isVisible || texture == null) {
			return;
		}

		GL11.glPushMatrix();

		GL11.glTranslated(laser.head.x, laser.head.y, laser.head.z);
		laser.update();

		GL11.glRotatef((float) laser.angleZ, 0, 1, 0);
		GL11.glRotatef((float) laser.angleY, 0, 0, 1);

		textureManager.bindTexture(texture);

		initScaledBoxes();

		if (laser.isGlowing) {
			float lastBrightnessX = OpenGlHelper.lastBrightnessX;
			float lastBrightnessY = OpenGlHelper.lastBrightnessY;

			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
			GL11.glDisable(GL11.GL_LIGHTING);

			doRenderLaserLine(laser.renderSize, laser.laserTexAnimation);

			GL11.glEnable(GL11.GL_LIGHTING);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
		} else {
			doRenderLaserLine(laser.renderSize, laser.laserTexAnimation);
		}

		GL11.glPopMatrix();
	}

	private static void doRenderLaserLine(double len, int texId) {
		float lasti = 0;

		if (len - 1 > 0) {
			for (float i = 0; i <= len - 1; i += 1) {
				getBox(texId).render(1F / 16F);
				GL11.glTranslated(1, 0, 0);
				lasti = i;
			}
			lasti++;
		}

		GL11.glPushMatrix();
		GL11.glScalef((float) len - lasti, 1, 1);
		getBox(texId).render(1F / 16F);
		GL11.glPopMatrix();

		GL11.glTranslated((float) (len - lasti), 0, 0);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return ((EntityLaser) entity).getTexture();
	}
}
